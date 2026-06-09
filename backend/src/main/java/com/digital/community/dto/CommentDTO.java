package com.digital.community.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long postId;
    private Long parentId;
    private Long replyUserId;
    private String content;
}
