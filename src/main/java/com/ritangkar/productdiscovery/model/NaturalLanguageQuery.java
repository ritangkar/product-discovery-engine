package com.ritangkar.productdiscovery.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NaturalLanguageQuery(
        @NotBlank @Size(min = 3, max = 300) String query
) {
}
