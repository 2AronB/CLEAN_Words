// Offline exhaustive audit of the pinned Bee corpus. Node built-ins only.
// Run at repository root; --check verifies the exact deterministic report bytes.
import { readFileSync, writeFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import assert from 'node:assert/strict';

const indexPath = 'app/src/main/assets/generation/bee/words-v1.tsv';
const outputPath = 'tools/generation/bee/opening-benchmark-report.json';
const sha = bytes => createHash('sha256').update(bytes).digest('hex');
const indexBytes = readFileSync(indexPath);
const indexHash = 'd9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143';
assert.equal(sha(indexBytes), indexHash, 'Pinned index changed; evaluate a new explicit input version');
const policy = JSON.parse(readFileSync('app/src/main/assets/generation/bee/policy.json', 'utf8'));
const mask = word => [...word].reduce((bits, c) => bits | (1 << (c.charCodeAt(0) - 65)), 0);
const lettersOf = bits => [...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'].filter(c => bits & mask(c)).join('');
const entries = indexBytes.toString('utf8').trimEnd().split('\n').slice(1).map(line => {
  const [bits, word, familiar] = line.split('\t');
  assert.equal(Number(bits), mask(word));
  assert.match(word, /^[A-Z]{4,32}$/);
  assert.ok(familiar === '0' || familiar === '1');
  return {word, mask: Number(bits), familiar: familiar === '1'};
});
assert.equal(entries.length, 98144);
assert.equal(new Set(entries.map(e => e.word)).size, entries.length);
assert.equal(entries.filter(e => e.familiar).length, 3636);
const source = readFileSync('app/src/main/assets/dictionaries/enable1.txt');
assert.equal(sha(source), policy.dictionarySha256);
// Independent exhaustive raw-dictionary inclusion, separate from TSV submask grouping.
const rawWords = [...new Set(source.toString('utf8').split(/\r?\n/)
  .filter(w => /^[a-z]{4,32}$/.test(w)).map(w => w.toUpperCase()))].sort();
const rawEntries = rawWords.map(word => ({word, mask: mask(word)}));
const grouped = new Map();
for (const entry of entries) {
  if (!grouped.has(entry.mask)) grouped.set(entry.mask, []);
  grouped.get(entry.mask).push(entry);
}
const candidates = [...new Set(entries.filter(e => e.familiar && new Set(e.word).size === 7).map(e => e.mask))].sort((a,b) => a-b);
assert.equal(candidates.length, 310);
const sum = values => values.reduce((a,b) => a+b, 0);
const histogram = words => Object.fromEntries([...new Set(words.map(w => w.word.length))].sort((a,b) => a-b)
  .map(length => [length, words.filter(w => w.word.length === length).length]));
const distribution = values => {
  const sorted = [...values].sort((a,b) => a-b);
  const at = p => sorted[Math.floor((sorted.length-1)*p)];
  return {min: at(0), p05: at(.05), p25: at(.25), p50: at(.5), p75: at(.75), p95: at(.95), max: at(1), mean: sum(sorted)/sorted.length};
};
const boards = [];
for (const bits of candidates) {
  const all = [];
  for (let subset = bits; subset > 0; subset = (subset-1)&bits) all.push(...(grouped.get(subset)||[]));
  assert.deepEqual(all.map(e => e.word).sort(), rawEntries.filter(e => (e.mask & bits) === e.mask).map(e => e.word));
  const letters = lettersOf(bits);
  const points = e => (e.word.length === 4 ? 1 : e.word.length) + (e.mask === bits ? 7 : 0);
  const best = words => [...words].sort((a,b) => points(b)-points(a) || (a.word < b.word ? -1 : a.word > b.word ? 1 : 0)).slice(0,10)
    .map(e => ({word: e.word, points: points(e), familiar: e.familiar, pangram: e.mask === bits}));
  for (const center of letters) {
    const accepted = all.filter(e => e.word.includes(center));
    const familiar = accepted.filter(e => e.familiar);
    const topAccepted = best(accepted), topFamiliar = best(familiar);
    const absoluteBenchmark = sum(topAccepted.map(e => e.points));
    const familiarBenchmark = sum(topFamiliar.map(e => e.points));
    const failures = [];
    if (familiar.length < 12) failures.push('too-few-familiar');
    if (accepted.length < 20 || accepted.length > 400) failures.push('accepted-count');
    if (new Set(familiar.map(e => e.word.length)).size < 3) failures.push('familiar-length-mix');
    if (!familiar.some(e => e.word.length === 4)) failures.push('no-familiar-four');
    if (!familiar.some(e => e.mask === bits)) failures.push('no-familiar-pangram');
    const pangrams = accepted.filter(e => e.mask === bits);
    boards.push({letters, center, defaultQualified: failures.length === 0, failures,
      acceptedCount: accepted.length, familiarCount: familiar.length, completionGoal: Math.ceil(familiar.length*.7),
      absoluteBenchmark, familiarBenchmark, familiarAbsoluteRatio: familiarBenchmark/absoluteBenchmark,
      hasTenAccepted: accepted.length >= 10, hasTenFamiliar: familiar.length >= 10,
      acceptedLengthHistogram: histogram(accepted), familiarLengthHistogram: histogram(familiar),
      acceptedMaxLength: Math.max(...accepted.map(e => e.word.length)), familiarMaxLength: Math.max(...familiar.map(e => e.word.length)),
      acceptedLongCount: accepted.filter(e => e.word.length >= 8).length,
      familiarLongCount: familiar.filter(e => e.word.length >= 8).length,
      pangramCount: pangrams.length, familiarPangramCount: pangrams.filter(e => e.familiar).length,
      pangrams: pangrams.map(e => ({word:e.word, points:points(e), familiar:e.familiar})).sort((a,b) => a.word < b.word ? -1 : 1),
      topAccepted, topFamiliar, topAcceptedFamiliarCount: topAccepted.filter(e => e.familiar).length});
  }
}
const qualified = boards.filter(b => b.defaultQualified);
assert.equal(boards.length, 2170);
assert.equal(qualified.length, 1489);
const summary = pool => ({boards:pool.length, sets:new Set(pool.map(b => b.letters)).size,
  fewerThanTenAccepted:pool.filter(b => !b.hasTenAccepted).length,
  fewerThanTenFamiliar:pool.filter(b => !b.hasTenFamiliar).length,
  metrics:Object.fromEntries(['acceptedCount','familiarCount','completionGoal','absoluteBenchmark','familiarBenchmark',
    'familiarAbsoluteRatio','acceptedMaxLength','familiarMaxLength','acceptedLongCount','familiarLongCount',
    'pangramCount','familiarPangramCount','topAcceptedFamiliarCount'].map(key => [key,distribution(pool.map(b => b[key]))]))});
const thresholds = [.50,.55,.60,.65,.70,.75,.80,.85,.90,.95,1].map(threshold => {
  // Integer comparison is identical to a basis-point runtime policy, avoiding rounded ratios.
  const retained = qualified.filter(b => b.familiarBenchmark*10000 >= b.absoluteBenchmark*Math.round(threshold*10000));
  return {minimumRatio:threshold, retainedBoards:retained.length, retainedSets:new Set(retained.map(b => b.letters)).size,
    retainedPercent:retained.length/qualified.length*100,
    ...(retained.length ? {completionGoal:distribution(retained.map(b => b.completionGoal))} : {})};
});
const asciiCompare = (a,b) => a < b ? -1 : a > b ? 1 : 0;
const byRatio = [...qualified].sort((a,b) => a.familiarAbsoluteRatio-b.familiarAbsoluteRatio || asciiCompare(a.letters,b.letters) || asciiCompare(a.center,b.center));
const compact = b => ({letters:b.letters, center:b.center, accepted:b.acceptedCount, familiar:b.familiarCount,
  goal:b.completionGoal, absolute:b.absoluteBenchmark, familiarBenchmark:b.familiarBenchmark, ratio:b.familiarAbsoluteRatio,
  pangrams:b.pangramCount, familiarPangrams:b.familiarPangramCount, topAccepted:b.topAccepted, topFamiliar:b.topFamiliar});
const report = {version:'bee-opening-benchmark-audit-v1', openingLength:10,
  inputs:{indexPath,indexSha256:indexHash,dictionarySha256:sha(source),familiarityVersion:policy.familiarityPolicyVersion,
    generatorVersion:'bee-seven-selector-v1',profileVersion:'seven-standard-provisional-v1'},
  methodology:{enumeration:'Every familiar seven-distinct-letter mask and all seven centers; exact accepted lists independently matched against the raw ENABLE dictionary for every mask.',
    benchmark:'Sum of at most ten highest-scoring words, points descending then ASCII word ascending. Boards with fewer than ten are explicitly flagged; all current default-qualified boards have ten.',
    points:'Four letters = 1; otherwise length; all seven distinct letters adds 7.',
    longWordDefinition:'At least eight letters; per-board complete length histograms and maximum lengths are also supplied.',
    percentile:'Nearest lower order statistic: sorted[floor((n-1)*p)]. Ratios are unrounded in threshold comparisons.',
    familiarity:'Pinned original AI editorial shortlist only. Zero human reviews; no empirical frequency evidence. No inferred frequency ratings.',
    activation:'Audit only; no quality threshold activated.'},
  allCandidates:summary(boards),currentDefaultQualified:summary(qualified),thresholdTradeoffs:thresholds,
  examples:{weakest:byRatio.slice(0,3).map(compact),median:[compact(byRatio[Math.floor(byRatio.length/2)])],strongest:byRatio.slice(-3).map(compact)},boards};
// Keep generated rows compact; each board is one independently inspectable line.
const header = JSON.stringify({...report,boards:undefined},null,2);
const output = header.slice(0,-2)+',\n  "boards": [\n'+boards.map(board => '    '+JSON.stringify(board)).join(',\n')+'\n  ]\n}\n';
if (process.argv.includes('--check')) assert.equal(readFileSync(outputPath,'utf8'),output,'Stale benchmark report');
else writeFileSync(outputPath,output,'utf8');
console.log(JSON.stringify({...report,boards:undefined,examples:undefined},null,2));
