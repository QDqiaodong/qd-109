package com.digital.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private Long rootId;
    private Long replyUserId;
    private String content;
    private Integer depth;
    private Integer isCollapsed;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
