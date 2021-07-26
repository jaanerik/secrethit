package com.jaanerikpihel.secrethit.controller

import com.jaanerikpihel.secrethit.event.PresidentPowerEvent
import com.jaanerikpihel.secrethit.model.*
import com.jaanerikpihel.secrethit.service.GameStateService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin


private val logger = KotlinLogging.logger {}
const val START_MSG = "\$start\$"
const val RESET_MSG = "\$reset\$"

@Controller
class Controller(private val gameStateService: GameStateService, private val publisher: ApplicationEventPublisher) {

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageMapping("/voting")
    fun processMessageFromClient(
            @Payload message: VotingMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) = gameStateService.handleVoteOrChancellorCandidate(sha, message)

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) = when (message.name) {
        START_MSG -> gameStateService.startGame(messageHeaders)
        RESET_MSG -> gameStateService.resetGame()
        else -> gameStateService.addPlayer(message, sha, messageHeaders)
    }

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageMapping("/discard")
    fun processMessageFromClient(
            @Payload message: DiscardMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) = gameStateService.handleDiscard(message)

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageMapping("/reset")
    fun processMessageFromClient(
            @Payload message: ResetMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) = if (message.message == RESET_MSG) gameStateService.resetGame() else logger.info { "Wrong arg reset" }

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageMapping("/presidentPower")
    fun processMessageFromClient(
            @Payload message: PresidentPowerMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) = publisher.publishEvent(PresidentPowerEvent(this::class, message))

    @CrossOrigin(origins = ["http://localhost:3000"])
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        logger.error { "Sending error ${exception.message}" }
        return exception.message
    }
}
