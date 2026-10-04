package com.ritangkar.productdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Product Discovery & Query Understanding Engine.
 *
 * Natural Language Query -> Intent Extraction -> Structured Query ->
 * Validation -> Search Parameters.
 *
 * Deliberately distinct from the Smart Product Search Engine project:
 * that project does retrieval and BM25 ranking over already-structured
 * parameters; this project's whole job is producing those parameters
 * from a free-text query in the first place. The two compose — this
 * project's output is exactly the shape Smart Product Search's
 * {@code /api/search} endpoint accepts.
 */
@SpringBootApplication
public class ProductDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductDiscoveryApplication.class, args);
    }
}
