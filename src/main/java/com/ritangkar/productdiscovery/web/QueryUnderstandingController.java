package com.ritangkar.productdiscovery.web;

import com.ritangkar.productdiscovery.model.NaturalLanguageQuery;
import com.ritangkar.productdiscovery.model.QueryUnderstandingResult;
import com.ritangkar.productdiscovery.service.QueryUnderstandingPipelineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryUnderstandingController {

    private final QueryUnderstandingPipelineService pipelineService;

    public QueryUnderstandingController(QueryUnderstandingPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * POST /api/discovery/understand — e.g.
     * {"query": "black running shoes under 8000 for long distance"}
     * returns the structured parameters that Smart Product Search Engine's
     * /api/search endpoint accepts directly.
     */
    @PostMapping("/api/discovery/understand")
    public QueryUnderstandingResult understand(@Valid @RequestBody NaturalLanguageQuery input) {
        return pipelineService.run(input);
    }
}
