package com.digital.community.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long parentId;
    private Long rootId;
    private Long replyUserId;
    private String replyNickname;
    private String replyContent;
    private Integer floor;
    private String content;
    private Integer depth;
    private Integer isCollapsed;
    private LocalDateTime createTime;
    private List<CommentVO> children;
}
