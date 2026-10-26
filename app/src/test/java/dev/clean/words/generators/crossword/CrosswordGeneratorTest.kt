package dev.clean.words.generators.crossword

import dev.clean.words.core.PuzzleRecord
import org.junit.Assert.*
import org.junit.Test

class CrosswordGeneratorTest {
    private val bank get() = BundledCrosswordBank.bank
    @Test fun sourceTemplatesAndReproduction() {
        assertEquals(emptyList<String>(),CrosswordGenerator.validateSource(bank))
        assertEquals(BundledCrosswordBank.contentSha256,bank.contentSha256)
        assertTrue(bank.answers.size >= 200)
        assertTrue(bank.templates.count {it.width==5}>=10)
        assertTrue(bank.templates.count {it.width==7}>=10)
        val g=CrosswordGenerator(bank)
        val a=g.generate(42) as CrosswordGenerationResult.Success
        assertEquals(a,g.generate(42)); assertIndependent(a.puzzle)
        assertNotEquals(a.signature,(g.generate(43) as CrosswordGenerationResult.Success).signature)
    }
    @Test fun malformedSourcesAndMissingClueCoverageFail() {
        val a=bank.answers.first()
        for(b in listOf(bank.copy(answers=listOf(a,a)),bank.copy(answers=listOf(a.copy(clues=emptyList()))),bank.copy(answers=listOf(a.copy(clues=listOf(a.clues.first().copy(sense="contradiction"))))),bank.copy(templates=listOf(CrosswordTemplate("bad",7,7,".".repeat(48)))))) {
            assertTrue(CrosswordGenerator.validateSource(b).isNotEmpty())
            assertEquals(CrosswordFailure.INVALID_SOURCE,(CrosswordGenerator(b).generate(1) as CrosswordGenerationResult.NoCandidate).reason)
        }
    }
    @Test fun impossibleCrossingsExhaustionBudgetAndCancellationAreExplicit() {
        val tiny=bank.copy(answers=listOf(bank.answers.first {it.text.length==3}))
        assertEquals(CrosswordFailure.IMPOSSIBLE_CONSTRAINTS,(CrosswordGenerator(tiny).generate(1) as CrosswordGenerationResult.NoCandidate).reason)
        assertEquals(CrosswordFailure.BUDGET_EXHAUSTED,(CrosswordGenerator(tiny).generate(1,CrosswordProfile(maxCandidates=1)) as CrosswordGenerationResult.NoCandidate).reason)
        val contradictory=CrosswordBank("synthetic-test-v1","Test-only impossible crossing fixture",listOf("AAA","BBB","CCC","DDD","EEEEE","FFFFF").map { word->CrosswordAnswer(word.lowercase(),word,word,listOf(CrosswordClue(word.lowercase()+"-clue","Test fixture $word",word))) },listOf(CrosswordTemplate("crossing-fixture",5,5,"...##.#.##.....##.#.##...")))
        assertTrue(CrosswordGenerator.validateSource(contradictory).isEmpty())
        assertEquals(CrosswordFailure.IMPOSSIBLE_CONSTRAINTS,(CrosswordGenerator(contradictory).generate(1,CrosswordProfile(5,5)) as CrosswordGenerationResult.NoCandidate).reason)
        assertTrue(CrosswordGenerator(bank).generate(1,cancelled={true}) is CrosswordGenerationResult.Cancelled)
        var polls=0
        assertTrue(CrosswordGenerator(bank).generate(1,cancelled={++polls>5}) is CrosswordGenerationResult.Cancelled)
        assertEquals(CrosswordFailure.BUDGET_EXHAUSTED,(CrosswordGenerator(bank).generate(1,CrosswordProfile(maxSteps=1)) as CrosswordGenerationResult.NoCandidate).reason)
        // All clue texts forbidden by one synthetic previous board: honest cooldown exhaustion.
        val allClues=bank.answers.flatMap {it.clues}.map {cwHash(it.text)}.joinToString(",")
        assertEquals(CrosswordFailure.RECENT_POOL_EXHAUSTED,(CrosswordGenerator(bank).generate(1,recentSignatures=setOf("cw1|7x7|x||$allClues")) as CrosswordGenerationResult.NoCandidate).reason)
    }
    @Test fun previousCluesExcludedAndLegacySignatureReconstructed() {
        val g=CrosswordGenerator(bank)
        val a=g.generate(4) as CrosswordGenerationResult.Success
        val b=g.generate(4,recentSignatures=setOf(a.signature)) as CrosswordGenerationResult.Success
        assertNotEquals(a.signature,b.signature)
        assertTrue(a.signature.split('|')[4].split(',').toSet().intersect(b.signature.split('|')[4].split(',').toSet()).isEmpty())
        val legacy=PuzzleRecord("legacy",title="legacy",data=mapOf("grid" to "ERA##L#P##MAPLE##L#W##EVE","clues" to "ERA|Time period\nELM|Tree\nAPPLE|Fruit\nMAPLE|Syrup tree\nEWE|Sheep\nEVE|Before a holiday"))
        assertTrue(CrosswordGenerator.semanticSignature(legacy).startsWith("cw1|5x5|"))
    }

    /** Independent scanner: does not call generator slots/validator or live game parser. */
    fun assertIndependent(p: PuzzleRecord) {
        val w=p.data.getValue("width").toInt();val h=p.data.getValue("height").toInt();val grid=p.data.getValue("grid")
        assertEquals(w*h,grid.length);assertTrue(grid.all {it=='#'||it in 'A'..'Z'})
        val words=mutableListOf<String>();val identities=mutableListOf<Pair<Int,String>>();val coverage=mutableSetOf<Int>();val starts=mutableSetOf<Int>()
        for(r in 0 until h) for(c in 0 until w) { val at=r*w+c;if(grid[at]=='#')continue
            for((dr,dc) in listOf(0 to 1,1 to 0)) {
                if(r-dr>=0 && c-dc>=0 && grid[(r-dr)*w+c-dc]!='#')continue
                val cells=mutableListOf<Int>();var rr=r;var cc=c
                while(rr<h && cc<w && grid[rr*w+cc]!='#') {cells.add(rr*w+cc);rr+=dr;cc+=dc}
                if(cells.size<=1)continue
                assertTrue(cells.size in 3..7);words.add(cells.map {grid[it]}.joinToString(""));coverage.addAll(cells);starts.add(at);identities.add(at to if(dr==0)"A" else "D")
            }
        }
        assertEquals(words.size,words.distinct().size)
        assertEquals(grid.indices.filter {grid[it]!='#'}.toSet(),coverage)
        val clues=p.data.getValue("clues").lines().associate {it.substringBefore('|') to it.substringAfter('|')}
        assertEquals(words.toSet(),clues.keys)
        for(word in words) {val answer=bank.answers.single {it.text==word};assertTrue(answer.clues.any {it.text==clues[word]&&it.sense==answer.sense})}
        val numbering=starts.sorted().withIndex().associate {it.value to it.index+1}
        val metadata=p.data.getValue("entryMetadata").lines().map {it.split('|')}
        assertEquals(identities.map {(start,dir)->"${numbering.getValue(start)}|$dir"}.toSet(),metadata.map {"${it[0]}|${it[1]}"}.toSet())
        val reached=mutableSetOf(coverage.first());val q=ArrayDeque<Int>();q.add(coverage.first())
        while(q.isNotEmpty()){val i=q.removeFirst();for(j in coverage)if(kotlin.math.abs(i/w-j/w)+kotlin.math.abs(i%w-j%w)==1&&reached.add(j))q.add(j)}
        assertEquals(coverage,reached)
        if(w==7)assertTrue("Larger boards need more clues",words.size>=9)
        assertEquals(CrosswordGenerator.semanticSignature(p),CrosswordGenerator.semanticSignature(p.copy(data=p.data.toMap())))
    }
}
