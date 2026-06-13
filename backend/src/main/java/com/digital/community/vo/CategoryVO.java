package com.digital.community.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private Integer hotScore;
    private Integer postCount;
}
