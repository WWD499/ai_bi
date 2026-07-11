package com.ruoyi.bi.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * RAG知识库表 bi_knowledge
 * 
 * @author ruoyi-bi
 */
public class BiKnowledge extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 知识ID */
    private Long id;

    /** 文档标题 */
    private String title;

    /** 文档内容（切片后） */
    private String content;

    /** 内容向量（BGE-M3：1024维） */
    private String contentVector;  // 存储为字符串，实际在PG中使用vector类型

    /** 来源类型：manual-手动录入、ocr-OCR识别、file-文件上传 */
    private String sourceType;

    /** 来源URL或文件路径 */
    private String sourceUrl;

    /** 业务领域（如：财务、销售、库存等） */
    private String businessDomain;

    /** 标签（逗号分隔） */
    private String tags;

    /** 切片序号（同一文档的多个切片） */
    private Integer chunkIndex;

    /** 总切片数 */
    private Integer totalChunks;

    /** 状态：0-停用，1-启用 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentVector() {
        return contentVector;
    }

    public void setContentVector(String contentVector) {
        this.contentVector = contentVector;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getBusinessDomain() {
        return businessDomain;
    }

    public void setBusinessDomain(String businessDomain) {
        this.businessDomain = businessDomain;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("title", getTitle())
                .append("sourceType", getSourceType())
                .append("businessDomain", getBusinessDomain())
                .toString();
    }
}
