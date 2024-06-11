package com.altinntech.clicksave.metrics;

import com.altinntech.clicksave.metrics.dto.base.MetricsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final MonitoringService monitoringService;

    @Autowired
    public WebController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/clicksave")
    public String getMonitoringData(Model model) {
        MetricsLog metricsLog = monitoringService.getMetricsLog();
        model.addAttribute("metricsLog", metricsLog);
        return "monitoring";
    }

    @GetMapping("/clicksave/metrics")
    @ResponseBody
    public MetricsLog getMetricsLog() {
        return monitoringService.getMetricsLog();
    }
}
