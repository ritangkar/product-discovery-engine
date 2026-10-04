package com.ritangkar.productdiscovery.service;

import com.ritangkar.productdiscovery.model.NaturalLanguageQuery;
import com.ritangkar.productdiscovery.model.QueryUnderstandingResult;
import com.ritangkar.productdiscovery.model.StructuredQuery;
import com.ritangkar.productdiscovery.model.ValidationIssue;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Natural Language Query -&gt; Intent Extraction -&gt; Structured Query -&gt;
 * Validation -&gt; Search Parameters.
 */
@Service
public class QueryUnderstandingPipelineService {

    private final IntentExtractionService intentExtractionService;
    private final QueryValidationService queryValidationService;
    private final BrandRegistry brandRegistry;

    public QueryUnderstandingPipelineService(IntentExtractionService intentExtractionService,
                                              QueryValidationService queryValidationService,
                                              BrandRegistry brandRegistry) {
        this.intentExtractionService = intentExtractionService;
        this.queryValidationService = queryValidationService;
        this.brandRegistry = brandRegistry;
    }

    public QueryUnderstandingResult run(NaturalLanguageQuery input) {
        StructuredQuery structured = intentExtractionService.extract(input.query(), brandRegistry.knownBrands());
        List<ValidationIssue> issues = queryValidationService.validate(structured);
        boolean valid = queryValidationService.isValid(issues);
        return new QueryUnderstandingResult(structured, issues, valid);
    }
}
