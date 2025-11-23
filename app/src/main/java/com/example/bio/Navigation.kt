package com.example.bio

object NavArguments {
    const val USER_ID = "userId"
    const val CONVERSATION_ID = "conversationId"
    const val PDF_FILENAME = "pdfFilename" // آرگومان برای نام فایل
}

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val ONBOARDING_ROUTE = "onboarding"
    const val LOGIN_ROUTE = "login"
    const val SIGNUP_ROUTE = "signup"
    const val FORGET_PASSWORD_ROUTE = "forget_Password"
    const val SUBSCRIPTION_ROUTE = "subscription"
    // Conversation List Route
    const val CONVERSATION_LIST_ROUTE_BASE = "conversationList"
    const val CONVERSATION_LIST_ROUTE = "$CONVERSATION_LIST_ROUTE_BASE/{${NavArguments.USER_ID}}"
    fun createConversationListRoute(userId: Int) = "$CONVERSATION_LIST_ROUTE_BASE/$userId"

    // Chat Route
    const val CHAT_ROUTE_BASE = "chat"
    const val CHAT_ROUTE = "$CHAT_ROUTE_BASE/{${NavArguments.USER_ID}}/{${NavArguments.CONVERSATION_ID}}"
    fun createChatRoute(userId: Int, conversationId: String): String {
        return "$CHAT_ROUTE_BASE/$userId/$conversationId"
    }

    const val SIMPLE_LANDING_ROUTE_BASE = "simple_landing"
    const val SIMPLE_LANDING_ROUTE = "$SIMPLE_LANDING_ROUTE_BASE/{${NavArguments.USER_ID}}"
    fun createSimpleLandingRoute(userId: Int) = "$SIMPLE_LANDING_ROUTE_BASE/$userId"

    // --- تغییرات نهایی بخش آزمون ---

    // 1. این مسیر، نقطه ورود به بخش آزمون است و لیست PDF ها را نشان می‌دهد
    const val QUIZ_ENTRY_ROUTE = "quiz"

    // 2. این مسیر داخلی برای نمایش خود آزمون استفاده می‌شود
    private const val QUIZ_SCREEN_ROUTE_BASE = "quiz_screen"
    const val QUIZ_SCREEN_ROUTE = "$QUIZ_SCREEN_ROUTE_BASE/{${NavArguments.PDF_FILENAME}}"
    fun createQuizRoute(pdfFilename: String) = "$QUIZ_SCREEN_ROUTE_BASE/$pdfFilename"
}