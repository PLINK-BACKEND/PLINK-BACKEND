package com.plink.backend.festival.service;

import com.plink.backend.festival.entity.Festival;
import com.plink.backend.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FestivalService {
    private final FestivalRepository festivalRepository;

    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    public List<Festival> searchFestivals(String keyword) {
        return festivalRepository.findByNameContainingIgnoreCase(keyword);
    }

}
