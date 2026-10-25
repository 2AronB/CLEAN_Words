# Game/content options — discovery research

2026-10-25. Recommendations and feasibility judgments only. No added games,
dependencies, content downloads or implementation approved.

## Additional candidates

| Game family | Dictionary-based generation | Additional quality work |
| --- | --- | --- |
| Boggle-style adjacent-letter hunt | Strong fit | Solve each board; ensure enough familiar words; tune board size and timer |
| Wordscapes-style anagram crossword | Strong fit | Choose letter sets and intersecting answer layouts; distinguish bonus words |
| Ordinary word search | Strong fit | Place words and fill cells; theme lists optional; avoid accidental objectionable words |
| Word ladders | Strong fit | Connect words differing by one letter; verify a route and fair intermediate vocabulary |
| Letter Boxed-style word chain | Good fit with solver | Enforce side restrictions and verify short solutions using familiar words |
| Solo Scrabble-style tile board | Feasible, larger effort | Tile distribution, scoring, move search and optional opponent strength |

Popularity evidence is not a ranked market analysis. Publisher Zynga describes
[Boggle](https://www.zynga.com/games/boggle-with-friends/) as having millions of
players and [Words With Friends](https://wordswithfriends.com/) reports over four
million daily active players. These are publisher claims, not independent counts.
[PeopleFun's company profile](https://www.linkedin.com/company/peoplefun) describes
Wordscapes as a leading word game serving millions. Word ladders and word search
are familiar formats; no current comparative usage count was established here.
[Merriam-Webster's Blossom page](https://www.merriam-webster.com/games/blossom-word-game)
links its four-answer Quordle game; multiple-board guessing is an established
variant. Do not describe all candidates as equally popular.

## Five-game content constraints (engineering assessment)

- Word guessing: separate a familiar answer list from a broader accepted-guess
  list. Multi-board variants can reuse the rules but need readability and guess
  budget decisions.
- Spelling Bee-style: letter-set generation and exhaustive dictionary search
  are natural fits; accepted words and satisfying answer counts need curation.
- Connections-style: words alone cannot supply satisfying categories, wordplay
  or evaluate competing groupings. Need category/relationship metadata and
  editorial review of each combined board.
- Strands-style: need theme clues, theme vocabulary, a spanning answer and a
  valid covering layout, not merely random letters and dictionary membership.
- Mini-crossword: a fillable grid is only half the task; clues must match the
  intended word sense and have fair difficulty.

Recommendation: generate mechanical puzzles from vetted vocabulary and consider
large reviewed bundled content pools for editorial games, as allowed by R-005.
No guarantee of near-infinite fair editorial puzzles is justified yet.

## Content availability and licensing check

The bundled-pool suggestion did not refer to an identified collection. No large
ready-to-bundle Connections, Strands or mini-crossword collection has been
verified as suitable and licensed for this app. Do not treat the recommendation
as proof that one exists. This is a current content feasibility unknown.

Confirmed building-block candidate:
[Open English WordNet](https://en-word.net/) provides word meanings and relations
under CC BY 4.0. It is vocabulary/semantic data, not a finished puzzle collection;
attribution and specific integration terms must be handled before inclusion.
No source has been downloaded or selected as a dependency.

Readily downloadable puzzle archives are not automatically redistribution
sources. For example, [Cryptics' own datasheet](https://cryptics.georgeho.org/datasheet)
describes scraped publisher clues, an ODbL database license and separately held
copyright in annotations. This is not a verified source for this app. Likewise,
[Crossword Clue Tutor](https://crosswordcluetutor.com/content-license/) explicitly
separates MIT software from restricted content and disallows bulk redistribution
without permission. Check content provenance and license separately from code.

Recommendation: openly licensed vocabulary plus original generated/authored and
reviewed puzzle content. Other creators' collections are candidates only when
their content license and provenance support bundling. Owner has not yet chosen
the reviewed-pool approach or an output-content license for original puzzles.

The research agent independently checked these boundaries against official help:
[Spelling Bee](https://nytimes.zendesk.com/hc/en-us/articles/23675493312404-Spelling-Bee),
[Connections](https://thenewyorktimeshelpcenter.helpjuice.com/360011158491-New-York-Times-Games/28525912587924-Connections),
and [Mini-crossword](https://nytimes.zendesk.com/hc/en-us/articles/360025912452-The-Mini-Crossword).
These describe reference mechanics, not permission to copy content.

Proposed Connections agent pilot: after audience, difficulty, content format and
review criteria are agreed, draft 20 original categories and five complete boards
with explanations, provenance notes and known overlaps. Close with structural
checks, independent ambiguity review and primary-agent acceptance/rejection per
board. Drafts remain unreviewed until reviewed; this is not an infinite corpus.
No such content task has been commissioned yet.

## Variant proposals, not selected requirements

Word guessing: 1/2/4/6 simultaneous boards or word lengths. Bee-style: letter
count, target score or pangram-focused mode. Connections-style: straightforward
categories versus harder wordplay or themed packs. Strands-style: grid sizes and
theme difficulty. Mini-crossword: grid sizes and clue difficulty. Mechanical
parameter changes alone do not guarantee fair puzzles; prefer distinct, tested
modes over many arbitrary settings. Exact MVP minimum remains open.
