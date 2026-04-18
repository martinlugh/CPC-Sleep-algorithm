package com.sleep.platform.domain.model;

import lombok.Data;

@Data
public class CpcAnalysisResult {

    private double hfcPower;
    private double lfcPower;
    private double vlfcPower;
    private double totalPower;
    private double hfcLfcRatio;
    private double cpcCouplingScore;
    private String cpcState;
}
