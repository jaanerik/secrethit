package com.jaanerikpihel.secrethit.service

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.controller.introMessage
import com.jaanerikpihel.secrethit.controller.waitAndDo
import com.jaanerikpihel.secrethit.model.*
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
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
const val TIME_TO_WAIT = 1500L //TODO: 15000L
const val CANDIDATE = "Candidate"
const val TOPIC_GAMESTATE = "/topic/gameState"
const val QUEUE_REPLY = "/queue/reply"
const val PRESIDENTIAL_POWER = "presidentialPower"
const val PEEKED_CARDS = "peekedCards"

@Service
class GameStateService (
        private var messagingTemplate: SimpMessageSendingOperations,
        private val publisher: ApplicationEventPublisher
) {
    private var allPlayers = emptyList<Player>().toMutableList()
    private var playerNameToHeaders: MutableMap<String, MessageHeaders> = mutableMapOf()
    private var gameState = GameState()
    private var voterYayOrNayMap = mutableMapOf<Player, String>()

    fun startGame(messageHeaders: MessageHeaders) {
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
        gameState.alivePlayerOrder = allPlayers.shuffled()
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
        waitAndDo(TIME_TO_WAIT) { startVoting(messageHeaders) }
    }

    fun addPlayer(message: RegisterMessage, sha: SimpMessageHeaderAccessor, messageHeaders: MessageHeaders) {
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
        logger.info {
            "Registered a new player: $player, " +
                    "total number of alive players=${gameState.alivePlayerOrder.size}"
        }
        gameState.players = allPlayers
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
    }

    private fun startVoting(messageHeaders: MessageHeaders) {
        logger.info { "Waited ${TIME_TO_WAIT / 1000} s before setting gamestate to ${GameState.VOTING}." }
        gameState.gameState = GameState.VOTING
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
    }

    fun handleDiscard(message: DiscardMessage, messageHeaders: MessageHeaders) {
        gameState.cardPack.addToDiscardPile(message.discardedCard)
        val isPresident = message.cards.size == 2
        if (isPresident) {
            logger.info { "Received from president: ${message.cards} (discarded ${message.discardedCard})" }
            val cards = Gson().toJson(mapOf("Cards" to message.cards))
            messagingTemplate.convertAndSendToUser(gameState.chancellor!!.sessionId, QUEUE_REPLY, cards)
        } else {
            logger.info { "Received from chancellor: ${message.cards} (discarded ${message.discardedCard})" }
            val isCardLiberal = message.cards[0].toLowerCase() == LIBERAL.toLowerCase()
            if (isCardLiberal) {
                gameState.libPolicies++
                sendGameStateAfterDiscard(messageHeaders)
            } else {
                gameState.facPolicies++
                handlePresidentialPowers { sendGameStateAfterDiscard(messageHeaders) }
            }
        }
    }

    private fun sendGameStateAfterDiscard(messageHeaders: MessageHeaders) {
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
        messagingTemplate.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
    }

    private fun handlePresidentialPowers(defaultFunction: () -> Unit) {
        /*
        This function is called only when fac policy is just enacted.
        TODO:   5-6 p 3rd card pres examines 3, kill, kill
                7-8 p 2nd card pres examines party, picks next pres, kill, kill
                9-10p 1st also examination, others same as before
         */
        when {
            gameState.alivePlayerOrder.size <= 6 -> when (gameState.facPolicies) {
                1, 3 -> sendPresidentThreeTopCards()
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
                else -> defaultFunction() //should be uneccesary
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
        logger.info { "Sending cards for prev pres to peek: ${gameState.cardPack.peekThree()}" }
        messagingTemplate.convertAndSendToUser(
                gameState.president!!.sessionId, QUEUE_REPLY,
                Gson().toJson(mapOf(PRESIDENTIAL_POWER to mapOf(PEEKED_CARDS to gameState.cardPack.peekThree())))
        )
    }

    fun handleVoteOrChancellorCandidate(sha: SimpMessageHeaderAccessor, message: VotingMessage, messageHeaders: MessageHeaders) {
        val messageFromPlayer = allPlayers.find { it.sessionId == sha.user!!.name }!!
        if (message.chancellor.isNotBlank() &&
                allPlayers.find { it.sessionId == sha.user!!.name } == gameState.president) {
            setChancellor(message, messageHeaders)
        } else {
            voterYayOrNayMap[messageFromPlayer] = message.yayOrNay
        }

        if (voterYayOrNayMap.keys.size == allPlayers.size) {
            handleVoteResults(messageHeaders)
        }
    }

    fun setChancellor(message: VotingMessage, messageHeaders: MessageHeaders) {
        gameState.chancellor = allPlayers.find { it.name == message.chancellor }
        gameState.extraInfo = CANDIDATE
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
        gameState.extraInfo = ""
    }

    private fun handleVoteResults(messageHeaders: MessageHeaders) {
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
                convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
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
            messagingTemplate.convertAndSend(TOPIC_GAMESTATE, gameState.toJSON(), messageHeaders)
        }
    }

    fun resetGame(messageHeaders: MessageHeaders) {
        logger.info { "Resetting game." }
        allPlayers = emptyList<Player>().toMutableList()
        playerNameToHeaders = mutableMapOf()
        gameState = GameState(GameState.REGISTER)
        voterYayOrNayMap = mutableMapOf()
        messagingTemplate.convertAndSend(
                TOPIC_GAMESTATE,
                gameState.toJSON(),
                messageHeaders
        )
    }
}