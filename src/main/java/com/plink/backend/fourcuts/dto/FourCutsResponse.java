package com.plink.backend.fourcuts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FourCutsResponse {
    private String imageUrl;
    private String qrUrl;
}
