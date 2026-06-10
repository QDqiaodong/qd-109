package com.digital.community.vo;

import lombok.Data;
import java.util.List;

@Data
public class ImageGroupVO {
    private String key;
    private String label;
    private List<String> images;
    private Integer sort;
}
