import {readFileSync, writeFileSync} from 'node:fs';
import {createHash} from 'node:crypto';
import assert from 'node:assert/strict';
import {pathToFileURL} from 'node:url';

const key = words => [...words].sort().join('|');
function choose4(words) {
  const groups = [];
  for(let a=0;a<words.length-3;a++) for(let b=a+1;b<words.length-2;b++)
    for(let c=b+1;c<words.length-1;c++) for(let d=c+1;d<words.length;d++)
      groups.push([words[a],words[b],words[c],words[d]]);
  return groups;
}
export function checkBank(bank) {
  const errors = [];
  const require = (ok, message) => {if(!ok) errors.push(message);};
  require(bank.schemaVersion === 1, 'Unsupported schema');
  require(typeof bank.version === 'string' && bank.version.length > 0, 'Missing bank version');
  require(bank.provenance?.source && bank.provenance?.rights, 'Missing provenance/rights');
  require(bank.reviewStatus === 'ai-draft-needs-human-review', 'Unexpected pilot review status');
  require(Array.isArray(bank.categories) && bank.categories.length === 20, 'Pilot must have 20 categories');
  require(Array.isArray(bank.decoyRelations) && Array.isArray(bank.boards), 'Missing relation/board arrays');
  if(errors.length) return {errors, boards:[]};
  const all = [...bank.categories,...bank.decoyRelations];
  const byId = new Map(all.map(r => [r.id,r]));
  const categoryIds = new Set(bank.categories.map(c => c.id));
  require(byId.size === all.length, 'Duplicate relationship ID');
  for(const r of all) {
    const prefix = r.id || 'missing-id';
    for(const field of ['id','label','relationType','explanation','prerequisites','reviewStatus'])
      require(typeof r[field] === 'string' && r[field].trim().length > 0, `${prefix}: missing ${field}`);
    require(r.reviewStatus === 'ai-draft-needs-human-review', `${prefix}: not a human-approved pilot`);
    require(Number.isInteger(r.revision) && r.revision > 0, `${prefix}: invalid revision`);
    require(typeof r.wordplay === 'boolean', `${prefix}: missing wordplay flag`);
    require(Array.isArray(r.members) && r.members.length >= 4, `${prefix}: insufficient members`);
    if(!Array.isArray(r.members)) continue;
    require(r.members.length >= 6 || !!r.shortBankReason, `${prefix}: small bank needs explanation`);
    require(new Set(r.members.map(m => m.tile)).size === r.members.length, `${prefix}: duplicate member`);
    for(const m of r.members) {
      require(typeof m.tile === 'string' && /^[A-Z]+(?: [A-Z]+)*$/.test(m.tile), `${prefix}: invalid tile`);
      require(typeof m.sense === 'string' && m.sense.trim().length > 0, `${prefix}: missing member sense`);
    }
    const expected = all.filter(other => other.id !== r.id && other.members?.some(m => r.members.some(n => n.tile === m.tile))).map(o => o.id).sort();
    require(Array.isArray(r.knownOverlaps) && JSON.stringify([...r.knownOverlaps].sort()) === JSON.stringify(expected), `${prefix}: incomplete overlap links`);
  }
  if(errors.length) return {errors,boards:[]};
  const reports = [], signatures = new Set(), boardIds = new Set();
  for(const board of bank.boards) {
    const startErrors = errors.length;
    require(!boardIds.has(board.id), `${board.id}: duplicate board ID`); boardIds.add(board.id);
    require(typeof board.id === 'string' && board.id.length > 0 && board.revision > 0, 'Invalid board identity');
    require(board.reviewStatus === 'ai-draft-needs-human-review' && !!board.notes, `${board.id}: missing review/notes`);
    require(Array.isArray(board.groups) && board.groups.length === 4, `${board.id}: needs four groups`);
    if(!Array.isArray(board.groups)) continue;
    const tiles = board.groups.flatMap(g => Array.isArray(g.words) ? g.words : []);
    require(tiles.length === 16 && new Set(tiles).size === 16, `${board.id}: needs 16 unique tiles`);
    require(new Set(board.groups.map(g => g.categoryId)).size === 4, `${board.id}: repeated intended category`);
    for(const g of board.groups) {
      const category = byId.get(g.categoryId);
      require(categoryIds.has(g.categoryId) && Array.isArray(g.words) && g.words.length === 4 && g.words.every(w => category?.members.some(m => m.tile === w)), `${board.id}: invalid group ${g.categoryId}`);
    }
    require(Array.isArray(board.decoyIds) && new Set(board.decoyIds).size === board.decoyIds.length && board.decoyIds.length >= 2, `${board.id}: two distinct decoy IDs required`);
    if(errors.length !== startErrors) continue;
    const signature = board.groups.map(g => key(g.words)).sort().join('::');
    require(!signatures.has(signature), `${board.id}: duplicate semantic board`); signatures.add(signature);
    const visible = new Set(tiles), candidates = new Map(), intersections = [];
    for(const r of all) {
      const matches = r.members.map(m => m.tile).filter(w => visible.has(w)).sort();
      intersections.push({relationId:r.id, visibleMembers:matches});
      for(const words of choose4(matches)) candidates.set(key(words),words);
    }
    const decoys = board.decoyIds.map(id => {
      const relation = byId.get(id), matches = relation?.members.filter(m => visible.has(m.tile)) ?? [];
      const groups = board.groups.filter(g => g.words.some(w => matches.some(m => m.tile === w))).length;
      require(!!relation && matches.length === 3 && groups >= 2 && !board.groups.some(g => g.categoryId === id), `${board.id}: invalid three-member cross-group decoy ${id}`);
      return {relationId:id, members:matches, intendedGroupsSpanned:groups};
    });
    const partitions = [], alternatives = [...candidates.values()];
    const visit = (remaining, chosen) => {
      if(partitions.length >= 2) return;
      if(!remaining.size) {partitions.push(chosen.map(key).sort());return;}
      const first = [...remaining].sort()[0];
      for(const quartet of alternatives) if(quartet.includes(first) && quartet.every(w => remaining.has(w))) {
        const next = new Set(remaining); quartet.forEach(w => next.delete(w)); visit(next,[...chosen,quartet]);
      }
    };
    visit(visible,[]);
    const intended = board.groups.map(g => key(g.words)).sort();
    require(partitions.length === 1 && JSON.stringify(partitions[0]) === JSON.stringify(intended), `${board.id}: competing or missing full partition`);
    reports.push({id:board.id, signature, candidateQuartets:candidates.size, testedQuartets:[...candidates.keys()].sort(), partitionCountCappedAtTwo:partitions.length, decoys, intersections});
  }
  return {errors,boards:reports};
}

function selfTest(bank) {
  assert.deepEqual(checkBank(bank).errors, []);
  const mutated = fn => {const b=structuredClone(bank);fn(b);return checkBank(b).errors;};
  assert(mutated(b=>b.boards[0].groups[0].words[0]='HAND').length);
  assert(mutated(b=>b.categories[0].members[0].sense='').length);
  assert(mutated(b=>b.boards[0].decoyIds=['ball','ball']).length);
  assert(mutated(b=>b.boards[0].decoyIds=['ball','eat']).length);
  assert(mutated(b=>b.categories[0].knownOverlaps=[]).length);
  const shuffled=structuredClone(bank);shuffled.boards[0].groups.reverse();shuffled.boards[0].groups.forEach(g=>g.words.reverse());
  assert.equal(checkBank(shuffled).boards[0].signature,checkBank(bank).boards[0].signature);
  const ambiguous=structuredClone(bank), words=ambiguous.boards[0].groups.map(g=>g.words);
  // A second partition mixing two intended groups; keep explicit overlap links complete.
  for(let i=0;i<2;i++) ambiguous.decoyRelations.push({...structuredClone(bank.decoyRelations[0]),id:`test-alt-${i}`, members:[...words[0].slice(i*2,i*2+2),...words[1].slice(i*2,i*2+2),'ALPHA','BETA'].map(tile=>({tile,sense:'test fixture only'}))});
  const relations=[...ambiguous.categories,...ambiguous.decoyRelations];
  for(const r of relations) r.knownOverlaps=relations.filter(o=>o!==r && o.members.some(m=>r.members.some(n=>m.tile===n.tile))).map(o=>o.id);
  assert(checkBank(ambiguous).errors.some(e=>e.includes('competing or missing full partition')));
  console.log('7 validator checks passed (baseline, duplicate, sense, duplicate decoy, insufficient decoy, overlap, order invariance) plus competing-partition rejection.');
}
if(process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const bytes=readFileSync(new URL('./pilot.json',import.meta.url));
  const bank=JSON.parse(bytes); if(process.argv.includes('--self-test')) selfTest(bank);
  const result=checkBank(bank);
  const report={schemaVersion:1,bankVersion:bank.version,sha256:createHash('sha256').update(bytes).digest('hex'), categoryCount:bank.categories.length,decoyRelationCount:bank.decoyRelations.length,exampleBoardCount:bank.boards.length,humanReviewedCategories:0,humanReviewedBoards:0,...result};
  writeFileSync(new URL('./validation-report.json',import.meta.url),JSON.stringify(report,null,2)+'\n');
  console.log(JSON.stringify({categories:report.categoryCount,decoyRelations:report.decoyRelationCount,boards:report.exampleBoardCount,errors:report.errors,partitions:report.boards.map(b=>({id:b.id,count:b.partitionCountCappedAtTwo,quartets:b.candidateQuartets,decoys:b.decoys.length}))}));
  if(result.errors.length) process.exitCode=1;
}
