package dev.clean.words.games.bee

import java.math.BigDecimal
import java.math.RoundingMode

/** Both milestones are independent. Percent is rounded half-up to two decimals for display. */
data class BeeOpeningProgress(
    val words: List<String>, val points: Int, val benchmark: Int, val percent: BigDecimal,
    val recorded: Boolean, val foundCount: Int, val completionGoal: Int, val allWordPoints: Int,
) {
    val goalReached: Boolean get() = foundCount >= completionGoal
}

/** Frozen, versioned quality decision. The production adapter supplies the owner's explicit choice. */
data class BeeOpeningQuality(val minimumRatioPermille: Int, val approvalVersion: String) {
    init {
        require(minimumRatioPermille in 0..1000)
        require(approvalVersion.isNotBlank())
    }
}

object BeeOpening {
    const val SCORING = "bee-opening10-v3"
    const val STATE = "bee-ordered-v1"
    const val LENGTH = 10
    const val BENCHMARK_POLICY = "best10-points-desc-word-asc-v1"
    const val ROUNDING = "percent-half-up-2dp-v1"

    fun bestTen(board: BeePuzzle, words: Set<String> = board.validWords): List<String> =
        words.sortedWith(compareByDescending<String> { BeeRules.points(board, it) }.thenBy { it }).take(LENGTH)

    fun benchmark(board: BeePuzzle, words: Set<String> = board.validWords): Int =
        bestTen(board, words).sumOf { BeeRules.points(board, it) }

    /** Input pins permit future frequency-backed sources without consulting them on resume. */
    fun freeze(board: BeePuzzle, pins: Map<String, String>, quality: BeeOpeningQuality): BeePuzzle {
        require(board.validWords.size >= LENGTH && board.goalWords.size >= LENGTH)
        val absolute = benchmark(board)
        val familiar = benchmark(board, board.goalWords)
        require(familiar.toLong() * 1000 >= absolute.toLong() * quality.minimumRatioPermille) { "Board fails approved opening quality" }
        val frozen = pins + mapOf(
            "openingLength" to LENGTH.toString(), "benchmarkPolicy" to BENCHMARK_POLICY, "statePolicy" to STATE,
            "benchmarkWords" to bestTen(board).joinToString(","), "benchmarkPoints" to absolute.toString(),
            "familiarBenchmarkPoints" to familiar.toString(), "wordTarget" to board.wordTarget.toString(),
            "scoreRounding" to ROUNDING, "minimumFamiliarRatioPermille" to quality.minimumRatioPermille.toString(),
            "qualityApprovalVersion" to quality.approvalVersion,
        )
        return board.copy(scoringVersion = SCORING, goalWords = board.goalWords.toSet(), validWords = board.validWords.toSet(), openingData = frozen.toMap()).also {
            require(BeeRules.validate(it).isEmpty()) { "Invalid frozen opening: ${BeeRules.validate(it)}" }
        }
    }

    fun puzzleErrors(board: BeePuzzle): List<String> = buildList {
        val data = board.openingData
        if (data["openingLength"] != LENGTH.toString()) add("Unsupported opening length")
        if (data["statePolicy"] != STATE) add("Unsupported frozen state policy")
        if (data["benchmarkPolicy"] != BENCHMARK_POLICY || data["scoreRounding"] != ROUNDING) add("Unsupported benchmark/rounding policy")
        if (board.validWords.size < LENGTH || board.goalWords.size < LENGTH) add("Opening requires ten accepted and familiar words")
        if (data["benchmarkWords"] != bestTen(board).joinToString(",") || data["benchmarkPoints"]?.toIntOrNull() != benchmark(board)) add("Inconsistent opening benchmark")
        if (data["familiarBenchmarkPoints"]?.toIntOrNull() != benchmark(board, board.goalWords)) add("Inconsistent familiar benchmark")
        if (data["wordTarget"]?.toIntOrNull() != board.wordTarget) add("Inconsistent frozen completion goal")
        listOf("generator", "profile", "profileThresholds", "sourceIndex", "sourceIndexSha256", "familiarityVersion", "qualityApprovalVersion").forEach {
            if (data[it].isNullOrBlank()) add("Missing opening pin: $it")
        }
        val ratio = data["minimumFamiliarRatioPermille"]?.toIntOrNull()
        if (ratio == null || ratio !in 0..1000) add("Invalid approved quality threshold")
        else if (benchmark(board, board.goalWords).toLong() * 1000 < benchmark(board).toLong() * ratio) add("Board fails frozen quality threshold")
    }

    /** No alphabetical migration or partial salvage: retain original map and block edits. */
    fun stateErrors(board: BeePuzzle, state: BeeState): List<String> = buildList {
        if (state.decodeError != null) add(state.decodeError)
        if (state.editRevision < 0 || state.editRevision == Long.MAX_VALUE) add("Invalid input edit revision")
        if (state.statePolicy != STATE && !(state.statePolicy == null && state.found.isEmpty() && state.acceptedOrder == null)) add("Unsupported or missing ordered state policy")
        val order = state.acceptedOrder
        if (order == null && state.found.isNotEmpty()) add("Accepted submission order is missing; it cannot be inferred")
        if (order != null) {
            if (order.size != order.distinct().size) add("Duplicate ordered submissions")
            if (order.toSet() != state.found) add("Ordered submissions disagree with found words")
            if (order.any { BeeRules.wordError(board, it) != null }) add("Unknown or invalid ordered submission")
        }
    }

    fun progress(board: BeePuzzle, state: BeeState): BeeOpeningProgress {
        require(board.opening) { "Opening progress requires v3" }
        require(BeeRules.validate(board).isEmpty()) { "Invalid opening puzzle" }
        require(stateErrors(board, state).isEmpty()) { stateErrors(board, state).joinToString() }
        val order = state.acceptedOrder.orEmpty()
        val opening = order.take(LENGTH)
        val points = opening.sumOf { BeeRules.points(board, it) }
        val denominator = board.openingData.getValue("benchmarkPoints").toInt()
        val percent = BigDecimal(points).multiply(BigDecimal(100)).divide(BigDecimal(denominator), 2, RoundingMode.HALF_UP)
        check(percent >= BigDecimal.ZERO && percent <= BigDecimal(100))
        return BeeOpeningProgress(opening, points, denominator, percent, opening.size == LENGTH,
            order.size, board.openingData.getValue("wordTarget").toInt(), order.sumOf { BeeRules.points(board, it) })
    }
}
