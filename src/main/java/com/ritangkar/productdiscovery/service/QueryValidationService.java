package com.ritangkar.productdiscovery.service;

import com.ritangkar.productdiscovery.model.StructuredQuery;
import com.ritangkar.productdiscovery.model.ValidationIssue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 3: Validation. Deterministic sanity checks on whatever Stage 2
 * extracted — never re-derives or second-guesses the extraction itself,
 * only checks internal consistency (e.g. a price range that's backwards)
 * and flags when nothing useful was extracted at all.
 */
@Service
public class QueryValidationService {

    public List<ValidationIssue> validate(StructuredQuery query) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (query.minPrice() != null && query.maxPrice() != null && query.minPrice() > query.maxPrice()) {
            issues.add(new ValidationIssue("priceRange", "ERROR",
                    "minPrice (" + query.minPrice() + ") is greater than maxPrice (" + query.maxPrice() + ")."));
        }

        if (query.minPrice() != null && query.minPrice() < 0) {
            issues.add(new ValidationIssue("minPrice", "ERROR", "minPrice cannot be negative."));
        }
        if (query.maxPrice() != null && query.maxPrice() < 0) {
            issues.add(new ValidationIssue("maxPrice", "ERROR", "maxPrice cannot be negative."));
        }

        boolean nothingExtracted = query.category() == null && query.color() == null && query.brand() == null
                && query.minPrice() == null && query.maxPrice() == null && query.useCase() == null;
        if (nothingExtracted) {
            issues.add(new ValidationIssue("query", "WARNING",
                    "No structured field could be extracted from this query \u2014 it will be treated as a free-text search only."));
        }

        return issues;
    }

    public boolean isValid(List<ValidationIssue> issues) {
        return issues.stream().noneMatch(i -> i.severity().equals("ERROR"));
    }
}
