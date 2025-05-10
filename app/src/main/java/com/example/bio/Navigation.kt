package com.example.bio

object NavArguments {
    const val USER_ID = "userId"
    const val CONVERSATION_ID = "conversationId"
}

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val ONBOARDING_ROUTE = "onboarding" // Assuming you have this
    const val LOGIN_ROUTE = "login"
    const val SIGNUP_ROUTE = "signup"
    const val FORGET_PASSWORD_ROUTE = "forget_Password"

//     Conversation List Route (if you still have a dedicated screen for it)
     const val CONVERSATION_LIST_ROUTE_BASE = "conversationList"
     const val CONVERSATION_LIST_ROUTE = "$CONVERSATION_LIST_ROUTE_BASE/{${NavArguments.USER_ID}}"
     fun createConversationListRoute(userId: Int) = "$CONVERSATION_LIST_ROUTE_BASE/$userId"

    // Chat Route - This is the primary route we'll be using
    const val CHAT_ROUTE_BASE = "chat"
    // The route definition now includes userId and conversationId as arguments
    const val CHAT_ROUTE = "$CHAT_ROUTE_BASE/{${NavArguments.USER_ID}}/{${NavArguments.CONVERSATION_ID}}"

    // Helper function to create the chat route with arguments
    fun createChatRoute(userId: Int, conversationId: String): String {
        // It's crucial that the argument names here ("userId", "conversationId")
        // exactly match the names used in navArgument(...) in your NavHost.
        return "$CHAT_ROUTE_BASE/$userId/$conversationId"
    }

    const val SIMPLE_LANDING_ROUTE_BASE = "simple_landing"
    const val SIMPLE_LANDING_ROUTE = "$SIMPLE_LANDING_ROUTE_BASE/{${NavArguments.USER_ID}}"
    fun createSimpleLandingRoute(userId: Int) = "$SIMPLE_LANDING_ROUTE_BASE/$userId"

    // Example of another route if you had one:
    // const val SETTINGS_ROUTE = "settings/{${NavArguments.USER_ID}}"
    // fun createSettingsRoute(userId: Int) = "settings/$userId"
}





