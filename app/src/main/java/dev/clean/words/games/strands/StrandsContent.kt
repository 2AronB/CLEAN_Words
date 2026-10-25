package dev.clean.words.games.strands

import dev.clean.words.core.PuzzleRecord

/** Ten original, deliberately compact development puzzles; not difficulty-calibrated. */
object StrandsContent {
    val puzzles: List<PuzzleRecord> = listOf(
        make("strands-garden-v1", "A little patch of green", 4, 6,
            listOf("GARDEN", "FLOWERS", "CARROTS", "SOIL"),
            listOf(0, 1, 7, 6, 12, 18, 19, 13, 14, 8, 2, 3, 9, 15, 16, 10, 4, 5, 11, 17, 23, 22, 21, 20)),
        make("strands-breakfast-v1", "Start the day", 4, 6,
            listOf("BREAKFAST", "TOAST", "EGGS", "CEREAL"),
            listOf(0, 6, 7, 1, 2, 3, 4, 5, 11, 10, 9, 8, 14, 13, 12, 18, 19, 20, 21, 15, 16, 17, 23, 22)),
        make("strands-weather-v1", "Look outside", 4, 6,
            listOf("WEATHER", "CLOUD", "RAIN", "SNOW", "WIND"),
            listOf(0, 1, 2, 3, 4, 5, 11, 17, 23, 22, 21, 15, 16, 10, 9, 8, 14, 20, 19, 13, 7, 6, 12, 18)),
        make("strands-music-v1", "Make a little noise", 4, 5,
            listOf("MUSIC", "PIANO", "FLUTE", "DRUMS"),
            listOf(0, 1, 2, 3, 4, 9, 8, 13, 14, 19, 18, 17, 16, 11, 12, 7, 6, 5, 10, 15)),
        make("strands-sports-v1", "Get in the game", 4, 6,
            listOf("SPORTS", "TENNIS", "SOCCER", "HOCKEY"),
            listOf(0, 6, 7, 13, 12, 18, 19, 20, 14, 15, 21, 22, 23, 17, 16, 10, 11, 5, 4, 3, 9, 8, 2, 1)),
        make("strands-colors-v1", "Bright ideas", 4, 6,
            listOf("COLORS", "PURPLE", "YELLOW", "ORANGE"),
            listOf(0, 6, 7, 13, 12, 18, 19, 20, 14, 15, 21, 22, 23, 17, 16, 10, 11, 5, 4, 3, 9, 8, 2, 1)),
        make("strands-kitchen-v1", "Set the table", 4, 6,
            listOf("KITCHEN", "PLATES", "SPOONS", "FORKS"),
            listOf(0, 1, 2, 3, 4, 5, 11, 17, 23, 22, 21, 20, 14, 15, 16, 10, 9, 8, 7, 6, 12, 18, 19, 13)),
        make("strands-ocean-v1", "Under the waves", 4, 5,
            listOf("OCEAN", "SHARK", "WHALE", "CORAL"),
            listOf(0, 1, 2, 3, 4, 9, 8, 7, 12, 11, 6, 5, 10, 15, 16, 17, 18, 13, 14, 19)),
        make("strands-forest-v1", "A walk in the woods", 4, 5,
            listOf("FOREST", "CEDAR", "BIRCH", "PINE"),
            listOf(0, 5, 6, 11, 10, 15, 16, 17, 12, 13, 18, 19, 14, 9, 4, 3, 8, 7, 2, 1)),
        make("strands-space-v1", "Beyond our sky", 4, 5,
            listOf("SPACE", "EARTH", "VENUS", "COMET"),
            listOf(0, 5, 10, 15, 16, 17, 12, 13, 18, 19, 14, 9, 4, 3, 8, 7, 2, 1, 6, 11)),
    )

    private fun make(id: String, clue: String, rows: Int, columns: Int, words: List<String>, route: List<Int>): PuzzleRecord {
        val letters = CharArray(rows * columns)
        var offset = 0
        val answers = words.map { word ->
            val path = route.subList(offset, offset + word.length).toList()
            path.forEachIndexed { index, cell -> letters[cell] = word[index] }
            offset += word.length
            StrandAnswer(word, path)
        }
        return StrandsPuzzle(rows, columns, letters.concatToString(), clue, words.first(), answers).snapshot(id)
    }
}
