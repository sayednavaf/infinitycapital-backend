package com.infinitycapital.api.advisory;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advisory")
public class AdvisoryServiceController {
    private final AdvisoryServiceRepository services;

    public AdvisoryServiceController(AdvisoryServiceRepository services) {
        this.services = services;
    }

    @GetMapping("/services")
    public List<AdvisoryService> services() {
        return services.findAllByActiveTrueOrderByIdAsc();
    }
}
