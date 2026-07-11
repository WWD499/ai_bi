package com.ruoyi.common.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.ocr.vo.OcrResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OCR 识别服务（Java 调 Python 脚本方案）
 *
 * 架构：
 *   Java 后端 → ProcessBuilder 调 Python 脚本 → PaddleOCR 识别 → stdout 返回 JSON → Java 解析
 *
 * 配置说明（在 application.yml 中）：
 *   ocr.python-path       Python 可执行文件路径（如 D:/Python39/python.exe 或 /usr/bin/python3）
 *   ocr.script-path      OCR Python 脚本路径（如 D:/工程组/RuoYi-Vue-v3.8.7/ruoyi-admin/ocr_paddle.py）
 *   ocr.timeout-ms       识别超时时间（毫秒，默认 60000）
 *
 * Python 脚本要求：
 *   接收命令行参数：<图片路径> [语言]
 *   输出 JSON 到 stdout：{"code": 0, "text": "...", "time_ms": 123}
 *   错误输出到 stderr
 *
 * @author 若依-BI扩展
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    /**
     * Python 可执行文件绝对路径
     * 例：D:/Python39/python.exe 或 /usr/bin/python3
     */
    @Value("${ocr.python-path}")
    private String pythonPath;

    /**
     * OCR Python 脚本绝对路径
     * 例：D:/工程组/RuoYi-Vue-v3.8.7/ruoyi-admin/ocr_paddle.py
     */
    @Value("${ocr.script-path}")
    private String scriptPath;

    /**
     * 识别超时时间（毫秒）
     */
    @Value("${ocr.timeout-ms:60000}")
    private long timeoutMs;

    /**
     * 识别上传的图片文件（MultipartFile）
     *
     * 流程：
     *   1. 将上传的文件临时保存到本地
     *   2. 调用 Python 脚本识别
     *   3. 解析 stdout 的 JSON 结果
     *   4. 删除临时文件
     *
     * @param file       上传的图片文件
     * @param language   语言（null 则用默认 ch）
     * @return 识别出的文字
     */
    public OcrResult recognize(InputStream inputStream, String originalFilename, String language) {
        // 1. 保存临时文件
        Path tempFile;
        try {
            String suffix = getFileSuffix(originalFilename);
            tempFile = Files.createTempFile("ocr-upload-", suffix);
            try (OutputStream os = Files.newOutputStream(tempFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
            }
            log.info("OCR临时文件：{}", tempFile);
        } catch (IOException e) {
            throw new ServiceException("保存上传文件失败：" + e.getMessage());
        }

        // 2. 调用 Python 脚本
        try {
            return doCallPython(tempFile.toAbsolutePath().toString(), language);
        } finally {
            // 3. 删除临时文件
            try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
        }
    }

    /**
     * 识别本地图片文件
     *
     * @param filePath  本地图片绝对路径
     * @param language 语言（null 则用默认 ch）
     * @return 识别出的文字
     */
    public OcrResult recognizeFile(String filePath, String language) {
        if (!new File(filePath).exists()) {
            throw new ServiceException("文件不存在：" + filePath);
        }
        return doCallPython(filePath, language);
    }

    /**
     * 核心：通过 ProcessBuilder 调用 Python OCR 脚本
     *
     * Python 脚本输出格式（JSON 到 stdout）：
     *   {"code": 0, "text": "识别文字...", "time_ms": 1234, "language": "ch"}
     *   {"code": -1, "msg": "错误信息"}
     */
    private OcrResult doCallPython(String imagePath, String language) {
        long startTime = System.currentTimeMillis();

        // 构建命令行参数
        // python ocr_paddle.py <图片路径> [语言]
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                scriptPath,
                imagePath,
                language != null ? language : "ch"
        );

        // === 关键修复：强制子进程使用 UTF-8 编码 ===
        // Windows 下 JVM 启动的子进程可能继承系统默认编码（GBK/cp936），
        // 导致 Python 的 print() 输出 GBK 字节，Java 端按 UTF-8 读取即产生乱码（大量 �）
        // 设置 PYTHONIOENCODING 强制 Python 所有 I/O 使用 UTF-8
        Map<String, String> env = pb.environment();
        env.put("PYTHONIOENCODING", "utf-8");

        // 合并 stderr 到 stdout，方便统一读取（也可以分开读）
        pb.redirectErrorStream(true);

        log.info("调用 Python OCR：{}", String.join(" ", pb.command()));

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new ServiceException("启动 Python 进程失败，请检查 python-path 配置：" + e.getMessage());
        }

        // 读取 stdout（Python 的 print 输出）
        // PaddlePaddle C++ 引擎可能在 stdout 输出诊断信息，只保留最后一行 JSON
        StringBuilder output = new StringBuilder();
        String lastJsonLine = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                // JSON 行以 { 开头，保留最后一个（即脚本的 print 输出）
                String trimmed = line.trim();
                if (trimmed.startsWith("{")) {
                    lastJsonLine = trimmed;
                }
            }
        } catch (IOException e) {
            log.warn("读取 Python 输出失败：{}", e.getMessage());
        }

        // 等待进程结束（带超时）
        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new ServiceException("OCR识别被中断");
        }

        if (!finished) {
            process.destroyForcibly();
            throw new ServiceException("OCR识别超时（>" + timeoutMs + "ms），请检查图片大小或 Python 环境");
        }

        int exitCode = process.exitValue();
        String outputStr = lastJsonLine.isEmpty() ? output.toString().trim() : lastJsonLine;

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("Python OCR 进程结束，exitCode={}，总耗时={}ms，JSON长度={}", exitCode, totalTime, outputStr.length());

        // 解析 Python 输出的 JSON（只解析提取的最后一行 JSON）
        try {
            if (exitCode != 0) {
                throw new ServiceException("Python OCR 进程异常退出（exitCode=" + exitCode + "），输出：" + outputStr);
            }

            // 用 Jackson 解析 JSON（ruoyi-common 已有 jackson-databind）
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(outputStr);

            int code = node.get("code").asInt();
            if (code != 0) {
                String msg = node.has("msg") ? node.get("msg").asText() : "未知错误";
                throw new ServiceException("OCR识别失败（code=" + code + "）：" + msg);
            }

            String text = node.has("text") ? node.get("text").asText("") : "";
            int pyTime = node.has("time_ms") ? node.get("time_ms").asInt(0) : 0;

            log.info("OCR识别成功，文字长度={}，Python耗时={}ms，总耗时={}ms", text.length(), pyTime, totalTime);

            OcrResult result = new OcrResult();
            result.setText(text);
            result.setPythonTimeMs(pyTime);
            result.setTotalTimeMs((int) totalTime);
            result.setImagePath(imagePath);
            return result;

        } catch (Exception e) {
            if (e instanceof ServiceException) throw (ServiceException) e;
            // JSON 解析失败，可能是 Python 脚本报错了
            throw new ServiceException("解析 Python 输出失败，原始输出：" + outputStr + "，错误：" + e.getMessage());
        }
    }

    /**
     * 健康检查：验证 Python 环境是否可用
     *
     * @return 成功返回 Python 版本信息，失败抛出异常
     */
    public String checkHealth() {
        ProcessBuilder pb = new ProcessBuilder(pythonPath, "--version");
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new ServiceException("Python 健康检查超时");
            }
            int exitCode = p.exitValue();
            if (exitCode != 0) {
                throw new ServiceException("Python 执行失败，exitCode=" + exitCode);
            }
            String version = sb.toString().trim();
            if (version.isEmpty()) {
                version = "(无法获取版本号，但 Python 可执行)";
            }

            // 同时检查脚本文件是否存在
            if (!new File(scriptPath).exists()) {
                throw new ServiceException("OCR 脚本不存在：" + scriptPath);
            }

            return "Python 环境正常，版本：" + version + "，脚本：" + scriptPath;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Python 健康检查失败：" + e.getMessage()
                    + "，请检查 ocr.python-path 配置（当前值：" + pythonPath + "）");
        }
    }

    private String getFileSuffix(String filename) {
        if (filename == null) return ".tmp";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : ".tmp";
    }
}
