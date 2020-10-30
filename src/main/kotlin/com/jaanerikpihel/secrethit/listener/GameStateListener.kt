package com.jaanerikpihel.secrethit.listener

import com.jaanerikpihel.secrethit.event.PresidentPowerEvent
import com.jaanerikpihel.secrethit.listener.GameStateListener.Companion.PEEK
import com.jaanerikpihel.secrethit.service.GameStateService
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GameStateListener (var gameStateService: GameStateService) {
    private val logger = KotlinLogging.logger {}

//    @EventListener
//    fun handleEvent(event: GameStartedEvent) {
//        logger.info { "Starting gaming in Listener!" }
//        gameStateService.startGame(event.messageHeaders)
//    }
//
//    @EventListener
//    fun handleEvent(event: PlayerAddedEvent) {
//        logger.info { "Added player in Listener!" }
//        gameStateService.addPlayer(event.registerMessage, event.sha, event.messageHeaders)
//    }

    @EventListener
    fun handleEvent(event: PresidentPowerEvent) {
        logger.info { "President power in Listener!" }
        val message = event.presidentPowerMessage.power
        when {
            message == PEEK -> gameStateService.sendVotingGameState()
            else -> TODO()
        }
    }

    companion object {
        private const val PEEK = "peekedCards"
    }
}