package com.ruoyi.bi.mapper;

import com.ruoyi.bi.domain.BiAlertRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预警记录 Mapper
 *
 * @author ruoyi-bi
 */
@Mapper
public interface BiAlertRecordMapper {

    List<BiAlertRecord> selectBiAlertRecordList(BiAlertRecord record);

    BiAlertRecord selectBiAlertRecordById(Long id);

    int insertBiAlertRecord(BiAlertRecord record);

    int updateBiAlertRecord(BiAlertRecord record);

    int deleteBiAlertRecordById(Long id);

    int deleteBiAlertRecordByIds(Long[] ids);

    /** 统计某规则今天的预警次数（防止重复预警） */
    int countTodayByRuleId(Long ruleId);

    /** 统计某规则未处理(pending)的预警数量，用于重复预警抑制 */
    int countPendingByRuleId(Long ruleId);
}
