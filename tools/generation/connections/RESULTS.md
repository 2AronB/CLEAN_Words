# Slice 1 checkpoint — 2026-10-26

Original development pilot; no runtime content replacement. Run from repository root:

```text
node tools/generation/connections/author-pilot.mjs
node tools/generation/connections/validate.mjs --self-test
git diff --check
```

All pass. Regenerating pilot.json produces identical SHA-256 bytes. JSON hash,
all modeled visible intersections, tested quartets, decoy senses and signatures
are retained in [validation-report.json](validation-report.json).

| Input/check | Observed |
| --- | --- |
| Intended categories | 20; 121 member/sense records |
| Separate decoy relations | 5; 30 member/sense records |
| Complete board examples | 2; use eight categories |
| Household board | Two three-member decoys; four candidate quartets; one partition |
| Compound board | Two three-member decoys; eight candidate quartets; one partition |
| Validation errors | 0 |
| Validator self-checks | 8, including rejecting an injected second full partition |
| Human reviews | 0 |

Read-only agent review independently confirmed no prior Connections delivery,
mapped live snapshot fields and reviewed the examples. Primary replaced ARM and
POT in the household fixture with KNEE and SAUCEPAN to reduce possible extra
compound readings. This review remains AI review, not human editorial approval.

The bank is intentionally uneven: broad semantic categories can have many true
members not yet recorded. Missing membership edges may conceal extra groups.
The complete partition check covers only the recorded model, not unrestricted
English. All members, senses, category labels, decoys and explanations require
human review before an editorial quality claim. Category/decoy record counts do
not measure puzzle variety or medium-plus difficulty.

Two examples are sufficient to exercise this slice's contract. The separate
broader content assignment's five-board pilot and 120-category expansion are
not delivered here. Twelve categories have no compatible complete fixture yet;
this does not establish whether the bank can satisfy 200 seeded requests or
recent-history exclusions. Avoid padding the bank or shipping fixtures as a
generated pool. No runtime bank/index format has been implemented.

Next authorized work must start with the [module contract](../../../app/src/main/java/dev/clean/words/generators/connections/README.md)
and [snapshot mapping](../../../app/src/main/java/dev/clean/words/generators/connections/INTEGRATION.md).
Slice 2 is the standalone composer plus focused tests; slice 3 qualifies it
against real inputs. Slice 4 integrates Connections only; slice 5 verifies and
delivers an APK. None of those later slices ran here. Existing Android/phone
checks elsewhere do not verify this pilot as a generated app.
