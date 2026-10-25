// Development-only authoring helper. Never runs in the app; no external data.
const fs = require('fs');
const path = require('path');
const definitions = [
  ['garden', 'A little patch of green', 'GARDEN', ['FLOWERS','CARROTS','SOIL']],
  ['breakfast', 'Start the day', 'BREAKFAST', ['TOAST','EGGS','CEREAL']],
  ['weather', 'Look outside', 'WEATHER', ['CLOUD','RAIN','SNOW','WIND']],
  ['music', 'Make a little noise', 'MUSIC', ['PIANO','FLUTE','DRUMS']],
  ['sports', 'Get in the game', 'SPORTS', ['TENNIS','SOCCER','HOCKEY']],
  ['colors', 'Bright ideas', 'COLORS', ['PURPLE','YELLOW','ORANGE']],
  ['kitchen', 'Set the table', 'KITCHEN', ['PLATES','SPOONS','FORKS']],
  ['ocean', 'Under the waves', 'OCEAN', ['SHARK','WHALE','CORAL']],
  ['forest', 'A walk in the woods', 'FOREST', ['CEDAR','BIRCH','PINE']],
  ['space', 'Beyond our sky', 'SPACE', ['EARTH','VENUS','COMET']],
];
function route(rows, cols, spanLength, seed) {
  let randomState = seed;
  const random = () => { randomState = (1664525 * randomState + 1013904223) >>> 0; return randomState / 4294967296; };
  const n = rows * cols;
  const neighbors = Array.from({length:n},(_,i)=>Array.from({length:n},(_,j)=>j).filter(j =>
    Math.abs(Math.floor(i/cols)-Math.floor(j/cols)) + Math.abs(i%cols-j%cols) === 1));
  let nodes = 0;
  const used = new Set([0]), result=[0];
  function connected() {
    const open = Array.from({length:n},(_,i)=>i).filter(i=>!used.has(i));
    if (!open.length) return true;
    const seen = new Set([open[0]]), q=[open[0]];
    for (let k=0;k<q.length;k++) for (const next of neighbors[q[k]]) if (!used.has(next)&&!seen.has(next)) {seen.add(next);q.push(next);}
    return seen.size===open.length;
  }
  function search() {
    if (++nodes > 1000000) return false;
    if (result.length === spanLength && !result.some(i=>Math.floor(i/cols)===rows-1) && !result.some(i=>i%cols===cols-1)) return false;
    if (result.length===n) return true;
    const options = neighbors[result[result.length-1]].filter(i=>!used.has(i)).map(i=>({i,r:random()})).sort((a,b)=>a.r-b.r);
    for (const {i} of options) {
      used.add(i);result.push(i);
      if (connected() && search()) return true;
      result.pop();used.delete(i);
    }
    return false;
  }
  if (!search()) throw Error('Could not create full board');
  return result;
}
let output = `package dev.clean.words.games.strands\n\nimport dev.clean.words.core.PuzzleRecord\n\n/** Ten original, deliberately compact development puzzles; not difficulty-calibrated. */\nobject StrandsContent {\n    val puzzles: List<PuzzleRecord> = listOf(\n`;
definitions.forEach(([id, clue, span, others], index) => {
  const words=[span,...others], total=words.join('').length, rows=4, cols=total/rows;
  if (!Number.isInteger(cols)) throw Error('Content dimensions');
  const cells=route(rows,cols,span.length,1009+index*701);
  output += `        make("strands-${id}-v1", "${clue}", ${rows}, ${cols},\n            listOf(${words.map(w=>'"'+w+'"').join(', ')}),\n            listOf(${cells.join(', ')})),\n`;
});
output += `    )\n\n    private fun make(id: String, clue: String, rows: Int, columns: Int, words: List<String>, route: List<Int>): PuzzleRecord {\n        val letters = CharArray(rows * columns)\n        var offset = 0\n        val answers = words.map { word ->\n            val path = route.subList(offset, offset + word.length).toList()\n            path.forEachIndexed { index, cell -> letters[cell] = word[index] }\n            offset += word.length\n            StrandAnswer(word, path)\n        }\n        return StrandsPuzzle(rows, columns, letters.concatToString(), clue, words.first(), answers).snapshot(id)\n    }\n}\n`;
fs.writeFileSync(path.join(__dirname,'StrandsContent.kt'),output);
process.stdout.write(`Generated ${definitions.length} original Strand-ish records.\n`);
