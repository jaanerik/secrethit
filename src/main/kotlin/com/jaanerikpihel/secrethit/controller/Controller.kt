package com.jaanerikpihel.secrethit.controller

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.*
import com.jaanerikpihel.secrethit.model.GameState.Companion.INTRODUCTION
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER
import com.jaanerikpihel.secrethit.model.GameState.Companion.VOTE_RESULTS
import com.jaanerikpihel.secrethit.model.GameState.Companion.VOTING
import mu.KotlinLogging
import org.apache.tomcat.util.buf.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.beans.PropertyChangeEvent
import java.time.LocalDateTime
import java.util.*
import kotlin.math.floor


private val logger = KotlinLogging.logger {}
const val START_MSG = "\$start\$"
const val RESET_MSG = "\$reset\$"
const val FASCIST = "Fascist"
const val LIBERAL = "Liberal"
const val HITLER = "Hitler"
const val YAY = "Yay"
const val NAY = "Nay"
const val TIME_TO_LEARN_ROLES = 1500L
const val CANDIDATE = "Candidate"
const val TOPIC_GAMESTATE = "/topic/gameState"
const val QUEUE_REPLY = "/queue/reply"

@Controller
class Controller {

    @Autowired
    private var messagingTemplate: SimpMessageSendingOperations? = null
    private var allPlayers = emptyList<Player>().toMutableList()
    private var playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf()
    private var gameState = GameState()
    private var voterYayOrNayMap = mutableMapOf<Player, String>()

    @MessageMapping("/voting")
    fun processMessageFromClient(
            @Payload message: VotingMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) {
        handleVoteOrChancellorCandidate(sha, message, messageHeaders)
    }

    @MessageMapping("/register")
    fun processMessageFromClient(
            @Payload message: RegisterMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) {
        if (message.name == START_MSG) {
            startGame(messageHeaders)
        } else {
            addPlayer(message, sha, messageHeaders)
        }
    }

    @MessageMapping("/discard")
    fun processMessageFromClient(
            @Payload message: DiscardMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) {
        handleDiscard(message, messageHeaders)
    }

    @MessageMapping("/reset")
    fun processMessageFromClient(
            @Payload message: ResetMessage,
            sha: SimpMessageHeaderAccessor,
            messageHeaders: MessageHeaders
    ) {
        if (message.message == RESET_MSG) {
            resetGame(messageHeaders)
        }
    }

    @EventListener
    fun handlePropertyChangeEvent(event: PropertyChangeEvent) {
        logger.info { "Just logging $event!" }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        logger.error { "Sending error" }
        return exception.message
    }

    private fun handleDiscard(message: DiscardMessage, messageHeaders: MessageHeaders) {
        gameState.cardPack.addToDiscardPile(message.discardedCard)
        logger.info { "Received from president: ${message.cards} and discarded ${message.discardedCard}" }
        if (message.cards.size == 2) {
            val cards = Gson().toJson(mapOf("Cards" to message.cards))
            logger.info { "Chancellor currently ${gameState.chancellor!!.name}, sending cards $cards" }
            messagingTemplate!!.convertAndSendToUser(gameState.chancellor!!.sessionId, QUEUE_REPLY, cards);
        }
        if (message.cards.size == 1) {
            if (message.cards[0].toLowerCase() == LIBERAL.toLowerCase()) {
                gameState.libPolicies++
            } else {
                gameState.facPolicies++
                handlePresidentialPowers()
            }
            voterYayOrNayMap = mutableMapOf()
            gameState.president = getNextPresident(
                    gameState.president!!,
                    null,
                    alivePlayersOrder = gameState.alivePlayerOrder
            )
            gameState.gameState = VOTING
            gameState.chancellor = null
            gameState.extraInfo = ""
            gameState.lastGovernment = Pair(gameState.president!!, gameState.chancellor!!)
            messagingTemplate!!.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
        }
    }

    private fun handlePresidentialPowers() {
        /*
        This function is called only when fac policy is just enacted.
        TODO:   5-6 p 3rd card pres examines 3, kill, kill
                7-8 p 2nd card pres examines party, picks next pres, kill, kill
                9-10p 1st also examination, others same as before
         */
        when {
            gameState.alivePlayerOrder.size <= 6 -> when (gameState.facPolicies) {
                3 -> sendPresidentThreeTopCards()
                4, 5 -> askPresidentToKillPlayer()
            }
            gameState.alivePlayerOrder.size <= 8 -> when (gameState.facPolicies) {
                2 -> askPresidentToExamineParty()
                3 -> askPresidentToPickNextPresident()
                4 -> askPresidentToKillPlayer()
                5 -> askPresidentToKillPlayer()
            }
            else -> when (gameState.facPolicies) {
                1 -> askPresidentToExamineParty()
                2 -> askPresidentToExamineParty()
                3 -> askPresidentToPickNextPresident()
                4 -> askPresidentToKillPlayer()
                5 -> askPresidentToKillPlayer()
            }
        }
    }

    private fun askPresidentToPickNextPresident() {
        TODO("Not yet implemented")
    }

    private fun askPresidentToExamineParty() {
        TODO("Not yet implemented")
    }

    private fun askPresidentToKillPlayer() {
        TODO("Not yet implemented")
    }

    private fun sendPresidentThreeTopCards() {
        messagingTemplate!!.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf("peekedCards" to gameState.cardPack.peekThree()))
        );
    }

    private fun handleVoteOrChancellorCandidate(sha: SimpMessageHeaderAccessor, message: VotingMessage, messageHeaders: MessageHeaders) {
        val messageFromPlayer = allPlayers.find { it.sessionId == sha.user!!.name }!!
        logger.info { "Message received from $messageFromPlayer" }
        if (message.chancellor.isNotBlank() &&
                allPlayers.find { it.sessionId == sha.user!!.name } == gameState.president) {
            gameState.chancellor = allPlayers.find { it.name == message.chancellor }
            gameState.extraInfo = CANDIDATE
            messagingTemplate!!.convertAndSend(
                    TOPIC_GAMESTATE,
                    gameState.toJSON(),
                    messageHeaders
            )
            gameState.extraInfo = ""
        } else {
            voterYayOrNayMap[messageFromPlayer] = message.yayOrNay
        }

        if (voterYayOrNayMap.keys.size == allPlayers.size) {
            logger.info { "ALL VOTES RECEIVED!" }
            gameState.gameState = VOTE_RESULTS
            gameState.extraInfo = Gson().toJson(voterYayOrNayMap.map { (p, v) -> mapOf(p.name to v) })
            if (voterYayOrNayMap.values.count { it == YAY } > voterYayOrNayMap.values.count { it == NAY }) {
                val cards = Gson().toJson(mapOf("Cards" to gameState.cardPack.drawThree()))
                logger.info { "Yays have it, sending ${cards}." }
                gameState.failedGovernments = 0
                messagingTemplate!!.convertAndSendToUser(gameState.president!!.sessionId, QUEUE_REPLY, cards);
                messagingTemplate!!.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
            } else {
                logger.info { "Nays have it." }
                voterYayOrNayMap = mutableMapOf()
                gameState.president = getNextPresident(
                        gameState.president!!,
                        null,
                        alivePlayersOrder = gameState.alivePlayerOrder
                )
                gameState.failedGovernments++
                if (gameState.failedGovernments == 3) {
                    if (gameState.cardPack.drawN(1)[0] == LIBERAL.toLowerCase()) gameState.libPolicies++
                    else gameState.facPolicies++
                    gameState.failedGovernments = 0

                }
                gameState.gameState = VOTING
                gameState.chancellor = null
                gameState.extraInfo = ""
                messagingTemplate!!.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
            }
        }
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
        gameState.players = allPlayers
        messagingTemplate!!.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
    }

    private fun resetGame(messageHeaders: MessageHeaders) {
        logger.info { "Resetting game." }
        allPlayers = emptyList<Player>().toMutableList()
        playerNameToHeaders = mutableMapOf()
        gameState = GameState(REGISTER)
        voterYayOrNayMap = mutableMapOf()
        messagingTemplate!!.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
    }

    private fun startGame(messageHeaders: MessageHeaders) {
        logger.info { "Started game at ${LocalDateTime.now()}" }
        gameState = GameState(
                gameState = INTRODUCTION, players = allPlayers,
                president = allPlayers[floor(Math.random() * allPlayers.size).toInt()],
                extraInfo = allPlayers.map { it -> it.sessionId }.toString()
        )
        logger.info { "gameState=: ${gameState.toJSON()}, allPlayers: $allPlayers" }
        messagingTemplate!!.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
        allPlayers.zip(getShuffledRoles(allPlayers.size)).forEach { run { it.first.role = it.second } }
        gameState.alivePlayerOrder = allPlayers as List<Player>
        logger.info { "Alive player order: ${gameState.alivePlayerOrder}" }
        allPlayers.forEach {
            if (it.role == FASCIST) {
                messagingTemplate?.convertAndSendToUser(
                        it.sessionId,
                        QUEUE_REPLY,
                        introMessage(true, it.role));
            } else {
                messagingTemplate?.convertAndSendToUser(
                        it.sessionId,
                        QUEUE_REPLY,
                        introMessage(false, it.role));
            }
        }
        allPlayers.forEach { logger.info { "${it.name} is ${it.role}" } }
        Timer().schedule(
                object : TimerTask() {
                    override fun run() {
                        logger.info { "Waited ${TIME_TO_LEARN_ROLES / 1000} s before setting gamestate to $VOTING." }
                        gameState.gameState = VOTING
                        messagingTemplate!!.convertAndSend(
                                TOPIC_GAMESTATE,
                                gameState.toJSON(),
                                messageHeaders
                        )
                    }
                },
                TIME_TO_LEARN_ROLES
        )
    }

    private fun introMessage(isFascist: Boolean, role: String): String {
        val information: String = if (isFascist) {
            "Hitler doesn't see this message. You have 10-15 seconds to memorise your team. Your team:" +
                    allPlayers
                            .filter { it.role != LIBERAL }
                            .map { "${it.name} is ${it.role}" }
                            .run { ";" + StringUtils.join(this, ';') }
        } else {
            "Please look at the screen for 10-15 seconds to allow Fascists to learn roles of the players."
        }
        mapOf("role" to role)
        mapOf("information" to information)
        return Gson().toJson(mapOf(
                "Introduction" to mapOf(
                        "role" to role,
                        "extraInfo" to information
                )
        ))
    }
}
