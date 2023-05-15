package com.zj.client.feature.executor.feature;

import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecuteResult;
import com.zj.client.feature.executor.vo.FeatureParam;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IFeatureExecutor {

    void execute(FeatureParam featureParam);
}
