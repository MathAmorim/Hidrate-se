package com.example.base.util

object MotivationManager {

    private val phrasesGeneral = listOf(
        "Hora de se hidratar! 💧",
        "Seu corpo pede água...",
        "Beba água e sinta a diferença!",
        "Hidrate-se para ter mais energia! ⚡",
        "Um copo de água agora cairia bem.",
        "Não espere sentir sede para beber água.",
        "Água é vida! Mantenha-se hidratado.",
        "Sua pele agradece cada gole! ✨"
    )

    private val phrasesAlmostThere = listOf(
        "Quase lá! 💪",
        "Metade do caminho já foi! 🚀",
        "Falta pouco para a meta!",
        "Continue assim, você está indo bem!",
        "Só mais alguns goles para a vitória!",
        "A meta está logo ali! 🎯",
        "Mantenha o ritmo!",
        "Você está brilhando! ✨"
    )

    private val phrasesGoalReached = listOf(
        "Meta Batida! 🎉",
        "Você venceu a sede hoje! 🔥",
        "Parabéns! Hidratação nota 10! 🌟",
        "Missão cumprida! 🏆",
        "Você é incrível! Meta alcançada.",
        "Seu corpo está feliz e hidratado! 💙",
        "Show de hidratação! 🌊",
        "Amanhã tem mais! Bom trabalho."
    )

    fun getPhrase(progressPercentage: Int): String {
        return when {
            progressPercentage >= 100 -> phrasesGoalReached.random()
            progressPercentage >= 50 -> phrasesAlmostThere.random()
            else -> phrasesGeneral.random()
        }
    }
}
