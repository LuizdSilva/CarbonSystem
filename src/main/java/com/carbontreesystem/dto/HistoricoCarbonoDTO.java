package com.carbontreesystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistoricoCarbonoDTO {
    private LocalDateTime data;
    private BigDecimal valor;

    public HistoricoCarbonoDTO(LocalDateTime data, BigDecimal valor) {
        this.data = data;
        this.valor = valor;
    }
    public LocalDateTime getData() {
        return data;
    }
    public BigDecimal getValor() {
        return valor;
    }
}