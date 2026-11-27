package com.zj.pipeline.adapter;

import com.zj.common.port.codechange.CodeChangeView;
import com.zj.common.port.codechange.ICodeChangeReader;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.repository.pipeline.ICodeChangeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码变更只读查询端口的实现。
 *
 * <p>流水线域自己读自己的代码变更表，把结果收敛成最小视图；
 * 需求域不再直接依赖流水线域的仓储与 BO。</p>
 *
 * @author guyuelan
 */
@Service
public class CodeChangeReader implements ICodeChangeReader {

  private final ICodeChangeQueryRepository codeChangeRepository;

  public CodeChangeReader(ICodeChangeQueryRepository codeChangeRepository) {
    this.codeChangeRepository = codeChangeRepository;
  }

  @Override
  public List<CodeChangeView> listByRelation(String relationId, Integer relationType) {
    return OrikaUtil.convertList(codeChangeRepository.getCodeChangeByRelationId(relationId, relationType),
        CodeChangeView.class);
  }
}
