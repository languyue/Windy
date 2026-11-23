package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.CommentBO;
import java.util.List;


/**
 * @author guyuelan
 */
public interface ICommentQueryRepository {

  /**
  * 获取关联资源的评论列表
  * @param relativeId 关联ID
  * @return 评论列表
  */
  List<CommentBO> getRelativeComments(String relativeId);

}
