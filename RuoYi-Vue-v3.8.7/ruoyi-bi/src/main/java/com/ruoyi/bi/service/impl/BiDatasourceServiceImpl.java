package com.ruoyi.bi.service.impl;

import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.mapper.BiDatasourceMapper;
import com.ruoyi.bi.service.IBiDatasourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BI数据源配置表 Service实现类
 * 
 * @author ruoyi-bi
 */
@Service
public class BiDatasourceServiceImpl implements IBiDatasourceService {

    private static final Logger log = LoggerFactory.getLogger(BiDatasourceServiceImpl.class);

    @Autowired
    private BiDatasourceMapper datasourceMapper;

    /**
     * 查询数据源列表
     */
    @Override
    public List<BiDatasource> selectBiDatasourceList(BiDatasource datasource) {
        return datasourceMapper.selectBiDatasourceList(datasource);
    }

    /**
     * 查询数据源 by ID
     */
    @Override
    public BiDatasource selectBiDatasourceById(Long id) {
        return datasourceMapper.selectBiDatasourceById(id);
    }

    /**
     * 新增数据源
     */
    @Override
    public int insertBiDatasource(BiDatasource datasource) {
        // TODO: 加密密码
        // TODO: 生成JDBC URL
        return datasourceMapper.insertBiDatasource(datasource);
    }

    /**
     * 修改数据源
     */
    @Override
    public int updateBiDatasource(BiDatasource datasource) {
        // TODO: 如果密码有变化，重新加密
        return datasourceMapper.updateBiDatasource(datasource);
    }

    /**
     * 删除数据源 by ID
     */
    @Override
    public int deleteBiDatasourceById(Long id) {
        return datasourceMapper.deleteBiDatasourceById(id);
    }

    @Override
    public int deleteBiDatasourceByIds(Long[] ids) {
        return datasourceMapper.deleteBiDatasourceByIds(ids);
    }

    /**
     * 测试数据源连接
     */
    @Override
    public boolean testConnection(BiDatasource datasource) {
        // TODO: 实现连接测试逻辑
        // 1. 根据type构建JDBC URL
        // 2. 尝试建立连接
        // 3. 返回结果
        log.info("测试数据源连接：{}", datasource.getName());
        return true; // 暂时返回true
    }
}
