package com.digital.community.vo;

import lombok.Data;
import java.util.List;

@Data
public class AccessoryCardVO {
    private String model;
    private String interfaceType;
    private List<String> compatibleDevices;
    private List<String> usageScenarios;
    private List<String> pros;
    private List<String> cons;
}
