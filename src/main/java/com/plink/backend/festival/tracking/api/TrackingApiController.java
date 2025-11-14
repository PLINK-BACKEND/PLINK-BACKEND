package com.plink.backend.festival.tracking.api;
import com.plink.backend.festival.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/plink/festival")
@RequiredArgsConstructor

public class TrackingApiController {

    private final TrackingService trackingService;

    // 전체 축제별 접속자 수
    @GetMapping("/active-users")
    public Map<String, Integer> getAllFestivalCounts() {
        return trackingService.getAllCounts();
    }

    // 특정 축제의 접속자 수
    @GetMapping("/{slug}/active-users")
    public Map<String, Integer> getFestivalCount(@PathVariable String slug) {
        int count = trackingService.getActiveCount(slug);
        return Map.of(slug, count);
    }
}
