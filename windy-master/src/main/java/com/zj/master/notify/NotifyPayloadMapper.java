package com.zj.master.notify;

import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.common.utils.OrikaUtil;
import com.zj.contract.event.ResultEvent;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;

/**
 * 通知载荷到领域对象的转换。
 *
 * <p>载荷是 wire 契约（{@code com.zj.contract.event.payload}），领域对象在 windy-domain，
 * 两者字段同名同型，这里集中做一次转换，事件处理类只关心业务逻辑。</p>
 *
 * @author guyuelan
 */
public final class NotifyPayloadMapper {

  private NotifyPayloadMapper() {
  }

  public static NodeRecordBO toNodeRecordBO(ResultEvent event) {
    return OrikaUtil.convert(event.getParams(), NodeRecordBO.class);
  }

  public static ExecuteRecordBO toExecuteRecordBO(ResultEvent event) {
    return OrikaUtil.convert(event.getParams(), ExecuteRecordBO.class);
  }

  public static FeatureHistoryBO toFeatureHistoryBO(ResultEvent event) {
    return OrikaUtil.convert(event.getParams(), FeatureHistoryBO.class);
  }

  public static GenerateRecordBO toGenerateRecordBO(ResultEvent event) {
    return OrikaUtil.convert(event.getParams(), GenerateRecordBO.class);
  }
}
