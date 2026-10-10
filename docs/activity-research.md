# Activity memory: evidence and initial calibration

Research checked 2026-10-10. No source found establishes an optimal Minecraft AFK camera history window. The values below are explicit engineering starting points, not facts supplied by the papers.

## What the sources support

- [NIST: Single Exponential Smoothing](https://www.itl.nist.gov/div898/handbook/pmc/section4/pmc431.htm): old observations can lose influence progressively; the damping parameter is selected against an objective and observations. This motivates smooth recency weighting instead of a sudden expiry. NIST's forecasting MSE is not a measure of cinematic quality and is not used as such here.
- [Yu Zheng / Microsoft Research: Trajectory Data Mining](https://www.microsoft.com/en-us/research/project/trajectory-data-mining/): meaningful stay points combine spatial proximity and duration; trajectory segmentation and compression separate meaningful visits from movement while reducing storage. Its real-world GPS thresholds are not imported into Minecraft.
- [Bonatti et al.: Autonomous drone cinematographer, 2018](https://arxiv.org/abs/1808.09563): safe filming considers smoothness, visibility and composition together. This supports checking the whole motion path rather than only rating attractive end positions. We do not implement their drone optimizer or learned policy.

## Chosen starting rules and rationale

| Signal | Initial rule | Reason / tradeoff |
|---|---|---|
| Recent active dwell / edits | Exponential half-life 10 minutes; no fixed look-back cutoff | 50% after 10 min, 25% after 20, 12.5% after 30, 1.56% after an hour. Current work should take over, while builds have a separate durable record. Compare 5/10/20 min in deterministic scenarios. |
| A stay | At least 8 seconds inside a 3-block radius, sampled once/sec | Walking through an 8-block cell should not equal spending time there; qualification fits inside the original default 25-sec AFK delay. Still only a heuristic. |
| Whole builds | Existing adjacent 8-block cells, bounded 96-block extent, retained across sessions until capacity eviction/clear | A finished multi-hour project must not disappear when recent activity fades. Score saturates so sheer block count cannot monopolize framing. |
| Habitual AFK places | Once on actual cinematic entry, at most once per area per 10 min; 14-day half-life | A long AFK session is one visit. Repeated returns matter across sessions; a discontinued hangout gradually matters less. First entry already helps current surroundings. |
| Spatial relevance | Only nearby subjects; distance penalty at 12 blocks | Distant past activity must not pull the camera away from the player's current location. Native focus range remains the hard framing constraint. |
| Raw walking trail | At most 512 spatial samples, session-local, segmented at gaps/teleports; revalidate geometry | Resource bound rather than an arbitrary historical window. Old points are route suggestions, never proof that a corridor is still open. |

These rules choose candidate subjects, not a uniquely "best perspective". Safe camera reach, visible surfaces, native preset variety and avoiding repetition determine whether a candidate can actually be filmed. No learned ownership or semantic understanding of "oil platform" is claimed. Aesthetic tuning requires recordings/user feedback from real builds; scenario tests alone cannot establish viewer preference.
