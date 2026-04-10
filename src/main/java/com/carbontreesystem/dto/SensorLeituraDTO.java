package com.carbontreesystem.dto;

import java.math.BigDecimal;

public class SensorLeituraDTO {
    private String sensorId;
    private BigDecimal valorCo2;

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public BigDecimal getValorCo2() { return valorCo2; }
    public void setValorCo2(BigDecimal valorCo2) { this.valorCo2 = valorCo2; }
}