package com.ritangkar.productdiscovery.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The brand vocabulary brand extraction matches against — the same 17
 * brands used in the Smart Product Search Engine project's catalog, so a
 * query parsed here produces a {@code brand} value that catalog can
 * actually filter on.
 */
@Component
public class BrandRegistry {

    private static final List<String> KNOWN_BRANDS = List.of(
            "AeroFit", "BaseCamp", "CoreFit", "FlexTech", "GripFit", "HydroFlow", "NorthPeak",
            "PacePro", "RapidTread", "SolarCap", "SprintFlex", "StrideMax", "ThermoLayer",
            "TrailBlazer", "TrailPack", "TrailRunner", "ZenithWear"
    );

    public List<String> knownBrands() {
        return KNOWN_BRANDS;
    }
}
