package com.example.data

data class PresetQuestion(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String,
    val category: String, // "GK", "Movies", "Gaming", "Sports", "Science", "Telugu"
    val theme: String = "General" // "General", "Gaming", "Telugu"
)

object PresetQuizzes {
    val questions = listOf(
        // --- GAMING QUESTIONS (Battle Royale/Free Fire) ---
        PresetQuestion(
            question = "Which active character in Free Fire drops a heart-beat dome block, healing allies inside?",
            optionA = "Chrono",
            optionB = "D-Bee",
            optionC = "Dimitri",
            optionD = "DJ Alok",
            correctOption = "C",
            explanation = "Dimitri regenerates allies and lets them self-revive inside his healing zone!",
            category = "Gaming",
            theme = "Gaming"
        ),
        PresetQuestion(
            question = "What is the name of the iconic, high-tier air-drop only Sniper Rifle in PUBG?",
            optionA = "AWM",
            optionB = "Kar98k",
            optionC = "M24",
            optionD = "SKS",
            correctOption = "A",
            explanation = "AWM (Arctic Warfare Magnum) is a legendary air-drop weapon that deals immense 1-shot headshot damage.",
            category = "Gaming",
            theme = "Gaming"
        ),
        PresetQuestion(
            question = "Which battle royale pioneered the building mechanism using wood, brick, and metal?",
            optionA = "Free Fire",
            optionB = "Apex Legends",
            optionC = "Fortnite",
            optionD = "Call of Duty Mobile",
            correctOption = "C",
            explanation = "Fortnite is famous for its vertical building dynamic during explosive build battles!",
            category = "Gaming",
            theme = "Gaming"
        ),
        PresetQuestion(
            question = "In Free Fire, what is the protective wall deployed instantly with a grenade called?",
            optionA = "Force Shield",
            optionB = "Gloo Wall",
            optionC = "Iron Cover",
            optionD = "Titan Wall",
            correctOption = "B",
            explanation = "Gloo Wall grenades are vital items used for instant protection in close-range combat.",
            category = "Gaming",
            theme = "Gaming"
        ),
        PresetQuestion(
            question = "What is the ultimate victory phrase shown upon winning a match in PUBG?",
            optionA = "BOOYAH!",
            optionB = "Winner Winner Chicken Dinner",
            optionC = "Victory Royale",
            optionD = "Apex Champions",
            correctOption = "B",
            explanation = "Winning a PUBG battle royale greets you with the viral statement 'Winner Winner Chicken Dinner!'",
            category = "Gaming",
            theme = "Gaming"
        ),

        // --- TELUGU QUESTIONS ---
        PresetQuestion(
            question = "భారతీయ చలనచిత్ర రంగాన్ని షేక్ చేసిన 'ఆర్.ఆర్.ఆర్' (RRR) సినిమా దర్శకుడు ఎవరు?",
            optionA = "త్రివిక్రమ్ శ్రీనివాస్",
            optionB = "ఎస్.ఎస్. రాజమౌళి",
            optionC = "సుకుమార్",
            optionD = "ప్రశాంత్ నీల్",
            correctOption = "B",
            explanation = "ఎస్.ఎస్. రాజమౌళి గారు RRR సినిమా ద్వారా ఆస్కార్ అవార్డును సాధించి తెలుగు ఖ్యాతిని పెంచారు!",
            category = "Telugu",
            theme = "Telugu"
        ),
        PresetQuestion(
            question = "తెలుగు సంవత్సరాలలో మొదటి సంవత్సరానికి గల పేరు ఏమిటి?",
            optionA = "విక్రమ",
            optionB = "శ్రీముఖ",
            optionC = "ప్రభవ",
            optionD = "అక్షయ",
            correctOption = "C",
            explanation = "తెలుగు పంచాంగం ప్రకారం, 60 సంవత్సరాల చక్రంలో మొదటిది 'ప్రభవ' నామ సంవత్సరం.",
            category = "Telugu",
            theme = "Telugu"
        ),
        PresetQuestion(
            question = "ఆంధ్రప్రదేశ్ రాష్ట్ర అధికారిక క్రీడ ఏది?",
            optionA = "కబడ్డీ",
            optionB = "క్రికెట్",
            optionC = "హాకీ",
            optionD = "కో కో",
            correctOption = "A",
            explanation = "ఆంధ్రప్రదేశ్ రాష్ట్ర అధికారిక క్రీడగా కబడ్డీ (కబాడి) గుర్తింపు పొందింది.",
            category = "Telugu",
            theme = "Telugu"
        ),
        PresetQuestion(
            question = "తెలుగు భాషా దినోత్సవాన్ని ఎవరి జన్మదినం సందర్భంగా జరుపుకుంటాం?",
            optionA = "సి. నారాయణ రెడ్డి",
            optionB = "గిడుగు వెంకట రామమూర్తి",
            optionC = "గురజాడ అప్పారావు",
            optionD = "శ్రీశ్రీ",
            correctOption = "B",
            explanation = "గిడుగు వెంకట రామమూర్తి గారి పుట్టినరోజైన ఆగస్టు 29ని తెలుగు వ్యవహారిక భాషా దినోత్సవంగా జరుపుకుంటాము.",
            category = "Telugu",
            theme = "Telugu"
        ),
        PresetQuestion(
            question = "ఏ తెలుగు నగరానికి 'నిజాంల నగరం' లేదా 'ముత్యాల నగరం' అని పేరు ఉంది?",
            optionA = "విజయవాడ",
            optionB = "విశాఖపట్నం",
            optionC = "హైదరాబాద్",
            optionD = "వరంగల్",
            correctOption = "C",
            explanation = "చారిత్రకంగా నిజాం రాజుల పాలన మరియు ముత్యాల వర్తకానికి హైదరాబాద్ నగరం ప్రసిద్ధి చెందింది.",
            category = "Telugu",
            theme = "Telugu"
        ),

        // --- GENERAL GK & SCIENCE QUESTIONS ---
        PresetQuestion(
            question = "Which planet is commonly known as the 'Red Planet' of our Solar System?",
            optionA = "Mars",
            optionB = "Venus",
            optionC = "Jupiter",
            optionD = "Saturn",
            correctOption = "A",
            explanation = "Mars gets its reddish color from iron oxide (rust) covering its dusty surface.",
            category = "GK",
            theme = "General"
        ),
        PresetQuestion(
            question = "What is the chemical symbol for gold on the periodic table?",
            optionA = "Gd",
            optionB = "Go",
            optionC = "Au",
            optionD = "Ag",
            correctOption = "C",
            explanation = "Au is derived from 'aurum', the Latin word for shining dawn/gold.",
            category = "Science",
            theme = "General"
        ),
        PresetQuestion(
            question = "Which country won the FIFA Men's World Cup in Qatar in 2022?",
            optionA = "France",
            optionB = "Brazil",
            optionC = "Argentina",
            optionD = "Croatia",
            correctOption = "C",
            explanation = "Argentina, led by Lionel Messi, emerged victorious after a dramatic final against France.",
            category = "Sports",
            theme = "General"
        ),
        PresetQuestion(
            question = "What is the premier acting award ceremony in Hollywood that presents golden Oscar statuettes?",
            optionA = "Emmy Awards",
            optionB = "Academy Awards",
            optionC = "Grammy Awards",
            optionD = "Tony Awards",
            correctOption = "B",
            explanation = "The Academy Awards (locally called Oscars) celebrate exceptional cinematic achievements.",
            category = "Movies",
            theme = "General"
        ),
        PresetQuestion(
            question = "How many bones are there in an adult human body?",
            optionA = "108",
            optionB = "206",
            optionC = "312",
            optionD = "270",
            correctOption = "B",
            explanation = "While humans are born with around 270 bones, many fuse during growth, leaving 206 bones in an adult.",
            category = "Science",
            theme = "General"
        ),
        PresetQuestion(
            question = "Which historical monument is famously situated on the banks of river Yamuna in Agra?",
            optionA = "Taj Mahal",
            optionB = "Red Fort",
            optionC = "Qutub Minar",
            optionD = "Hawa Mahal",
            correctOption = "A",
            explanation = "The Taj Mahal was commissioned by Shah Jahan in 1632 to house the tomb of his favorite wife, Mumtaz Mahal.",
            category = "GK",
            theme = "General"
        ),
        PresetQuestion(
            question = "Who directed the sci-fi epic movie 'Interstellar' and the 'Dark Knight' trilogy?",
            optionA = "Steven Spielberg",
            optionB = "Christopher Nolan",
            optionC = "James Cameron",
            optionD = "Quentin Tarantino",
            correctOption = "B",
            explanation = "Christopher Nolan is highly acclaimed for his mind-bending narratives and spectacular practical effects.",
            category = "Movies",
            theme = "General"
        ),
        PresetQuestion(
            question = "In chess, which piece is the only one that can leap or jump over other pieces?",
            optionA = "Bishop",
            optionB = "Knight",
            optionC = "Rook",
            optionD = "Queen",
            correctOption = "B",
            explanation = "The Knight moves in an 'L' shape and can jump over obstacles on the board.",
            category = "Sports",
            theme = "General"
        ),
        PresetQuestion(
            question = "Deficiency of which vitamin leads to the disease named Scurvy?",
            optionA = "Vitamin A",
            optionB = "Vitamin B12",
            optionC = "Vitamin C",
            optionD = "Vitamin D",
            correctOption = "C",
            explanation = "Scurvy is caused by severe lack of Vitamin C, which is essential to produce collagen inside our tissues.",
            category = "Science",
            theme = "General"
        ),
        PresetQuestion(
            question = "Which active volcano is considered the tallest and most famous in Japan?",
            optionA = "Mount Fuji",
            optionB = "Mount Vesuvius",
            optionC = "Mount Etna",
            optionD = "Kilauea",
            correctOption = "A",
            explanation = "Mount Fuji is an symmetrical composite cone and an iconic symbol representing Japan.",
            category = "GK",
            theme = "General"
        )
    )
}
