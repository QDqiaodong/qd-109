package com.digital.community.vo;

import lombok.Data;
import java.util.List;

@Data
public class FaultThemeSuggestionVO {
    private String inputModel;
    private List<ModelFaultStatsVO> matchedModels;
    private List<FaultThemeVO> commonThemes;
    private String troubleshootingTips;
}
