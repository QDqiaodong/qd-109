package com.digital.community.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private List<String> images;
    private List<AccessoryCardVO> accessoryCards;
    private Integer type;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
}
