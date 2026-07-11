package com.ruoyi.bi.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BI大屏实体
 * 映射 bi_dashboard 表
 *
 * @author ruoyi-bi
 */
public class BiDashboard extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "大屏名称")
    private String name;

    @Excel(name = "描述")
    private String description;

    /** 大屏配置JSON：包含布局（GridStack）、图表列表、数据源绑定 */
    private String configJson;

    /** 缩略图URL */
    private String thumbnail;

    /** 状态：0-停用，1-启用 */
    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    private Integer status;

    /** 是否公开：0-否，1-是 */
    private Integer isPublic;

    /** 访问令牌（公开分享用） */
    private String accessToken;

    // ==================== Getter/Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
