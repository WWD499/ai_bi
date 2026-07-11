package com.ruoyi.common.ocr.vo;

/**
 * OCR 识别结果封装
 * <p>原作为 {@code OcrService} 的静态内部类，按代码规范 P2-2 抽到独立 vo 包，
 * 与项目其余实体保持一致的手写 getter/setter 风格（项目未引入 Lombok）。
 */
public class OcrResult {
    private String text;
    private int pythonTimeMs;
    private int totalTimeMs;
    private String imagePath;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getPythonTimeMs() { return pythonTimeMs; }
    public void setPythonTimeMs(int pythonTimeMs) { this.pythonTimeMs = pythonTimeMs; }

    public int getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(int totalTimeMs) { this.totalTimeMs = totalTimeMs; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
