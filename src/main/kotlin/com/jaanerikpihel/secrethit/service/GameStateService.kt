package com.jaanerikpihel.secrethit.service

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.controller.introMessage
import com.jaanerikpihel.secrethit.controller.waitAndDo
import com.jaanerikpihel.secrethit.listener.GameStateListener.Companion.LOYALTY
import com.jaanerikpihel.secrethit.listener.GameStateListener.Companion.LOYALTY_PEEKED
import com.jaanerikpihel.secrethit.listener.GameStateListener.Companion.PEEK
import com.jaanerikpihel.secrethit.model.*
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER
import mu.KotlinLogging
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.math.floor

private val logger = KotlinLogging.logger {}
const val FASCIST = "Fascist"
const val LIBERAL = "Liberal"
const val HITLER = "Hitler"
const val YAY = "Yay"
const val NAY = "Nay"
const val TIME_TO_WAIT = 15000L
const val CANDIDATE = "Candidate"
const val TOPIC_GAMESTATE = "/topic/gameState"
const val QUEUE_REPLY = "/queue/reply"
const val PRESIDENTIAL_POWER = "presidentialPower"
const val PEEKED_CARDS = "peekedCards"
const val KILL_PLAYER = "killPlayer"

@Service
class GameStateService(
        private var messagingTemplate: SimpMessageSendingOperations
) {
    private var allPlayers = emptyList<Player>().toMutableList()
    private var playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf()
    private var gameState = GameState()
    private var voterYayOrNayMap = mutableMapOf<Player, String>()
    private var messageHeaders: MessageHeaders? = null

    fun startGame(messageHeaders: MessageHeaders) {
        this.messageHeaders = messageHeaders
        logger.info { "Started game at ${LocalDateTime.now()}" }
        gameState = GameState(
                gameState = GameState.INTRODUCTION, players = allPlayers,
                president = allPlayers[floor(Math.random() * allPlayers.size).toInt()],
                extraInfo = allPlayers.map { it.sessionId }.toString()
        )
        logger.info { "gameState=: ${gameState.toJSON()}, allPlayers: $allPlayers" }
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
        allPlayers.zip(getShuffledRoles(allPlayers.size)).forEach { run { it.first.role = it.second } }
        gameState.alivePlayerOrder = allPlayers.shuffled().toMutableList()
        logger.info { "Alive player order: ${gameState.alivePlayerOrder}" }
        allPlayers.forEach {
            if (it.role == FASCIST) {
                messagingTemplate.convertAndSendToUser(
                        it.sessionId,
                        QUEUE_REPLY,
                        introMessage(allPlayers, true, it.role))
            } else {
                messagingTemplate.convertAndSendToUser(
                        it.sessionId,
                        QUEUE_REPLY,
                        introMessage(allPlayers, false, it.role))
            }
        }
        allPlayers.forEach { logger.info { "${it.name} is ${it.role}" } }
        waitAndDo(TIME_TO_WAIT) { startVoting() }
    }

    fun addPlayer(message: RegisterMessage, sha: SimpMessageHeaderAccessor, messageHeaders: MessageHeaders) {
        if (gameState.gameState == REGISTER) {
            val player: Player
            if (allPlayers.map { it.name }.contains(message.name)) {
                //Update sessionId if name already exists. Simple fix for connectivity issues.
                player = allPlayers.find { it.name == message.name }!!
                if (player.isAlive) player.sessionId = sha.user!!.name
                else player.sessionId = ""
            } else {
                player = Player(
                        sha.user!!.name,
                        message.name, ""
                )
                playerNameToHeaders[player.name] = messageHeaders
                allPlayers.add(player)
            }
            logger.info {
                "Registered a new player: $player, " +
                        "total number of alive players=${gameState.alivePlayerOrder.size}"
            }
            gameState.alivePlayerOrder = allPlayers
            messagingTemplate.convertAndSend(
                    TOPIC_GAMESTATE,
                    gameState.toJSON(),
                    messageHeaders
            )
        }
    }

    private fun startVoting() {
        logger.info { "Waited ${TIME_TO_WAIT / 1000} s before setting gamestate to ${GameState.VOTING}." }
        gameState.gameState = GameState.VOTING
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders!!
        )
    }

    fun handleDiscard(message: DiscardMessage) {
        gameState.cardPack.addToDiscardPile(message.discardedCard)
        val isPresident = message.cards.size == 2
        if (isPresident) {
            logger.info { "Received from president: ${message.cards} (discarded ${message.discardedCard})" }
            val cards = Gson().toJson(mapOf("Cards" to message.cards))
            messagingTemplate.convertAndSendToUser(gameState.chancellor!!.sessionId, QUEUE_REPLY, cards)
        } else {
            logger.info { "Received from chancellor: ${message.cards} (discarded ${message.discardedCard})" }
            val isCardLiberal = message.cards[0].toLowerCase() == LIBERAL.toLowerCase()
            val isCardFascist = message.cards[0].toLowerCase() == FASCIST.toLowerCase()
            when {
                isCardLiberal -> {
                    gameState.libPolicies++
                    sendVotingGameState()
                }
                isCardFascist -> {
                    gameState.facPolicies++
                    handlePresidentialPowers { sendVotingGameState() }
                }
                else -> sendVotingGameState()
            }
        }
    }

    fun sendVotingGameState() {
        voterYayOrNayMap = mutableMapOf()
        gameState.lastGovernment = Pair(gameState.president!!, gameState.chancellor!!)
        gameState.president = getNextPresident(
                gameState.president!!,
                null,
                alivePlayersOrder = gameState.alivePlayerOrder
        )
        gameState.gameState = GameState.VOTING
        gameState.chancellor = null
        gameState.extraInfo = ""
        messagingTemplate.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders!!)
        println("Sent game state with players: ${gameState.alivePlayerOrder}")
    }

    private fun handlePresidentialPowers(defaultFunction: () -> Unit) =/*
        This function is called only when fac policy is just enacted.
        TODO:   5-6 p 3rd card pres examines 3, kill, kill
                7-8 p 2nd card pres examines party, picks next pres, kill, kill
                9-10p 1st also examination, others same as before
         */
            when {
                gameState.alivePlayerOrder.size <= 6 -> when (gameState.facPolicies) {
                    1 -> askPresidentToExamineParty() //delete
                    3 -> sendPresidentThreeTopCards()
                    4, 5 -> askPresidentToKillPlayer()
                    else -> defaultFunction()
                }
                gameState.alivePlayerOrder.size <= 8 -> when (gameState.facPolicies) {
                    2 -> askPresidentToExamineParty()
                    3 -> askPresidentToPickNextPresident()
                    4 -> askPresidentToKillPlayer()
                    5 -> askPresidentToKillPlayer()
                    else -> defaultFunction()
                }
                else -> when (gameState.facPolicies) {
                    1 -> askPresidentToExamineParty()
                    2 -> askPresidentToExamineParty()
                    3 -> askPresidentToPickNextPresident()
                    4 -> askPresidentToKillPlayer()
                    5 -> askPresidentToKillPlayer()
                    else -> defaultFunction() //should be unnecessary
                }
            }

    private fun askPresidentToPickNextPresident() {
        println("askPresidenttoPickNext")
        TODO("Not yet implemented")
    }

    private fun askPresidentToExamineParty() {
        messagingTemplate.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf(PRESIDENTIAL_POWER to mapOf(LOYALTY to "")))
        )
        logger.info { "Asked president to examine somebody." }

    }

    fun sendPresidentLoyalty(subject: String) {
        val player = gameState.alivePlayerOrder.first { it.name == subject }
        val loyalty = if (player.role != LIBERAL) FASCIST else LIBERAL
        messagingTemplate.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf(PRESIDENTIAL_POWER to mapOf(LOYALTY_PEEKED to
                    mapOf("playerName" to player.name, "playerRole" to loyalty)
                )))
        )
        logger.info { "Sent party of  ${player.name} (${player.role}) to president." }
    }

    private fun askPresidentToKillPlayer() {
        logger.info { "Asking president to kill a player." }
        messagingTemplate.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(
                        mapOf(PRESIDENTIAL_POWER to
                                mapOf(KILL_PLAYER to
                                        gameState.alivePlayerOrder.filter { it != gameState.president }.map { it.name }
                                )))
        )
    }

    private fun sendPresidentThreeTopCards() {
        logger.info { "Sending cards for prev pres to peek: ${gameState.cardPack.peekThree()}" }
        messagingTemplate.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf(PRESIDENTIAL_POWER to mapOf(PEEKED_CARDS to gameState.cardPack.peekThree())))
        )
    }

    fun handleVoteOrChancellorCandidate(
            sha: SimpMessageHeaderAccessor,
            message: VotingMessage
    ) {
        val messageFromPlayer = allPlayers.find { it.sessionId == sha.user!!.name }!!
        if (message.chancellor.isNotBlank() &&
                allPlayers.find { it.sessionId == sha.user!!.name } == gameState.president) {
            setChancellor(message)
        } else {
            voterYayOrNayMap[messageFromPlayer] = message.yayOrNay
        }

        if (voterYayOrNayMap.keys.size == gameState.alivePlayerOrder.size) {
            handleVoteResults()
        }
    }

    private fun setChancellor(message: VotingMessage) {
        gameState.chancellor = allPlayers.find { it.name == message.chancellor }
        gameState.extraInfo = CANDIDATE
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders!!
        )
        gameState.extraInfo = ""
    }

    private fun handleVoteResults() {
        gameState.gameState = GameState.VOTE_RESULTS
        gameState.extraInfo = Gson().toJson(voterYayOrNayMap.map { (p, v) -> mapOf(p.name to v) })
        if (voterYayOrNayMap.values.count { it == YAY } > voterYayOrNayMap.values.count { it == NAY }) {
            val cards = Gson().toJson(mapOf("Cards" to gameState.cardPack.drawThree()))
            logger.info { "Yays have it, sending ${cards}." }
            gameState.failedGovernments = 0
            with(messagingTemplate) {
                logger.info { "Yays have it, sending ${cards}." }
                gameState.failedGovernments = 0
                convertAndSendToUser(gameState.president!!.sessionId, QUEUE_REPLY, cards);
                convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders!!)
            }
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
            gameState.gameState = GameState.VOTING
            gameState.chancellor = null
            gameState.extraInfo = ""
            messagingTemplate.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders!!)
        }
    }

    fun resetGame() {
        logger.info { "Resetting game." }
        allPlayers = emptyList<Player>().toMutableList()
        playerNameToHeaders = mutableMapOf()
        gameState = GameState(GameState.REGISTER)
        voterYayOrNayMap = mutableMapOf()
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders!!
        )
    }

    fun killPlayer(playerName: String) {
        val player = gameState.alivePlayerOrder.first { it.name == playerName }
        messagingTemplate.convertAndSendToUser(
                player.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf("dead" to ""))
        )
        gameState.alivePlayerOrder.remove(player)
        player.isAlive = false;
        logger.info { "Removed $player from game." }
    }

    fun pickNextPresident(subject: String) {
        TODO("Not yet implemented")
    }
}