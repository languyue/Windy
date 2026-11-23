package com.zj.domain.repository.feature;

import com.zj.contract.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import java.util.List;

/**
* @author guyuelan
* @since 2023/5/17
*/
public interface IFeatureHistoryQueryRepository {

  /**
  * 根据历史ID获取用例历史记录
  * @param historyId 历史ID
  * @return 用例历史
  */
  FeatureHistoryBO getFeatureHistory(String historyId);

  /**
  * 获取用例历史记录列表
  * @param featureId 用例ID
  * @return 用例历史列表
  */
  List<FeatureHistoryBO> featureHistories(String featureId);

  /**
  * 查询任务的用例历史记录
  * @param taskRecordId 任务ID
  * @return 是否成功
  */
  List<FeatureHistoryBO> getHistoriesByTaskRecordId(String taskRecordId);

  /**
  * 获取指定时间之前的用例历史记录
  * @param queryTime 查询时间
  * @return 历史记录
  */
  List<FeatureHistoryBO> getOldFeatureHistory(long queryTime);

}
