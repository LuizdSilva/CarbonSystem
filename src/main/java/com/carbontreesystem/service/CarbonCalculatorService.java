package com.carbontreesystem.service;

import com.carbontreesystem.dto.HistoricoCarbonoDTO;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarbonCalculatorService {

    private final BigDecimal COTA_MENSAL = new BigDecimal("500.00"); // Exemplo: 500kg de CO2

    public List<HistoricoCarbonoDTO> gerarDadosDashboard() {
        // No futuro, isso virá do Banco com SQL (GROUP BY week)
        return List.of(
                new HistoricoCarbonoDTO(LocalDateTime.now().minusWeeks(3), new BigDecimal("120.5")),
                new HistoricoCarbonoDTO(LocalDateTime.now().minusWeeks(2), new BigDecimal("145.2")),
                new HistoricoCarbonoDTO(LocalDateTime.now().minusWeeks(1), new BigDecimal("98.8")),
                new HistoricoCarbonoDTO(LocalDateTime.now(), new BigDecimal("110.0"))
        );
    }

    public BigDecimal calcularMediaSemanal(List<HistoricoCarbonoDTO> dados) {
        if (dados.isEmpty()) return BigDecimal.ZERO;
        BigDecimal soma = dados.stream().map(HistoricoCarbonoDTO::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(new BigDecimal(dados.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal quantoFaltaParaCota(BigDecimal totalEmitido) {
        return COTA_MENSAL.subtract(totalEmitido).max(BigDecimal.ZERO);
    }
}