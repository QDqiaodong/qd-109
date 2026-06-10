package com.digital.community.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImageGroupDTO {
    private String key;
    private String label;
    private List<String> images;
    private Integer sort;
}
