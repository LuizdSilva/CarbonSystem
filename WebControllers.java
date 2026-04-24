package com.carbontreesystem.controller;  // CORRIGIDO: era com.carbontreesystemsystem.controller (typo duplo)

import com.carbontreesystem.dto.DashboardKpiDto;          // CORRIGIDO: era com.carbontreesystem.system.dto.*
import com.carbontreesystem.repository.SensorReadingRepository;  // CORRIGIDO: era com.carbontreesystem.system.repository.*
import com.carbontreesystem.repository.StationRepository;
import com.carbontreesystem.service.AlertService;         // CORRIGIDO: era com.carbontreesystem.system.service.*
import com.carbontreesystem.service.DashboardService;
import com.carbontreesystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ============================================================
// LOGIN E ROOT
// ============================================================
@Controller
@Slf4j
class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}

// ============================================================
// DASHBOARD
// ============================================================
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model) {
        DashboardKpiDto kpis = dashboardService.getKpis();
        model.addAttribute("kpis", kpis);
        model.addAttribute("pageTitle", "Dashboard CarbonTree");
        return "dashboard";
    }
}

// ============================================================
// ESTAÇÕES
// ============================================================
@Controller
@RequestMapping("/stations")
@RequiredArgsConstructor
class StationsWebController {   // CORRIGIDO: renomeado para evitar conflito com StationController.java

    private final StationRepository stationRepo;
    private final SensorReadingRepository readingRepo;

    @GetMapping
    public String listStations(Model model) {
        model.addAttribute("stations", stationRepo.findAll());
        model.addAttribute("pageTitle", "Estações de Monitoramento");
        return "stations";
    }

    @GetMapping("/new")
    public String newStation(Model model) {
        model.addAttribute("station", new com.carbontreesystem.model.Station());
        model.addAttribute("pageTitle", "Nova Estação");
        return "station-form";
    }

    @GetMapping("/edit/{id}")
    public String editStation(@PathVariable Long id, Model model) {
        stationRepo.findById(id).ifPresent(s -> model.addAttribute("station", s));
        model.addAttribute("pageTitle", "Editar Estação");
        return "station-form";
    }

    @PostMapping("/save")
    public String saveStation(@ModelAttribute com.carbontreesystem.model.Station station) {
        stationRepo.save(station);
        return "redirect:/stations";
    }

    @PostMapping("/delete/{id}")
    public String deleteStation(@PathVariable Long id) {
        stationRepo.deleteById(id);
        return "redirect:/stations";
    }

    @GetMapping("/{id}")
    public String stationDetail(@PathVariable Long id, Model model) {
        stationRepo.findById(id).ifPresent(s -> {
            model.addAttribute("station", s);
            model.addAttribute("readings",
                    readingRepo.findByStationIdOrderByRecordedAtDesc(id));
        });
        model.addAttribute("pageTitle", "Detalhe da Estação");
        return "station-detail";
    }
}

// ============================================================
// ALERTAS
// ============================================================
@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
class AlertController {

    private final AlertService alertService;

    @GetMapping
    public String listAlerts(Model model) {
        model.addAttribute("alerts", alertService.getActiveAlerts());
        model.addAttribute("pageTitle", "Alertas Críticos");
        return "alerts";
    }
}

// ============================================================
// RELATÓRIOS
// ============================================================
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String reportsPage(Model model) {
        model.addAttribute("pageTitle", "Relatórios de Auditoria");
        return "reports";
    }

    @GetMapping("/audit/download")
    public ResponseEntity<byte[]> downloadAuditReport(
            @RequestParam(defaultValue = "30") int days) {
        try {
            byte[] report = reportService.generateAuditReport(days);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=auditoria.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(report);
        } catch (Exception e) {
            log.error("Erro ao gerar relatório: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

// ============================================================
// CALCULADORA FDA
// ============================================================
@Controller
@RequestMapping("/fda")
class FdaCalculatorController {

    @GetMapping
    public String fdaPage(org.springframework.ui.Model model) {
        model.addAttribute("pageTitle", "Calculadora FDA");
        return "fda-calculator";
    }
}
