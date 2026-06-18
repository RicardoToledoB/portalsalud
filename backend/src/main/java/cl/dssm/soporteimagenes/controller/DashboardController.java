package cl.dssm.soporteimagenes.controller;

import cl.dssm.soporteimagenes.dto.DashboardSummaryDto;
import cl.dssm.soporteimagenes.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDto summary(@RequestParam(required = false) Long portalId) {
        return dashboardService.summary(portalId);
    }
}
