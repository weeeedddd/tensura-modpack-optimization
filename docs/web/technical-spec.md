# Technical Spec — Tensura Abyss

## Stack
Tier A: one file, semantic HTML, tokenized CSS, and 2.2 KB-class vanilla animation logic. No packages, network requests, WebGL, external assets, or build step. The page is `file://` safe.

## Architecture
- Hero: sticky stage with six decorative depth layers.
- Evolution: sticky desktop rail mapped to nine native race stages.
- World/sites/gear: content-first free-scroll chapters.
- Runtime: passive scroll listener only schedules rAF; rAF reads `scrollY` and writes transforms. Layout measurements occur only at initialization/resize.
- Reveals: one shared `IntersectionObserver`, one-shot.

## Budgets
Desktop compositor target: six hero layers plus active content. Mobile target: one parallax mover. Zero images, fonts, third-party scripts, autoplay media, or per-frame layout reads.

## Degradation
Coarse pointers unpin chapters through CSS and skip desktop depth/timeline writes. Reduced motion disables sticky narrative, transitions, parallax, and hidden initial states.

## Risks
Very old browsers without `clip-path` see simpler geometric silhouettes but retain all text. JavaScript failure leaves the semantic content readable; mobile cards are CSS-visible.
