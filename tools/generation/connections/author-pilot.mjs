// Original AI-authored pilot. Development tool; no downloaded source or runtime use.
import { writeFileSync } from 'node:fs';
const c = (id, label, relationType, explanation, entries, prerequisites = 'Everyday American English') => ({
  id, revision: 1, label, relationType, explanation, prerequisites,
  wordplay: relationType === 'compound', reviewStatus: 'ai-draft-needs-human-review',
  members: entries.split(';').map(e => { const [tile, sense] = e.split('='); return {tile, sense}; }),
});
const categories = [
  c('body','Human limbs or limb sections','semantic','Parts of human arms or legs.', 'FOOT=lower limb extremity;HAND=upper limb extremity;ARM=upper limb;LEG=lower limb;KNEE=leg joint;ELBOW=arm joint'),
  c('containers','Containers','semantic','Objects used to hold things.', 'BASKET=woven container;CUP=small drinking vessel;BIN=storage receptacle;CRATE=packing container;JAR=wide mouthed vessel;TUB=large open vessel'),
  c('cookware','Stovetop cooking vessels','semantic','Vessels used to cook food on a stove.', 'PAN=shallow cooking vessel;POT=deep cooking vessel;WOK=rounded stir fry vessel;SKILLET=frying pan;SAUCEPAN=handled cooking vessel;KETTLE=water heating vessel'),
  c('cleaning','Manual cleaning tools','semantic','Hand operated tools for cleaning household surfaces.', 'SPONGE=absorbent cleaning tool;MOP=floor cleaning tool;BROOM=sweeping tool;DUSTER=dust removing tool;BRUSH=scrubbing tool;SQUEEGEE=tool wiping water from glass'),
  c('fire','FIRE ___','compound','Add FIRE before each tile.', 'FLY=firefly;PLACE=fireplace;WOOD=firewood;BOAT=fireboat;WALL=firewall;ENGINE=fire engine;WORK=firework'),
  c('book','BOOK ___','compound','Add BOOK before each tile.', 'SHELF=bookshelf;WORM=bookworm;MARK=bookmark;CLUB=book club;END=bookend;CASE=bookcase'),
  c('tooth','TOOTH ___','compound','Add TOOTH before each tile.', 'BRUSH=toothbrush;PASTE=toothpaste;ACHE=toothache;PICK=toothpick;DECAY=tooth decay;FAIRY=tooth fairy'),
  c('paper','PAPER ___','compound','Add PAPER before each tile.', 'WORK=paperwork;PLANE=paper plane;BACK=paperback;WEIGHT=paperweight;BAG=paper bag;CUT=paper cut'),
  c('instruments','Musical instruments','semantic','Instruments played to make music.', 'BASS=bass guitar or double bass;HORN=brass musical instrument;HARP=plucked string instrument;FLUTE=woodwind instrument;DRUM=percussion instrument;VIOLIN=bowed string instrument'),
  c('birds','Birds','semantic','Common English bird names.', 'CRANE=long legged bird;SWALLOW=aerial insect eating bird;SWIFT=fast flying bird;ROBIN=red breasted bird;FINCH=small seed eating bird;WREN=small songbird'),
  c('fasteners','Fasteners','semantic','Items used to join or secure materials.', 'BOLT=threaded fastener;NAIL=driven metal fastener;SCREW=helical threaded fastener;TACK=small sharp nail;RIVET=permanent metal fastener;STAPLE=U shaped fastener'),
  c('mammals','Mammals','semantic','Animal names in their mammal senses.', 'WOLF=wild canine;BEAR=large omnivorous mammal;SEAL=marine mammal;OTTER=semi aquatic mammal;FOX=small wild canine;MOLE=burrowing mammal'),
  c('mail','Verbs for preparing or sending a letter','semantic','Actions used when mailing a physical letter.', 'POST=send through the postal system;STAMP=apply postage;ADDRESS=write destination information;SEND=dispatch;SEAL=close envelope securely;MAIL=send through the postal system'),
  c('trees','Trees','semantic','Names of trees.', 'PALM=tropical tree;PINE=needle leaved evergreen;ASH=deciduous tree;FIR=conifer;ELM=deciduous shade tree;BIRCH=tree often with pale bark'),
  c('banking','Banking terms','semantic','Words in ordinary personal banking.', 'BANK=financial institution;INTEREST=charge or return on money;LOAN=borrowed sum;DEPOSIT=money placed in an account;CREDIT=addition to an account;BALANCE=amount in an account'),
  c('sports','Actions in ball sports','semantic','Verbs for playing a ball in sports; American and British terms.', 'PITCH=throw a baseball to a batter;BAT=hit with a bat;BOWL=deliver a cricket ball;SERVE=put a tennis ball into play;PASS=send ball to teammate;SHOOT=attempt to score a goal or basket', 'Baseball, cricket, tennis and team ball sports; BOWL uses the cricket sense'),
  c('subjects','School subjects','semantic','Common school fields of study.', 'ART=visual arts study;HISTORY=study of the past;MATH=mathematics;SCIENCE=study of natural phenomena;MUSIC=musical study;GEOGRAPHY=study of places'),
  c('punctuation','Punctuation marks','semantic','Names of written punctuation marks.', 'COMMA=comma punctuation;COLON=colon punctuation;PERIOD=full stop;DASH=dash punctuation;SEMICOLON=semicolon punctuation;APOSTROPHE=apostrophe punctuation'),
  c('dog','Dog training cues','semantic','Common spoken cues to a trained dog.', 'SIT=lower hindquarters;STAY=remain in place;HEEL=walk beside handler;FETCH=retrieve an object;COME=approach handler;DOWN=lie down'),
  c('chess','Chess piece types','semantic','The six standard types of chess piece.', 'KING=royal chess piece;QUEEN=combined rook and bishop movement;ROOK=orthogonal moving piece;BISHOP=diagonal moving piece;KNIGHT=L moving piece;PAWN=basic forward moving piece', 'Basic chess vocabulary'),
];
const decoyRelations = [
  c('ball','___ BALL','compound','Append BALL; closed compounds.', 'FOOT=football;HAND=handball;BASKET=basketball;BASE=baseball;VOLLEY=volleyball;SOFT=softball'),
  c('cake','___ CAKE','compound','Append CAKE; open or closed compounds.', 'CUP=cupcake;PAN=pancake;SPONGE=sponge cake;CHEESE=cheesecake;POUND=pound cake;FRUIT=fruitcake'),
  c('ear','EAR ___','compound','Prepend EAR; closed compounds.', 'WORM=earworm;MARK=earmark;ACHE=earache;DRUM=eardrum;RING=earring;PLUG=earplug'),
  c('house','HOUSE ___','compound','Prepend HOUSE; closed compounds.', 'FLY=housefly;BOAT=houseboat;WORK=housework;HOLD=household;WIFE=housewife;COAT=housecoat'),
  c('eat','Eat quickly','sense','Verbs meaning to eat quickly or greedily.', 'SWALLOW=gulp down;BOLT=eat hastily;WOLF=eat greedily;DEVOUR=eat ravenously;SCOFF=eat greedily;GOBBLE=eat quickly', 'Informal English; SCOFF in its eating sense'),
];
for (const relation of [...categories, ...decoyRelations]) {
  relation.knownOverlaps = [...categories, ...decoyRelations].filter(other => other !== relation && other.members.some(m => relation.members.some(n => n.tile === m.tile))).map(r => r.id);
}
const group = (categoryId, words) => ({categoryId, words: words.split(' ')});
const boards = [
  {id:'pilot-household', revision:1, groups:[group('body','FOOT HAND KNEE LEG'), group('containers','BASKET CUP BIN CRATE'), group('cookware','PAN WOK SKILLET SAUCEPAN'), group('cleaning','SPONGE MOP BROOM DUSTER')], decoyIds:['ball','cake'], notes:'Football/handball/basketball and cupcake/pancake/sponge cake cross intended categories. KNEE and SAUCEPAN avoid additional possible compound readings of ARM and POT. Broad household-object interpretations still need human review.'},
  {id:'pilot-compounds', revision:1, groups:[group('fire','FLY PLACE WOOD BOAT'), group('book','SHELF WORM MARK CLUB'), group('tooth','BRUSH PASTE ACHE PICK'), group('paper','WORK PLANE BACK WEIGHT')], decoyIds:['ear','house'], notes:'Earworm/earmark/earache and housefly/houseboat/housework. FIREWORK is a modeled extra quartet option; test all five FIRE matches rather than ignoring it.'},
].map(b => ({...b, reviewStatus:'ai-draft-needs-human-review'}));
const bank = {schemaVersion:1, version:'connections-pilot-2026-10-26-v1', provenance:{source:'Original AI-authored CLEAN Words pilot, 2026-10-26', rights:'Original project-authored selections and explanations; ordinary words and factual relationships. No external dataset or published puzzle source used.', humanReviews:0, frequencyReviews:0}, reviewStatus:'ai-draft-needs-human-review', categories, decoyRelations, boards};
writeFileSync(new URL('./pilot.json', import.meta.url), JSON.stringify(bank,null,2)+'\n');
console.log(`Wrote ${categories.length} intended categories, ${decoyRelations.length} decoy relations, ${boards.length} example boards.`);
