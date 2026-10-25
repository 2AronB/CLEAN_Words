package dev.clean.words.games.connections

import dev.clean.words.core.PuzzleRecord

internal object ConnectionsContent {
    private fun group(label: String, words: String, explanation: String) = ConnectionsGroup(label, words.split('|'), explanation)
    private fun board(number: Int, vararg groups: ConnectionsGroup) = PuzzleRecord(
        id = "connections-original-$number", title = "Original mix $number", data = buildMap {
            put("origin", "CLEAN Words original authored starter content, 2026-10-25; see CONTENT.md")
            groups.forEachIndexed { i, g ->
                put("group.$i.label", g.label); put("group.$i.words", g.words.joinToString("|")); put("group.$i.explanation", g.explanation)
            }
        })

    val puzzles = listOf(
        board(1,
            group("Pasta shapes", "PENNE|FUSILLI|ORZO|RIGATONI", "These are names of pasta shapes; orzo resembles grains of rice."),
            group("Chess pieces", "ROOK|BISHOP|KNIGHT|PAWN", "Each is a type of piece on a chessboard."),
            group("Cloud types", "CIRRUS|CUMULUS|STRATUS|NIMBOSTRATUS", "Each names a recognized type of cloud."),
            group("Metric prefixes", "MILLI|CENTI|KILO|MEGA", "These prefixes indicate powers of ten in metric units.")),
        board(2,
            group("Sewing equipment", "NEEDLE|THIMBLE|BOBBIN|SEAM RIPPER", "These tools or components are used when sewing."),
            group("Percussion instruments", "GONG|CYMBAL|TAMBOURINE|MARACA", "Each instrument makes sound when struck or shaken."),
            group("Objects in space", "COMET|ASTEROID|NEBULA|GALAXY", "These are astronomical objects or collections of matter in space."),
            group("Metals used in jewelry", "GOLD|SILVER|PLATINUM|TITANIUM", "All four metals are used to make jewelry.")),
        board(3,
            group("Can follow FIRE", "FLY|PLACE|WOOD|PROOF", "Firefly, fireplace, firewood and fireproof begin with FIRE."),
            group("Can follow BOOK", "SHELF|WORM|MARK|CASE", "Bookshelf, bookworm, bookmark and bookcase begin with BOOK."),
            group("Can follow TOOTH", "BRUSH|PASTE|ACHE|PICK", "Toothbrush, toothpaste, toothache and toothpick begin with TOOTH."),
            group("Can follow RAIN", "BOW|COAT|DROP|FALL", "Rainbow, raincoat, raindrop and rainfall begin with RAIN.")),
        board(4,
            group("Ballet movements", "PLIE|JETE|PIROUETTE|ARABESQUE", "These are ballet positions or movements, shown without accent marks."),
            group("Cooking methods", "BRAISE|SAUTE|SIMMER|ROAST", "Each describes a way of cooking food."),
            group("Polygon names", "TRIANGLE|PENTAGON|HEXAGON|OCTAGON", "These polygons have three, five, six and eight sides respectively."),
            group("Poetic forms", "SONNET|HAIKU|LIMERICK|SESTINA", "Each names a form of poem.")),
        board(5,
            group("Palindromes", "LEVEL|RADAR|CIVIC|KAYAK", "Each word reads the same forward and backward."),
            group("Months with 30 days", "APRIL|JUNE|SEPTEMBER|NOVEMBER", "These are the four Gregorian calendar months with exactly thirty days."),
            group("Standard playing-card suits", "HEARTS|DIAMONDS|CLUBS|SPADES", "These are the four suits in a standard French-suited deck."),
            group("Shades of blue", "NAVY|AZURE|COBALT|CERULEAN", "Each can name a blue color.")),
        board(6,
            group("Can follow SNOW", "BALL|FLAKE|MAN|PLOW", "Snowball, snowflake, snowman and snowplow begin with SNOW."),
            group("Bicycle components", "PEDAL|SADDLE|HANDLEBAR|SPOKE", "Each is a component of a bicycle."),
            group("Geometric solids", "CUBE|SPHERE|CONE|PYRAMID", "Each names a three-dimensional geometric solid."),
            group("End in NESS", "KINDNESS|DARKNESS|STILLNESS|FAIRNESS", "Each word ends with the suffix NESS.")),
        board(7,
            group("Knots", "BOWLINE|SHEET BEND|CLOVE HITCH|REEF KNOT", "Each names a knot used to secure or join rope."),
            group("Units of digital information", "BIT|BYTE|KILOBYTE|GIGABYTE", "These units measure digital information or storage."),
            group("Woodwind instruments", "FLUTE|CLARINET|OBOE|BASSOON", "These are orchestral woodwind instruments; a flute need not be made of wood."),
            group("Terms for groups of animals", "FLOCK|HERD|SWARM|POD", "These collective nouns can describe groups of animals.")),
        board(8,
            group("Japanese foods", "SUSHI|RAMEN|TEMPURA|MOCHI", "These are dishes or foods associated with Japanese cuisine."),
            group("Punctuation marks", "COMMA|COLON|SEMICOLON|APOSTROPHE", "Each names a punctuation mark."),
            group("Parts of a tree", "ROOT|TRUNK|BRANCH|BARK", "Each names a part or outer covering of a tree."),
            group("Timekeeping devices", "SUNDIAL|HOURGLASS|STOPWATCH|CLOCK", "These devices measure elapsed time or indicate the time of day.")),
        board(9,
            group("Silent initial letter", "GNOME|WRIST|PSALM|KNIFE", "In standard English pronunciation, each word's first written letter is silent."),
            group("English number homophones", "WON|TOO|FOR|ATE", "These sound like ONE, TWO, FOUR and EIGHT in common English pronunciation."),
            group("Names of Greek letters", "ALPHA|BETA|GAMMA|DELTA", "These are the first four letters of the Greek alphabet."),
            group("Roman numeral symbols", "I|V|X|L", "These symbols represent one, five, ten and fifty in Roman numerals.")),
        board(10,
            group("Can precede HOUSE", "GREEN|LIGHT|WARE|BIRD", "Greenhouse, lighthouse, warehouse and birdhouse end with HOUSE."),
            group("Can precede LINE", "DEAD|HEAD|OUT|PIPE", "Deadline, headline, outline and pipeline end with LINE."),
            group("Can precede ROOM", "BED|BATH|CLASS|SHOW", "Bedroom, bathroom, classroom and showroom end with ROOM."),
            group("Can precede BOARD", "SKATE|SURF|KEY|DASH", "Skateboard, surfboard, keyboard and dashboard end with BOARD."))
    )
}
