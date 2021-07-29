package com.jaanerikpihel.secrethit.listener

import com.jaanerikpihel.secrethit.event.PresidentPowerEvent
import com.jaanerikpihel.secrethit.service.GameStateService
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GameStateListener (private var gameStateService: GameStateService) {
    private val logger = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: PresidentPowerEvent) {
        logger.debug { "President power in Listener!" }
        when (event.presidentPowerMessage.power) {
            PEEK -> gameStateService.sendVotingGameState()
            PEEK_LOYALTY -> gameStateService.sendPresidentLoyalty(event.presidentPowerMessage.theObject)
            LOYALTY_PEEKED -> gameStateService.sendVotingGameState()
            KILLED_PLAYER -> {
                gameStateService.killPlayer(event.presidentPowerMessage.theObject)
                gameStateService.sendVotingGameState()
            }
            PICKED_PRESIDENT -> {
                gameStateService.pickNextPresident(event.presidentPowerMessage.theObject)
                gameStateService.sendVotingGameState()
            }
            else -> {
                logger.error { "Weird presidential power: ${event.presidentPowerMessage.power}" }
            }
        }
    }

    companion object {
        const val PEEK = "peekedCards"
        const val KILLED_PLAYER = "killedPlayer"
        const val PEEK_LOYALTY = "peekLoyalty"
        const val LOYALTY_PEEKED = "peekedLoyalty"
        const val PICK_PRESIDENT = "pickPresident"
        const val PICKED_PRESIDENT = "pickedPresident"
    }
}