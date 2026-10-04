package com.ritangkar.productdiscovery.model;

import java.util.List;

/**
 * The structured search parameters extracted from a free-text query.
 * Any field the query didn't mention stays null — this project never
 * guesses a value it didn't find evidence for. {@code extractedFrom}
 * names, for each non-null field, the literal phrase in the query that
 * produced it, so every extraction is traceable.
 */
public record StructuredQuery(
        String rawQuery,
        String category,
        String color,
        String brand,
        Integer minPrice,
        Integer maxPrice,
        String useCase,
        List<String> extractedFrom
) {
}
