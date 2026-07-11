package com.ruoyi.web.controller.bi;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.ocr.OcrService;
import com.ruoyi.common.ocr.vo.OcrResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * BI - OCR 文档识别控制器
 *
 * 架构：Java 后端 → ProcessBuilder 调 Python 脚本 → PaddleOCR 识别 → stdout 返回 JSON
 *
 * 接口列表：
 *   POST /bi/ocr/recognize       上传图片识别文字（默认简体中文）
 *   POST /bi/ocr/recognize-advanced  上传图片 + 指定语言识别
 *   POST /bi/ocr/recognize-file      识别服务器本地图片文件
 *   GET  /bi/ocr/health            健康检查
 *
 * @author 若依-BI扩展
 */
@Api("BI-OCR文档识别")
@RestController
@RequestMapping("/bi/ocr")
public class BiOcrController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BiOcrController.class);

    @Autowired
    private OcrService ocrService;

    /**
     * 上传图片识别文字（默认简体中文）
     *
     * 请求：multipart/form-data
     * 参数：file = 图片文件（PNG/JPG/BMP/TIFF）
     *
     * 返回：
     * {
     *   "msg": "操作成功",
     *   "code": 200,
     *   "data": {
     *     "text": "识别出的文字内容",
     *     "language": "ch",
     *     "length": 123,
     *     "pythonTimeMs": 2345,
     *     "totalTimeMs": 2456,
     *     "filename": "test.png"
     *   }
     * }
     */
    @PreAuthorize("@ss.hasPermi('bi:ocr:query')")
    @ApiOperation("上传图片识别文字（默认简体中文）")
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请上传图片文件");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.startsWith("image/") ||
                  "application/pdf".equals(contentType))) {
            return error("仅支持图片文件（PNG/JPG/BMP/TIFF）");
        }

        log.info("OCR识别请求，文件名：{}，大小：{}KB", file.getOriginalFilename(), file.getSize() / 1024);
        try {
            OcrResult result = ocrService.recognize(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    null  // null = 使用脚本默认语言（ch）
            );

            Map<String, Object> data = new HashMap<>();
            data.put("text", result.getText());
            data.put("language", "ch");
            data.put("length", result.getText().length());
            data.put("pythonTimeMs", result.getPythonTimeMs());
            data.put("totalTimeMs", result.getTotalTimeMs());
            data.put("filename", file.getOriginalFilename());

            return success(data);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return error("识别失败：" + e.getMessage());
        }
    }

    /**
     * 上传图片识别文字（指定语言）
     *
     * 请求：multipart/form-data
     * 参数：file = 图片文件，language = 语言代码
     *
     * 语言代码示例：
     *   ch         简体中文（PaddleOCR 默认）
     *   en         英语
     *   ch+en      中英混合（PaddleOCR 支持多语言同时识别）
     *   japan      日语
     *   korean     韩语
     */
    @PreAuthorize("@ss.hasPermi('bi:ocr:query')")
    @ApiOperation("上传图片识别文字（指定语言）")
    @PostMapping("/recognize-advanced")
    public AjaxResult recognizeAdvanced(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "ch") String language) {

        if (file == null || file.isEmpty()) {
            return error("请上传图片文件");
        }

        log.info("OCR识别请求（高级），文件名：{}，语言：{}", file.getOriginalFilename(), language);
        try {
            OcrResult result = ocrService.recognize(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    language
            );

            Map<String, Object> data = new HashMap<>();
            data.put("text", result.getText());
            data.put("language", language);
            data.put("length", result.getText().length());
            data.put("pythonTimeMs", result.getPythonTimeMs());
            data.put("totalTimeMs", result.getTotalTimeMs());
            data.put("filename", file.getOriginalFilename());

            return success(data);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return error("识别失败：" + e.getMessage());
        }
    }

    /**
     * 识别服务器本地图片文件（供内部服务调用，不走上传）
     *
     * 参数：filePath = 服务器本地图片绝对路径
     *       language = 语言（可选）
     */
    @PreAuthorize("@ss.hasPermi('bi:ocr:query')")
    @ApiOperation("识别服务器本地图片文件")
    @PostMapping("/recognize-file")
    public AjaxResult recognizeFile(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "language", required = false) String language) {

        log.info("OCR识别本地文件，路径：{}，语言：{}", filePath, language);
        try {
            OcrResult result = ocrService.recognizeFile(filePath, language);
            Map<String, Object> data = new HashMap<>();
            data.put("text", result.getText());
            data.put("language", language != null ? language : "ch");
            data.put("length", result.getText().length());
            data.put("pythonTimeMs", result.getPythonTimeMs());
            data.put("totalTimeMs", result.getTotalTimeMs());
            data.put("imagePath", result.getImagePath());
            return success(data);
        } catch (Exception e) {
            log.error("OCR识别本地文件失败", e);
            return error("识别失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查（无需权限，供前端探测 OCR 服务是否可用）
     *
     * 实际检查：调用 OcrService.checkHealth() 验证 Python 环境 + 脚本文件
     */
    @ApiOperation("OCR服务健康检查")
    @GetMapping("/health")
    public AjaxResult health() {
        try {
            String result = ocrService.checkHealth();
            return success(result);
        } catch (Exception e) {
            return error("OCR服务异常：" + e.getMessage());
        }
    }
}
