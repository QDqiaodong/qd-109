package com.digital.community.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccessoryCardDTO {
    private String category;
    private String role;
    private String model;
    private String interfaceType;
    private List<String> compatibleDevices;
    private List<String> usageScenarios;
    private List<String> pros;
    private List<String> cons;
}
