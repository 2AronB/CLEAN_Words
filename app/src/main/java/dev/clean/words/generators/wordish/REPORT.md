# Wordish checks — 2026-10-25

Command (Android Studio bundled JBR; existing approved Gradle cache):

```powershell
.\gradlew.bat --offline :app:testDebugUnitTest --tests dev.clean.words.generators.wordish.WordishGeneratorTest
node tools/generation/wordish/prepare-bank.mjs --check
```

Nine JVM tests passed. On real bundled inputs, seeds 0–999, after 100 warmups:
731 distinct answers; 17 repeated-letter patterns; zero generation failures,
zero runtime rejections; worst scan 1,387 entries. p50 576 µs, p95 1,395 µs,
maximum 5,516 µs on this development machine. Timing is diagnostic, not a device
latency guarantee. A separate exclusion-drain test selected all 1,387 answers
without repetition and then returned Exhausted.

Tests cover canonical order and deterministic reproduction, distinct seeds,
empty/tiny/exhausted banks, unknown dictionary, malformed/duplicate/unaccepted
entries, preflight and in-loop cancellation, every eligible answer accepted and
solved, duplicate-letter evidence, strongest keyboard feedback, six-guess win/loss,
invalid-guess budget, copied bank ownership and full snapshot codec/draft retention
after bank changes. Original ten starter snapshots remain unchanged.

Actual source review: 1,387 AI-editorial eligible answers, zero human/frequency
reviewed; 64 rejected draft occurrences, fully listed by preprocessing report.
TSV SHA-256 390a36fde39a5264d51032eb7dea2187dd2934c940f1ad2f9dda2d6a9bf15414.
No Android/phone/fairness acceptance is inferred from these tests. Shared UI
generation, persistence/cancellation and packaged APK checks are recorded by the
primary integration report in docs/VERIFICATION.md.
