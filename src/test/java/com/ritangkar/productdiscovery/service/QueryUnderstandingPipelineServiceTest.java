package com.ritangkar.productdiscovery.service;

import com.ritangkar.productdiscovery.model.NaturalLanguageQuery;
import com.ritangkar.productdiscovery.model.QueryUnderstandingResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every expected extraction below was independently verified with a
 * Python reference implementation of the same dictionaries and regexes
 * before this test was written — see docs/design-decisions.md.
 */
class QueryUnderstandingPipelineServiceTest {

    private final IntentExtractionService intentExtractionService = new IntentExtractionService();
    private final QueryValidationService queryValidationService = new QueryValidationService();
    private final BrandRegistry brandRegistry = new BrandRegistry();
    private final QueryUnderstandingPipelineService pipeline = new QueryUnderstandingPipelineService(
            intentExtractionService, queryValidationService, brandRegistry);

    @Test
    void parsesTheMasterBriefsCanonicalExampleCorrectly() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery(
                "black running shoes under 8000 for long distance"));

        var sq = result.structuredQuery();
        assertThat(sq.category()).isEqualTo("Footwear");
        assertThat(sq.color()).isEqualTo("Black");
        assertThat(sq.maxPrice()).isEqualTo(8000);
        assertThat(sq.minPrice()).isNull();
        assertThat(sq.useCase()).isEqualTo("long-distance running");
        assertThat(result.valid()).isTrue();
    }

    @Test
    void parsesAPriceRangeQuery() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery(
                "red hiking boots between 3000 and 6000"));

        var sq = result.structuredQuery();
        assertThat(sq.category()).isEqualTo("Footwear");
        assertThat(sq.color()).isEqualTo("Red");
        assertThat(sq.minPrice()).isEqualTo(3000);
        assertThat(sq.maxPrice()).isEqualTo(6000);
        assertThat(sq.useCase()).isEqualTo("hiking");
    }

    @Test
    void parsesShorthandKNotation() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery("electronics under 5k"));

        assertThat(result.structuredQuery().category()).isEqualTo("Electronics");
        assertThat(result.structuredQuery().maxPrice()).isEqualTo(5000);
    }

    @Test
    void extractsAKnownBrandWhenMentioned() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery("AeroFit jacket for training under 4000"));

        var sq = result.structuredQuery();
        assertThat(sq.brand()).isEqualTo("AeroFit");
        assertThat(sq.category()).isEqualTo("Apparel");
        assertThat(sq.useCase()).isEqualTo("training");
        assertThat(sq.maxPrice()).isEqualTo(4000);
    }

    @Test
    void everyNonNullFieldHasATraceableExtractionReason() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery(
                "black running shoes under 8000 for long distance"));

        // category, color, maxPrice and useCase were all extracted -> 4 traces expected.
        assertThat(result.structuredQuery().extractedFrom()).hasSize(4);
    }

    @Test
    void aQueryWithNoExtractableFieldsIsStillValidButFlaggedWithAWarning() {
        QueryUnderstandingResult result = pipeline.run(new NaturalLanguageQuery("something completely unrelated"));

        assertThat(result.valid()).isTrue();
        assertThat(result.validationIssues()).anyMatch(i -> i.severity().equals("WARNING"));
    }

    @Test
    void aBackwardsPriceRangeIsFlaggedAsInvalid() {
        // Directly construct a pipeline scenario the extractor itself would never produce,
        // to prove the validator independently catches an inconsistent structured query.
        var structured = new com.ritangkar.productdiscovery.model.StructuredQuery(
                "test", null, null, null, 9000, 3000, null, java.util.List.of());
        var issues = queryValidationService.validate(structured);

        assertThat(queryValidationService.isValid(issues)).isFalse();
        assertThat(issues).anyMatch(i -> i.field().equals("priceRange") && i.severity().equals("ERROR"));
    }
}
