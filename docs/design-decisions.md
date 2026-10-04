# Design Decisions

---

**Decision:** Dictionary- and regex-based extraction by default, not an
LLM call — despite the master brief allowing "structured LLM output where
appropriate."

**Why:** For a bounded vocabulary (5 categories, 11 colors, 17 brands, 8
use-case phrases, a handful of price-phrasing patterns), a deterministic
matcher is faster, free, fully explainable per field (`extractedFrom`),
and testable without a network call or API key. An LLM earns its cost
when the vocabulary is open-ended or the phrasing is too varied for
patterns to cover — that's not this problem, at this project's scope.

**Trade-off:** A genuinely novel phrasing the dictionaries don't cover
(a category described only by a brand-new slang term, say) won't be
extracted. The query still isn't discarded — `QueryValidationService`
flags it as a free-text-only fallback rather than failing.

**Future:** An LLM-backed extractor could be added behind the same
`IntentExtractionService.extract(...)` method signature — the rest of the
pipeline (`StructuredQuery`, validation) wouldn't need to change, the same
seam pattern used in this collection's Personal Research Agent project.

---

**Decision:** `extractedFrom` records a human-readable trace for every
extracted field, not just the final structured values.

**Why:** "The system understood your query" means little without showing
*what* it matched on. A recruiter (or a real user debugging an
unexpected search result) can see exactly which words in their query
produced which field — this is the difference between a black box and an
inspectable pipeline.

**Trade-off:** None significant — a small amount of extra bookkeeping in
`IntentExtractionService`.

---

**Decision:** The brand vocabulary (`BrandRegistry`) is the exact same 17
brands as the Smart Product Search Engine project's catalog.

**Why:** These two projects are explicitly designed to compose — this
project's structured output is that project's search input. Sharing the
brand vocabulary means a query like "AeroFit jacket under 4000," parsed
here, produces a `brand` value that catalog can actually filter on,
demonstrating the composition isn't just a documentation claim.
