#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
BI 平台 OCR 识别脚本（PaddleOCR 3.x）

供 Java 后端通过命令行调用：
    python ocr_paddle.py <图片路径> [语言]

输出格式（JSON，打印到 stdout）：
    成功: {"code": 0, "text": "识别出的文字", "time_ms": 123, "image_path": "...", "language": "ch"}
    失败: {"code": -1, "msg": "错误信息", "time_ms": 0}

PaddleOCR 3.x 适配要点：
    1. 构造参数变更：use_angle_cls → use_textline_orientation, use_gpu/show_log 移除
    2. ocr() 方法不再接受 cls 参数
    3. 返回值为 OCRResult 对象，文字在 result.json['res']['rec_texts'] 中
    4. 模型缓存路径必须避免含中文（C++ 推理引擎无法处理 Unicode 路径），
       通过设置 PADDLE_PDX_CACHE_HOME 环境变量解决
"""
import sys
import json
import time
import os
import warnings

# === 关键：设置模型缓存路径到无中文字符的目录 ===
# PaddlePaddle C++ 推理引擎无法打开含 Unicode 路径的模型文件
# 必须在 import paddleocr 之前设置此环境变量
_cache_home = os.environ.get("PADDLE_PDX_CACHE_HOME", "D:/paddlex_cache")
os.environ["PADDLE_PDX_CACHE_HOME"] = _cache_home

# 抑制废弃警告
warnings.filterwarnings("ignore", category=DeprecationWarning)


def get_ocr(lang="ch"):
    """懒加载 PaddleOCR 实例，按语言缓存"""
    if not hasattr(get_ocr, "_ocr_cache"):
        get_ocr._ocr_cache = {}

    if lang not in get_ocr._ocr_cache:
        from paddleocr import PaddleOCR

        # PaddleOCR 3.x 构造参数：
        #   use_doc_orientation_classify=False  关闭文档方向分类（非必需模型）
        #   use_doc_unwarping=False            关闭文档去弯曲（非必需模型）
        #   use_textline_orientation=False     关闭文字行方向（减少模型依赖）
        #   lang='ch'                           中文识别
        ocr = PaddleOCR(
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False,
            lang=lang,
        )
        get_ocr._ocr_cache[lang] = ocr

    return get_ocr._ocr_cache[lang]


def recognize(image_path, lang="ch"):
    """
    识别图片文字

    :param image_path: 图片文件路径
    :param lang:        语言（ch/en/japan/korean）
    :return: (code, text, time_ms)  code=0 成功, code=-1 失败(text 存错误信息)
    """
    if not os.path.exists(image_path):
        return -1, f"文件不存在：{image_path}", 0

    start = time.time()
    try:
        ocr = get_ocr(lang)

        # PaddlePaddle C++ 引擎会在 stdout (fd 1) 输出诊断信息（如 ReduceMeanCheckIfOneDNNSupport）
        # 这会干扰 Java 端的 JSON 解析，因此临时将 fd 1 重定向到 stderr
        import os as _os
        _stdout_fd = _os.dup(1)           # 保存原始 stdout fd
        _os.dup2(_os.dup(2), 1)            # 将 fd 1 重定向到 fd 2 (stderr) 的副本
        try:
            # PaddleOCR 3.x: ocr() 不再接受 cls 参数
            result = ocr.ocr(image_path)
        finally:
            _os.dup2(_stdout_fd, 1)        # 恢复 stdout
            _os.close(_stdout_fd)

        elapsed = int((time.time() - start) * 1000)

        if not result or len(result) == 0:
            return 0, "", elapsed

        # PaddleOCR 3.x 返回 OCRResult 对象列表
        ocr_result = result[0]

        # 方式1：通过 json 属性获取结构化数据（推荐）
        if hasattr(ocr_result, "json"):
            res = ocr_result.json.get("res", {})
            rec_texts = res.get("rec_texts", [])
            full_text = "\n".join(rec_texts)
            return 0, full_text, elapsed

        # 方式2：向后兼容 2.x 格式（list of [box, (text, conf)]）
        if isinstance(ocr_result, list):
            lines = []
            for line in ocr_result:
                if isinstance(line, (list, tuple)) and len(line) >= 2:
                    td = line[1]
                    if isinstance(td, (list, tuple)):
                        lines.append(td[0])
                    elif isinstance(td, str):
                        lines.append(td)
            full_text = "\n".join(lines)
            return 0, full_text, elapsed

        # 未知格式，尝试 dict
        if isinstance(ocr_result, dict):
            rec_texts = ocr_result.get("rec_texts", [])
            full_text = "\n".join(rec_texts) if rec_texts else str(ocr_result)
            return 0, full_text, elapsed

        return 0, "", elapsed

    except Exception as e:
        elapsed = int((time.time() - start) * 1000)
        return -1, str(e), elapsed


def main():
    # === 强制 stdout/stderr 使用 UTF-8 编码（防止 Windows 子进程继承 GBK 编码）===
    # 即使 Java 端设置了 PYTHONIOENCODING，这里再加一道保险
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

    if len(sys.argv) < 2:
        print(json.dumps(
            {"code": -1, "msg": "用法：python ocr_paddle.py <图片路径> [语言]", "time_ms": 0},
            ensure_ascii=False,
        ))
        sys.exit(1)

    image_path = sys.argv[1]
    lang = sys.argv[2] if len(sys.argv) > 2 else "ch"

    code, text, time_ms = recognize(image_path, lang)

    output = {
        "code": code,
        "text": text if code == 0 else "",
        "time_ms": time_ms,
        "image_path": image_path,
        "language": lang,
    }

    if code != 0:
        output["msg"] = text  # 失败时 text 字段存放错误信息

    # ensure_ascii=False 保证中文正常输出
    print(json.dumps(output, ensure_ascii=False))
    sys.exit(0 if code == 0 else 1)


if __name__ == "__main__":
    main()
