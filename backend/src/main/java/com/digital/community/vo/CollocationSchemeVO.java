package com.digital.community.vo;

import lombok.Data;
import java.util.List;

@Data
public class CollocationSchemeVO {
    private String schemeKey;
    private List<String> accessoryModels;
    private Integer itemCount;
    private Integer postCount;
    private Long categoryId;
    private String categoryName;
    private Double percentage;
    private List<PostVO> relatedPosts;
    private Integer totalViews;
    private Integer totalComments;
}
