package com.plink.backend.festival.controller;

import com.plink.backend.festival.entity.Festival;
import com.plink.backend.festival.repository.FestivalRepository;
import com.plink.backend.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plink/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @GetMapping
    public ResponseEntity<List<Festival>> getFestivals(
            @RequestParam(required = false) String keyword) {

        List<Festival> festivals;

        if (keyword != null && !keyword.isBlank()) {
            festivals = festivalService.searchFestivals(keyword);
        } else {
            festivals = festivalService.getAllFestivals();
        }

        return ResponseEntity.ok(festivals);
    }
}