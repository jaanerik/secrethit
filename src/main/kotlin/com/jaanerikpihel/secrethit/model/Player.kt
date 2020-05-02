package com.jaanerikpihel.secrethit.model

import org.springframework.messaging.MessageHeaders

data class Player(
        var sessionId: String = "",
        var name: String = "",
        var role: String = "",
        var isDead: Boolean = false
)