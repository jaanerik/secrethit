package com.jaanerikpihel.secrethit.controller

import com.jaanerikpihel.secrethit.model.GameState
import com.jaanerikpihel.secrethit.model.GameState.Companion.INTRODUCTION
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER
import com.jaanerikpihel.secrethit.model.GameState.Companion.VOTING
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
import org.springframework.web.bind.annotation.CrossOrigin
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import kotlin.math.floor

private val logger = KotlinLogging.logger {}
const val START_MSG = "\$start\$"

@Controller
class GreetingController {

    @Autowired
    private var messagingTemplate: SimpMessageSendingOperations? = null
    private var allPlayers = emptyList<Player>().toMutableList()
    private val playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf<String, MessageHeaders>()
    var gameState = GameState()

    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            messageHeaders: MessageHeaders
    ) {
        if (message.name == START_MSG) {
            logger.info { "Started game at ${LocalDateTime.now()}" }
            gameState = GameState(
                    gameState = INTRODUCTION, players = allPlayers,
                    president = allPlayers[floor(Math.random() * allPlayers.size).toInt()]
            )
            logger.info { "This is gameState sting: ${gameState.toJSON()}, allPlayers string: $allPlayers" }
            messagingTemplate!!.convertAndSend(
                    "/topic/gameState",
                    gameState.toJSON(),
                    messageHeaders
            )
            allPlayers.zip(getShuffledRoles(allPlayers.size)).forEach { run {
                it.first.role = it.second
                logger.info { "Sent message to: ${it.first}" }
                messagingTemplate!!.convertAndSendToUser(
                        it.first.sessionId,
                        "/queue/reply",
                        it.first,
                        playerNameToHeaders[it.first.name]!!
                )
            } }
            allPlayers.forEach { logger.info {"${it.name} is ${it.role}" } }
        } else {
            var player: Player
            if (allPlayers.map { it.name }.contains(message.name)) {
                //Update sessionId if name already exists. Simple fix for connectivity issues.
                player = allPlayers.find { it.name == message.name }!!
                player.sessionId = messageHeaders["simpSessionId"].toString();
                playerNameToHeaders[player.name] = messageHeaders
            } else {
                player = Player(messageHeaders["simpSessionId"].toString(), message.name, "")
                playerNameToHeaders[player.name] = messageHeaders
                allPlayers.add(player)
            }
            logger.info { "Registered a new player: $player" }
            gameState = GameState(gameState = REGISTER, players = allPlayers)
            messagingTemplate!!.convertAndSend(
                    "/topic/gameState",
                    gameState.toJSON(),
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
