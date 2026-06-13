package com.digital.community.vo;

import lombok.Data;

@Data
public class FaultThemeVO {
    private String themeKey;
    private String themeName;
    private String themeIcon;
    private Integer count;
    private Double percentage;
    private String sampleSymptoms;
}
