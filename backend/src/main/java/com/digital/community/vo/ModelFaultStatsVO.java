package com.digital.community.vo;

import lombok.Data;
import java.util.List;

@Data
public class ModelFaultStatsVO {
    private String model;
    private String modelType;
    private Long categoryId;
    private String categoryName;
    private Integer totalHelpPosts;
    private Integer totalComments;
    private Integer totalViews;
    private List<FaultThemeVO> topThemes;
    private List<PostVO> relatedPosts;
}
