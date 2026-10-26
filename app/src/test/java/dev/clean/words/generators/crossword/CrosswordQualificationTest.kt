package dev.clean.words.generators.crossword

import org.junit.Assert.*
import org.junit.Test

class CrosswordQualificationTest {
    @Test fun realPilotHundredSeedsPerSizeAndRollingHundred() {
        val g=CrosswordGenerator(BundledCrosswordBank.bank)
        for((width,rolling) in listOf(5 to false,7 to false,7 to true)) {
            val history=ArrayDeque<String>();val fills=mutableSetOf<String>();val outputs=mutableSetOf<String>();val layouts=mutableSetOf<String>();val answers=mutableMapOf<String,Int>();val clues=mutableMapOf<String,Int>();val failures=mutableMapOf<String,Int>();val rejections=sortedMapOf<String,Int>();val times=mutableListOf<Long>();var worst=0;var minEntries=100
            repeat(100) {seed->
                val recent=if(rolling)history.toSet() else emptySet();val start=System.nanoTime()
                val result=g.generate(seed.toLong(),CrosswordProfile(width,width),recent)
                times.add((System.nanoTime()-start)/1000)
                val diagnostics=when(result){is CrosswordGenerationResult.Success->result.diagnostics;is CrosswordGenerationResult.NoCandidate->result.diagnostics;is CrosswordGenerationResult.Cancelled->result.diagnostics}
                diagnostics.rejections.forEach{(key,count)->rejections[key]=(rejections[key]?:0)+count}
                when(result) {
                    is CrosswordGenerationResult.Success -> {
                        CrosswordGeneratorTest().assertIndependent(result.puzzle)
                        assertFalse(result.signature in recent)
                        if(rolling && history.isNotEmpty())assertTrue(history.first().split('|')[4].split(',').toSet().intersect(result.signature.split('|')[4].split(',').toSet()).isEmpty())
                        outputs.add(result.signature);fills.add(result.puzzle.data.getValue("grid"));layouts.add(result.puzzle.data.getValue("templateId"));worst=maxOf(worst,result.diagnostics.steps)
                        val entries=result.puzzle.data.getValue("entryMetadata").lines().map {it.split('|')};minEntries=minOf(minEntries,entries.size)
                        entries.forEach {answers[it[2]]=(answers[it[2]]?:0)+1;clues[it[3]]=(clues[it[3]]?:0)+1}
                        if(rolling){history.addFirst(result.signature);if(history.size>100)history.removeLast()}
                    }
                    is CrosswordGenerationResult.NoCandidate -> {failures[result.reason.name]=(failures[result.reason.name]?:0)+1;worst=maxOf(worst,result.diagnostics.steps)}
                    is CrosswordGenerationResult.Cancelled -> fail("Unexpected cancellation")
                }
            }
            times.sort()
            println("CROSSWORD_QUALIFICATION width=$width rolling100=$rolling seeds=100 outputs=${outputs.size} fills=${fills.size} layouts=$layouts uniqueAnswers=${answers.size} uniqueClues=${clues.size} minEntries=$minEntries worstSteps=$worst failures=$failures p50us=${times[49]} p95us=${times[94]} maxus=${times.last()} rejections=$rejections answerReuse=$answers clueReuse=$clues humanReviews=0 source=${BundledCrosswordBank.sourceSha256}")
            assertTrue("Real bank failed: $failures",failures.isEmpty());assertTrue(fills.size>=50);assertTrue(layouts.size>=10)
        }
    }
}
