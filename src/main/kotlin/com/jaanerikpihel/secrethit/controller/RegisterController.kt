package com.jaanerikpihel.secrethit.controller

import com.jaanerikpihel.secrethit.model.GameState
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER
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
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}
const val START_MSG = "\$start\$"

@Controller
class GreetingController {

    @Autowired
    private var messagingTemplate: SimpMessageSendingOperations? = null
    private var allPlayers = emptyList<Player>().toMutableList()
    private val playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf<String, MessageHeaders>()
    var gameState = GameState(REGISTER)

    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            messageHeaders: MessageHeaders
    ) {
        if (message.name == START_MSG) {
            logger.info { "Started game at ${LocalDateTime.now()}" }
            gameState = GameState(allPlayers.size)
            messagingTemplate!!.convertAndSend(
                    "/topic/registrations",
                    START_MSG,
                    messageHeaders
            )
            allPlayers.zip(getShuffledRoles(allPlayers.size)).forEach { run {
                it.first.role = it.second
                messagingTemplate!!.convertAndSendToUser(
                        it.first.sessionId,
                        "/queue/reply",
                        it.first,
                        playerNameToHeaders[it.first.name]!!
                )
            } }
            allPlayers.forEach { logger.info {"${it.name} is ${it.role}" } }
        } else {
            var player: Player = Player()
            if (allPlayers.map { it.name }.contains(message.name)) {
                //Update sessionId if name already exists. Simple fix for connectivity issues.
                player = allPlayers.find { it.name == message.name }!!
                player.sessionId = messageHeaders["simpSessionId"].toString();
                playerNameToHeaders[player.name] = messageHeaders
            } else {
                player = Player(messageHeaders["simpSessionId"].toString(), message.name, "", false)
                playerNameToHeaders[player.name] = messageHeaders
                allPlayers.add(player)
            }
            logger.info { "Registered a new player: $player" }
            messagingTemplate!!.convertAndSend(
                    "/topic/registrations",
                    allPlayers,
                    messageHeaders
            )
        }
    }

    private fun getShuffledRoles(size: Int): List<String> {
        val roles = when(size) {
            5 -> howManyLibFacWithOutHit(3,1)
            6 -> howManyLibFacWithOutHit(4, 1)
            7 -> howManyLibFacWithOutHit(4, 2)
            8 -> howManyLibFacWithOutHit(5, 2)
            9 -> howManyLibFacWithOutHit(5, 3)
            10 -> howManyLibFacWithOutHit(6, 3)
            else -> throw IllegalArgumentException("Not enough or too many players!")
        }
        return roles.shuffled()
    }

    private fun howManyLibFacWithOutHit(libCount: Int, facCount: Int): MutableList<String> {
        return (
                MutableList(libCount) { "Liberal" } + MutableList(facCount) { "Fascist" } + listOf("Hitler")
                ) as MutableList<String>
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        return exception.message
    }
}
