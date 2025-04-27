package com.example.bio

object NavArguments {
    const val USER_ID = "userId"
    const val CONVERSATION_ID = "conversationId"
}

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val ONBOARDING_ROUTE = "onboarding"

    // --- Add Login/Signup Routes ---
    const val LOGIN_ROUTE = "login"
    const val SIGNUP_ROUTE = "signup"
    const val FORGET_PASSWORD_ROUTE = "forget_Password"
    // --- End Login/Signup Routes ---

    // Conversation List Route
    const val CONVERSATION_LIST_ROUTE_BASE = "conversationList"
    const val CONVERSATION_LIST_ROUTE = "$CONVERSATION_LIST_ROUTE_BASE/{${NavArguments.USER_ID}}"
    fun createConversationListRoute(userId: Int) = "$CONVERSATION_LIST_ROUTE_BASE/$userId"

    // Chat Route
    const val CHAT_ROUTE_BASE = "chat"
    const val CHAT_ROUTE = "$CHAT_ROUTE_BASE/{${NavArguments.USER_ID}}/{${NavArguments.CONVERSATION_ID}}"
    fun createChatRoute(userId: Int, conversationId: String) =
        "$CHAT_ROUTE_BASE/$userId/$conversationId"

    // Settings Route (Placeholder for later)
    // const val SETTINGS_ROUTE = "settings/{userId}"
}