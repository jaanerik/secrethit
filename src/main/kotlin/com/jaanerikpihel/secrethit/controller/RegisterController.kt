package com.jaanerikpihel.secrethit.controller

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.*
import com.jaanerikpihel.secrethit.model.GameState.Companion.INTRODUCTION
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionConnectedEvent
import java.time.LocalDateTime
import kotlin.math.floor


private val logger = KotlinLogging.logger {}
const val START_MSG = "\$start\$"
const val FASCIST = "Fascist"
const val LIBERAL = "Liberal"
const val HITLER = "Hitler"


@Controller
class RegisterController {

    @Autowired
    private var messagingTemplate: SimpMessageSendingOperations? = null
    private var allPlayers = emptyList<Player>().toMutableList()
    private val playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf()
    private var gameState = GameState()

    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) {
        if (message.name == START_MSG) {
            startGame(messageHeaders, sha)
        } else {
            addPlayer(message, sha, messageHeaders)
        }
    }

    @EventListener
    fun handleSessionConnectedEvent(event: SessionConnectedEvent) {
        val sha: SimpMessageHeaderAccessor = SimpMessageHeaderAccessor.wrap(event.message)
        logger.info { "Just logging ${sha.user!!.name}" } // Testing Listeners
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        logger.error { "Sending error" }
        return exception.message
    }


    private fun addPlayer(message: RegisterMessage, sha: SimpMessageHeaderAccessor, messageHeaders: MessageHeaders) {
        val player: Player
        if (allPlayers.map { it.name }.contains(message.name)) {
            //Update sessionId if name already exists. Simple fix for connectivity issues.
            player = allPlayers.find { it.name == message.name }!!
            player.sessionId = sha.user!!.name
        } else {
            player = Player(
                    sha.user!!.name,
                    message.name, ""
            )
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

    private fun startGame(messageHeaders: MessageHeaders, sha: SimpMessageHeaderAccessor) {
        logger.info { "Started game at ${LocalDateTime.now()}" }
        gameState = GameState(
                gameState = INTRODUCTION, players = allPlayers,
                president = allPlayers[floor(Math.random() * allPlayers.size).toInt()],
                extraInfo = allPlayers.map { it -> it.sessionId }.toString()
        )
        logger.info { "gameState=: ${gameState.toJSON()}, allPlayers: $allPlayers" }
        messagingTemplate!!.convertAndSend(
                "/topic/gameState",
                gameState.toJSON(),
                messageHeaders
        )
        allPlayers.zip(getShuffledRoles(allPlayers.size)).forEach {
            run {
                it.first.role = it.second
                messagingTemplate?.convertAndSendToUser(
                        it.first.sessionId,
                        "/queue/reply",
                        it.first);
            }
        }
        allPlayers.filter { it.role == FASCIST }.forEach {
            messagingTemplate?.convertAndSendToUser(
                    it.sessionId,
                    "/queue/reply",
                    Gson().toJson(mapOf(
                            "Introduction" to
                                    allPlayers.filter { it.role != LIBERAL }.map { mapOf(it.name to it.role) }
                    )));
        }
        allPlayers.forEach { logger.info { "${it.name} is ${it.role}" } }
    }
}
