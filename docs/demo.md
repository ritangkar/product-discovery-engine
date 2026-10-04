# Portfolio Demo

**Demo level:** 1 — client-side, fully deterministic.

`lab/demos/product-discovery-engine.js` ports the full dictionary/regex
extraction pipeline to client-side JavaScript. Type a query — the master
brief's own example is pre-filled — and see the structured parameters,
each with its traceable extraction reason, update live as you type. No
backend call.

Try typing something the demo's vocabulary doesn't cover to see the
honest "no structured field could be extracted" fallback, rather than a
guessed answer.
