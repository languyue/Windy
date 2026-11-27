package com.zj.infrastructure.repository.demand;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.CommentBO;
import com.zj.infrastructure.entity.po.demand.Comment;
import com.zj.infrastructure.mapper.demand.CommentMapper;
import com.zj.domain.repository.demand.ICommentCommandRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CommentRepository extends ServiceImpl<CommentMapper, Comment> implements ICommentCommandRepository {
    @Override
    public List<CommentBO> getRelativeComments(String relativeId) {
        List<Comment> comments = list(Wrappers.lambdaQuery(Comment.class).eq(Comment::getRelativeId, relativeId)
                        .orderByDesc(Comment::getCreateTime));
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(comments, CommentBO.class);
    }

    @Override
    public boolean saveComment(CommentBO commentBO) {
        Comment comment = OrikaUtil.convert(commentBO, Comment.class);
        comment.setCreateTime(System.currentTimeMillis());
        comment.setUpdateTime(System.currentTimeMillis());
        return save(comment);
    }

    @Override
    public boolean updateComment(CommentBO commentBO) {
        Comment comment = OrikaUtil.convert(commentBO, Comment.class);
        comment.setUpdateTime(System.currentTimeMillis());
        return update(comment,
                Wrappers.lambdaUpdate(Comment.class).eq(Comment::getCommentId, comment.getCommentId()));
    }

    @Override
    public boolean deleteComment(String commentId) {
        return remove(Wrappers.lambdaQuery(Comment.class).eq(Comment::getCommentId, commentId));
    }

    @Override
    public boolean deleteByRelativeId(String relativeId) {
        return remove(Wrappers.lambdaQuery(Comment.class).eq(Comment::getRelativeId, relativeId));
    }
}
