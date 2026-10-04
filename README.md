# AI Product Discovery & Query Understanding Engine

Converts a natural-language product query into structured search
parameters: **Natural Language Query → Intent Extraction → Structured
Query → Validation → Search Parameters.** Deliberately distinct from the
Smart Product Search Engine project — that one ranks results; this one
produces the parameters it ranks against.

Part of the [Commerce Engineering Lab](https://ritangkar.github.io/#commerce-lab) —
17 small, production-minded engineering capabilities by Ritangkar Dey.

---

## 1. Project Overview

Send `"black running shoes under 8000 for long distance"`; get back
`{category: "Footwear", color: "Black", maxPrice: 8000, useCase:
"long-distance running"}` — the master brief's own canonical example,
parsed exactly.

## 2. Problem

A search box takes free text; a search *engine* needs structured filters.
Something has to bridge the two — and that bridge needs to be honest about
what it did and didn't understand, not silently guess.

## 3. Solution

Independent dictionary/regex extractors for category, color, brand, price
range and use case, each only populating its field when the query
actually contains evidence for it, each recording exactly what triggered it.

## 4. Key Features

- Handles the master brief's exact example query correctly, verified in a test
- Price parsing: "under X", "below X", "between X and Y", "over X",
  shorthand "8k" notation, comma-formatted numbers
- Shared 17-brand vocabulary with the Smart Product Search Engine
  project's catalog — the two projects compose directly
- `extractedFrom`: a traceable reason for every non-null field
- Never guesses — an unmatched field stays null; a query with nothing
  extractable is flagged, not silently misinterpreted

## 5. Architecture

```
Natural Language Query -> Intent Extraction (category, color, brand,
  price range, use case -- each independent) -> Structured Query ->
  Validation -> Search Parameters
```

See [`docs/architecture.md`](docs/architecture.md) for the full diagram.

## 6. Technical Approach

Deterministic by default — see [`docs/design-decisions.md`](docs/design-decisions.md)
for why an LLM wasn't the default choice here, and exactly where one
would plug in if a future batch added it.

## 7. Design Decisions

See [`docs/design-decisions.md`](docs/design-decisions.md).

## 8. Sample Input

```json
POST /api/discovery/understand
{ "query": "black running shoes under 8000 for long distance" }
```

## 9. Sample Output

Real, verified output for the input above:

```json
{
  "structuredQuery": {
    "rawQuery": "black running shoes under 8000 for long distance",
    "category": "Footwear",
    "color": "Black",
    "brand": null,
    "minPrice": null,
    "maxPrice": 8000,
    "useCase": "long-distance running",
    "extractedFrom": [
      "category <- matched keyword in query",
      "color <- \"black\"",
      "maxPrice <- \"under 8000\"",
      "useCase <- matched phrase in query"
    ]
  },
  "validationIssues": [],
  "valid": true
}
```

## 10. How to Run

```bash
git clone <your-repo-url>
cd product-discovery-engine
mvn spring-boot:run
```

API available at `http://localhost:8093`.

## 11. How to Test

```bash
mvn test
```

Covers the master brief's canonical example, a price-range query,
shorthand "5k" notation, brand extraction, the traceability guarantee, the
no-match fallback, and a validator-level backwards-price-range check.

## 12. API Documentation

| Method | Path | Description |
|---|---|---|
| POST | `/api/discovery/understand` | `{"query": "..."}` returns structured parameters |

## 13. Portfolio Demo

The Commerce Engineering Lab demo (`lab/demos/product-discovery-engine.js`)
ports the full extraction pipeline to client-side JavaScript. See
[`docs/demo.md`](docs/demo.md).

## 14. Limitations

- A fixed, hand-built vocabulary (5 categories, 11 colors, 17 brands, 8
  use-case phrases) — genuinely novel phrasing outside it won't be extracted
- English only
- No spelling-correction/fuzzy matching on category or color terms

## 15. Future Enhancements

- An optional LLM-backed extractor behind the same method signature (see
  design decisions for the seam)
- Fuzzy matching for minor misspellings
- Multi-value extraction (e.g. "black or white shoes")

## 16. License

MIT — see [`LICENSE`](LICENSE).
