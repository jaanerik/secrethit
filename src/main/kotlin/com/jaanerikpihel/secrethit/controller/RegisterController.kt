package com.jaanerikpihel.secrethit.controller

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.Player
import com.jaanerikpihel.secrethit.model.RegisterMessage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Controller
class GreetingController {

    @Autowired
    private var messagingTemplate: SimpMessageSendingOperations? = null

    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            messageHeaders: MessageHeaders //			SimpMessageHeaderAccessor headerAccessor
            //			Principal principal
    ) {
        logger.info { messageHeaders }
        val player = Player(messageHeaders["simpSessionId"].toString(), message.name, "", false)
        messagingTemplate!!.convertAndSendToUser(
                messageHeaders["simpSessionId"].toString(),
                "/queue/reply",
                player,
                messageHeaders
        )
        messagingTemplate!!.convertAndSend(
                "/topic/registrations",
                player,
                messageHeaders
        )
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        return exception.message
    }
}
