package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.CommentBO;
import java.util.List;
import com.zj.domain.repository.marker.ConfigCommandRepository;


/**
 * @author guyuelan
 */
public interface ICommentCommandRepository extends ICommentQueryRepository, ConfigCommandRepository {

  /**
  * 保存评论
  * @param commentBO 评论信息
  * @return 是否成功
  */
  boolean saveComment(CommentBO commentBO);

  /**
  * 更新评论
  * @param commentBO 评论信息
  * @return 是否成功
  */
  boolean updateComment(CommentBO commentBO);

  /**
  * 删除评论
  * @param commentId 评论ID
  * @return 是否成功
  */
  boolean deleteComment(String commentId);

  /**
  *  根据关联资源ID删除所有评论
  * @param relativeId 关联的资源ID
  * @return 是否删除成功
  */
  boolean deleteByRelativeId(String relativeId);

}
