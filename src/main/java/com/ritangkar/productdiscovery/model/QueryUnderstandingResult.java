package com.ritangkar.productdiscovery.model;

import java.util.List;

public record QueryUnderstandingResult(
        StructuredQuery structuredQuery,
        List<ValidationIssue> validationIssues,
        boolean valid
) {
}
