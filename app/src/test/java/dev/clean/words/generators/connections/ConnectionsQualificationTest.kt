package dev.clean.words.generators.connections

import dev.clean.words.games.connections.ConnectionsRules
import org.junit.Assert.*
import org.junit.Test

class ConnectionsQualificationTest {
    @Test fun actualBankTwoHundredSeedsAndRollingRecentWindow() {
        val bank = ConnectionsPilot.bank
        val generator = ConnectionsGenerator(bank)
        val profile = ConnectionsProfile(bank.contentSha256)
        for (rolling in listOf(false, true)) {
            val history = ArrayDeque<String>()
            val signatures = mutableSetOf<String>()
            val categories = sortedMapOf<String, Int>()
            val decoyPairs = sortedMapOf<String, Int>()
            val failures = sortedMapOf<String, Int>()
            val rejections = sortedMapOf<String, Int>()
            val times = mutableListOf<Long>()
            var worstSteps = 0
            var worstCandidates = 0
            var minDecoys = Int.MAX_VALUE
            repeat(200) { seed ->
                val recent = if (rolling) history.toSet() else emptySet()
                val start = System.nanoTime()
                val result = generator.generate(seed.toLong(), profile, recent)
                times.add((System.nanoTime() - start) / 1000)
                when (result) {
                    is ConnectionsGenerationResult.Success -> {
                        ConnectionsGeneratorTest().assertIndependentBoard(bank, result)
                        assertTrue(ConnectionsRules.validate(result.puzzle).isEmpty())
                        assertFalse(result.signature in recent)
                        assertEquals(result.signature, ConnectionsGenerator.semanticSignature(result.puzzle))
                        assertTrue(result.diagnostics.decoys.all { it.members.size == 3 && it.groupsSpanned >= 2 })
                        signatures.add(result.signature)
                        if (rolling) {history.addLast(result.signature);if(history.size > 100) history.removeFirst()}
                        result.diagnostics.chosenCategoryIds.forEach { categories[it] = (categories[it] ?: 0) + 1 }
                        val pair = result.diagnostics.decoys.map { it.relationId }.sorted().joinToString("+")
                        decoyPairs[pair] = (decoyPairs[pair] ?: 0) + 1
                        minDecoys = minOf(minDecoys, result.diagnostics.decoys.size)
                        worstSteps = maxOf(worstSteps, result.diagnostics.steps)
                        worstCandidates = maxOf(worstCandidates, result.diagnostics.candidates)
                        result.diagnostics.rejections.forEach { (k,v) -> rejections[k] = (rejections[k] ?: 0) + v }
                    }
                    is ConnectionsGenerationResult.NoCandidate -> {
                        failures[result.reason.name] = (failures[result.reason.name] ?: 0) + 1
                        worstSteps = maxOf(worstSteps,result.diagnostics.steps)
                        worstCandidates = maxOf(worstCandidates,result.diagnostics.candidates)
                        result.diagnostics.rejections.forEach { (k,v) -> rejections[k] = (rejections[k] ?: 0) + v }
                    }
                    is ConnectionsGenerationResult.Cancelled -> fail("Unexpected cancellation")
                }
            }
            times.sort()
            println("CONNECTIONS_QUALIFICATION rolling100=$rolling seeds=200 distinct=${signatures.size} failures=$failures minDecoys=$minDecoys worstSteps=$worstSteps worstCandidates=$worstCandidates p50us=${times[99]} p95us=${times[189]} maxus=${times.last()} categoryReuse=$categories decoyPairs=$decoyPairs rejections=$rejections bankHash=${bank.contentSha256} sourceHash=${bank.sourceSha256} humanReviews=0")
            assertTrue("Actual bank failed: $failures", failures.isEmpty())
        }
    }
}
