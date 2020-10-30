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
        logger.info { "President power in Listener!" }
        when (event.presidentPowerMessage.power) {
            PEEK -> gameStateService.sendVotingGameState()
            LOYALTY -> gameStateService.sendPresidentLoyalty(event.presidentPowerMessage.theObject)
            LOYALTY_PEEKED -> gameStateService.sendVotingGameState()
            KILL_PLAYER -> {
                gameStateService.killPlayer(event.presidentPowerMessage.theObject)
                gameStateService.sendVotingGameState()
            }
            PICK_PRESIDENT -> {
                gameStateService.pickNextPresident(event.presidentPowerMessage.theObject)
                gameStateService.sendVotingGameState()
            }
            else -> TODO()
        }
    }

    companion object {
        const val PEEK = "peekedCards"
        const val KILL_PLAYER = "killPlayer"
        const val LOYALTY = "peekLoyalty"
        const val LOYALTY_PEEKED = "peekedLoyalty"
        const val PICK_PRESIDENT = "pickPresident"
    }
}