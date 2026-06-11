package com.digital.community.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PostDTO {
    private Long categoryId;
    private String title;
    private String content;
    private List<String> images;
    private List<ImageGroupDTO> imageGroups;
    private List<AccessoryCardDTO> accessoryCards;
    private Map<String, String> faultInfo;
    private Integer type;
}
