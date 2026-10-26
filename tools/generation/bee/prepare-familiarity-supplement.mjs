// Original AI editorial supplement. Offline Node built-ins only; no frequency inference.
// From repository root: node tools/generation/bee/prepare-familiarity-supplement.mjs [--check]
import {readFileSync, writeFileSync} from 'node:fs';
import {createHash} from 'node:crypto';
import assert from 'node:assert/strict';

const sha = value => createHash('sha256').update(value).digest('hex');
const version = 'original-ai-editorial-bee-supplement-v2';
const evidence = 'original-ai-editorial; human-review-pending; frequency-unknown';
const indexPath = 'app/src/main/assets/generation/bee/words-v1.tsv';
const supplementPath = 'app/src/main/assets/generation/bee/familiarity-supplement-v2.tsv';
const reportPath = 'tools/generation/bee/familiarity-supplement-report.json';
const draftPath = 'tools/generation/bee/familiarity-supplement-draft.json';
const indexRaw = readFileSync(indexPath);
const indexHash = 'd9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143';
assert.equal(sha(indexRaw), indexHash, 'Pinned original index changed');
const sourceRaw = readFileSync('app/src/main/assets/dictionaries/enable1.txt');
const sourceHash = '3f16130220645692ed49c7134e24a18504c2ca55b3c012f7290e3e77c63b1a89';
assert.equal(sha(sourceRaw), sourceHash, 'Pinned original ENABLE changed');
const draftRaw = readFileSync(draftPath);
const draft = JSON.parse(draftRaw);
assert.equal(draft.version, version);
assert.equal(draft.humanReview, 'pending');
assert.equal(draft.frequencyEvidence, 'none');
const mask = word => [...word].reduce((n,c) => n | (1 << (c.charCodeAt(0)-65)), 0);
const ascii = (a,b) => a < b ? -1 : a > b ? 1 : 0;
const entries = indexRaw.toString('utf8').trimEnd().split('\n').slice(1).map(line => {
  const [bits, word, flag] = line.split('\t');
  assert.equal(Number(bits), mask(word));
  assert.match(word, /^[A-Z]{4,32}$/);
  assert.ok(flag === '0' || flag === '1');
  return {word, mask:Number(bits), familiar:flag === '1'};
});
assert.equal(entries.length,98144);
const byWord = new Map(entries.map(e => [e.word,e]));
assert.equal(byWord.size, entries.length);
const familiar = new Set(entries.filter(e => e.familiar).map(e => e.word));
assert.equal(familiar.size,3636);
const rejected = [...draft.excludedDraftExamples].map(e => ({...e, disposition:'explicit editorial exclusion'}));
const additions = [];
const seen = new Set();
let draftOccurrences = 0;
for (const group of draft.groups) {
  assert.ok(group.rationale.includes('AI editorial') && group.rationale.includes('human review pending'));
  for (const word of group.words.trim().split(/\s+/)) {
    draftOccurrences++;
    const reason = !/^[A-Z]{4,32}$/.test(word) ? 'not uppercase ASCII length 4..32'
      : seen.has(word) ? 'duplicate draft occurrence'
      : new Set(word).size > 7 ? 'more than seven distinct letters'
      : !byWord.has(word) ? 'not in pinned accepted ENABLE index'
      : familiar.has(word) ? 'already familiar in original pinned index' : null;
    seen.add(word);
    if (reason) rejected.push({word,reason,disposition:'validation rejection'});
    else additions.push({word,rationale:group.rationale,humanReview:'pending',frequencyEvidence:'none'});
  }
}
additions.sort((a,b) => ascii(a.word,b.word));
assert.ok(additions.length >= 150 && additions.length <= 350, 'Keep this editorial slice bounded to 150..350 additions');
const supplement = 'word\n'+additions.map(e => e.word).join('\n')+'\n';
const supplementHash = sha(supplement);
const added = new Set(additions.map(e => e.word));
const augmented = new Set([...familiar,...added]);
// Composite pins the exact original accepted/familiar source plus the additive policy.
const fingerprintInput = [indexHash,supplementHash,version,evidence].join('|');
const compositeFingerprint = sha(fingerprintInput);
const grouped = new Map();
for(const entry of entries) {
  if(!grouped.has(entry.mask)) grouped.set(entry.mask,[]);
  grouped.get(entry.mask).push(entry);
}
const candidatesFor = known => [...new Set([...known].filter(w => new Set(w).size === 7).map(mask))].sort((a,b)=>a-b);
const originalMasks = candidatesFor(familiar), expandedMasks = candidatesFor(augmented);
assert.equal(originalMasks.length,310);
const originalMaskSet = new Set(originalMasks);
const rawEntries = [...new Set(sourceRaw.toString('utf8').split(/\r?\n/).filter(w => /^[a-z]{4,32}$/.test(w)).map(w=>w.toUpperCase()))]
  .sort(ascii).map(word=>({word,mask:mask(word)}));
const sum = values => values.reduce((a,b)=>a+b,0);
const distribution = values => {
  const a = [...values].sort((x,y)=>x-y);
  const p = ratio => a[Math.floor((a.length-1)*ratio)];
  return {min:p(0),p05:p(.05),p50:p(.5),p95:p(.95),max:p(1),mean:sum(a)/a.length};
};
const allRows = [];
for(const bits of expandedMasks) {
  const accepted = [];
  for(let subset=bits;subset>0;subset=(subset-1)&bits) accepted.push(...(grouped.get(subset)||[]));
  assert.deepEqual(accepted.map(e=>e.word).sort(ascii),rawEntries.filter(e=>(e.mask&bits)===e.mask).map(e=>e.word));
  const letters = [...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'].filter(c=>mask(c)&bits).join('');
  const points = e => (e.word.length===4 ? 1 : e.word.length)+(e.mask===bits ? 7 : 0);
  const ranked = words => [...words].sort((a,b)=>points(b)-points(a)||ascii(a.word,b.word));
  const benchmark = words => sum(ranked(words).slice(0,10).map(points));
  for(const center of letters) {
    const a = accepted.filter(e=>e.word.includes(center));
    const describe = known => {
      const f = a.filter(e=>known.has(e.word));
      const failures = [];
      if(f.length<12) failures.push('too-few-familiar');
      if(a.length<20||a.length>400) failures.push('accepted-count');
      if(new Set(f.map(e=>e.word.length)).size<3) failures.push('familiar-length-mix');
      if(!f.some(e=>e.word.length===4)) failures.push('no-familiar-four');
      if(!f.some(e=>e.mask===bits)) failures.push('no-familiar-pangram');
      return {qualified:failures.length===0,failures,familiarCount:f.length,completionGoal:Math.ceil(f.length*.7),
        familiarBenchmark:benchmark(f),ratio:benchmark(f)/benchmark(a),hasTenFamiliar:f.length>=10,
        familiarLongCount:f.filter(e=>e.word.length>=8).length,familiarPangrams:f.filter(e=>e.mask===bits).length,
        familiarMaxLength:f.length ? Math.max(...f.map(e=>e.word.length)) : 0,
        familiarTopTen:ranked(f).slice(0,10).map(e=>e.word)};
    };
    allRows.push({letters,center,originalCandidate:originalMaskSet.has(bits),acceptedCount:a.length,
      acceptedBenchmark:benchmark(a),acceptedLongCount:a.filter(e=>e.word.length>=8).length,
      original:describe(familiar),augmented:describe(augmented)});
  }
}
const describePool = (rows,which) => {
  const selected = rows.map(row=>({...row,...row[which]}));
  const qualified = selected.filter(row=>row.qualified);
  const describe = pool => ({boards:pool.length,letterSets:new Set(pool.map(r=>r.letters)).size,
    fewerThanTenFamiliar:pool.filter(r=>!r.hasTenFamiliar).length,
    noFamiliarLongWord:pool.filter(r=>r.familiarLongCount===0).length,
    metrics:Object.fromEntries(['acceptedCount','familiarCount','completionGoal','acceptedBenchmark','familiarBenchmark','ratio','familiarLongCount','familiarPangrams','familiarMaxLength']
      .map(key=>[key,distribution(pool.map(r=>r[key]))])),
    thresholds:[.5,.55,.6,.65,.7,.75,.8,.85,.9,.95,1].map(floor=>{
      const passing=pool.filter(r=>r.familiarBenchmark*10000>=r.acceptedBenchmark*Math.round(floor*10000));
      return {floor,boards:passing.length,letterSets:new Set(passing.map(r=>r.letters)).size};
    })});
  return {all:describe(selected),qualified:describe(qualified)};
};
const originalRows = allRows.filter(r=>r.originalCandidate);
assert.equal(originalRows.length,2170);
assert.equal(originalRows.filter(r=>r.original.qualified).length,1489);
const oldQualified = originalRows.filter(r=>r.original.qualified);
const goalChanges = oldQualified.map(r=>r.augmented.completionGoal-r.original.completionGoal);
const report = {version:'bee-familiarity-supplement-review-v2',
  approval:{ownerDirection:'2026-10-26: preserve full ENABLE acceptance and expand/review familiarity coverage before choosing a filter. Scored generation stays gated meanwhile.',qualityThreshold:null,generatorActivated:false},
  inputs:{indexPath,indexSha256:indexHash,dictionarySha256:sourceHash,originalFamiliarityVersion:'original-ai-editorial-bee-v1',originalFamiliarCount:familiar.size,
    draftPath,draftSha256:sha(draftRaw),supplementPath,supplementSha256:supplementHash,supplementVersion:version,
    supplementWordCount:additions.length,familiarityEvidence:evidence,compositeFamiliarityVersion:version,compositeFamiliarityFingerprint:compositeFingerprint,
    compositeFingerprintRecipe:'SHA256([base.sourceHash, supplementSha256, version, evidence].join("|")); base.sourceHash is the pinned original index SHA-256',compositeFingerprintInput:fingerprintInput,
    augmentedFamiliarCount:augmented.size},
  review:{basis:'Original AI editorial general-use familiarity judgment only; no automatic promotion based on points or ratio.',
    humanReviewedWords:0,empiricalFrequencyRatedWords:0,humanReview:'pending',frequencyEvidence:'none',
    acceptance:'Every original matching ENABLE word remains accepted; no length cap, pangram cap, new acceptance policy or score change.',
    goals:'New snapshots using this optional input freeze the augmented familiar count. Existing snapshots retain original goal/accepted/familiar sets.',
    futureFrequencyBoundary:'A future frequency-backed input must have a new pinned version/hash, evidence/license review and exhaustive evaluation; unfinished work is not a dependency.'},
  counts:{draftOccurrences,explicitEditorialExclusions:draft.excludedDraftExamples.length,validationRejections:rejected.filter(e=>e.disposition==='validation rejection').length,
    addedWords:additions.length,addedLongWords:additions.filter(e=>e.word.length>=8).length,
    addedLengthHistogram:Object.fromEntries([...new Set(additions.map(e=>e.word.length))].sort((a,b)=>a-b).map(n=>[n,additions.filter(e=>e.word.length===n).length])),
    originalCandidateSets:originalMasks.length,expandedCandidateSets:expandedMasks.length,newCandidateSets:expandedMasks.length-originalMasks.length},
  comparison:{originalSetsOriginalFamiliarity:describePool(originalRows,'original'),originalSetsAugmentedFamiliarity:describePool(originalRows,'augmented'),
    expandedSetsAugmentedFamiliarity:describePool(allRows,'augmented'),
    originalQualifiedOnlyAugmentedFamiliarity:describePool(oldQualified,'augmented'),
    originalQualifiedGoalIncrease:distribution(goalChanges),originalQualifiedGoalsChanged:goalChanges.filter(n=>n!==0).length},
  additions,rejected,boards:allRows};
const header=JSON.stringify({...report,boards:undefined},null,2);
const reportText=header.slice(0,-2)+',\n  "boards": [\n'+allRows.map(row=>'    '+JSON.stringify(row)).join(',\n')+'\n  ]\n}\n';
for(const[path,text]of [[supplementPath,supplement],[reportPath,reportText]]) {
  if(process.argv.includes('--check')) assert.equal(readFileSync(path,'utf8'),text,`Stale ${path}`);
  else writeFileSync(path,text,'utf8');
}
console.log(JSON.stringify({inputs:report.inputs,counts:report.counts,comparison:report.comparison},null,2));
