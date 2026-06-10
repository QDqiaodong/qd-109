package com.digital.community.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostDTO {
    private Long categoryId;
    private String title;
    private String content;
    private List<String> images;
    private List<AccessoryCardDTO> accessoryCards;
    private Integer type;
}
