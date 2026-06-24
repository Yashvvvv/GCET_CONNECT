package app.recruit.collegebot.domain.model

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,   // always 4 items
    val correctIndex: Int,       // 0-based
    val explanation: String
)
