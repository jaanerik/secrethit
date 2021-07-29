package com.jaanerikpihel.secrethit.model

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER

class GameState() {

    var gameState = REGISTER
    var facPolicies: Int = 0
    var libPolicies: Int = 0
    var cardPack: CardPack = CardPack()
    var lastGovernment: Pair<Player, Player>? = null
    var president: Player? = null
    var chancellor: Player? = null
    var players: List<Player>? = null
    var failedGovernments: Int = 0
    var extraInfo: String = ""
    var alivePlayerOrder: MutableList<Player> = mutableListOf()
    var nextGovernmentIsSpecialElection: Boolean = false
    var nextPresidentialCandidate: Player? = null
    var previousNormalPresident: Player? = null

    constructor(
            gameState: String = "",
            facPolicies: Int = 0,
            libPolicies: Int = 0,
            cardPack: CardPack = CardPack(),
            lastGovernment: Pair<Player, Player>? = null,
            president: Player? = null,
            chancellor: Player? = null,
            players: List<Player>? = null,
            nullGovernments: Int = 0,
            extraInfo: String = "{}",
            lastGovernmentWasSpecialElection: Boolean = false,
            nextPresidentialCandidate: Player? = null,
            previousNormalPresident: Player? = null
    ) : this() {
        this.gameState = gameState
        this.facPolicies = facPolicies
        this.libPolicies = libPolicies
        this.lastGovernment = lastGovernment
        this.cardPack = cardPack
        this.lastGovernment = lastGovernment
        this.president = president
        this.chancellor = chancellor
        this.players = players
        this.failedGovernments = nullGovernments
        this.extraInfo = extraInfo
        this.nextGovernmentIsSpecialElection = lastGovernmentWasSpecialElection
        this.nextPresidentialCandidate = nextPresidentialCandidate
        this.previousNormalPresident = previousNormalPresident
    }

    companion object {
        const val VOTE_RESULTS = "VoteResults"
        const val REGISTER = "Register"
        const val ENACTING = "Enacting"
        const val VOTING = "Voting"
        const val INTRODUCTION = "Introduction"
        const val LIB_VICTORY = "LiberalVictory"
        const val FAC_VICTORY = "FascistVictory"

    }

    override fun toString(): String {
        return "GameState(gameState='$gameState', facPolicies=$facPolicies, libPolicies=$libPolicies, " +
                "cardPack=${cardPack.cards.size}, lastGovernment=$lastGovernment, president=$president, " +
                "chancellor=$chancellor, players=$players, nullGovernments=$failedGovernments)"
    }

    fun toJSON(): String {
        return GameStateShareable(this).toJSON()
    }
}

class GameStateShareable(gameState: GameState) {

    private var gameState = REGISTER
    private var fascistCardsPlayed: Int = 0
    private var liberalCardsPlayed: Int = 0
    private var cardPackSize: Int = 0
    private var lastGovernment: Pair<String, String>? = Pair("","")
    private var president: String = ""
    private var chancellor: String = ""
    private var players: List<String>? = null
    private var nullGovernments: Int = 0
    private var extraInfo: String = "{}"

    init {
        this.gameState = gameState.gameState
        this.fascistCardsPlayed = gameState.facPolicies
        this.liberalCardsPlayed = gameState.libPolicies
        this.lastGovernment = gameState.lastGovernment?.run { Pair(this.first.name, this.second.name) } ?: Pair("", "")
        this.cardPackSize = gameState.cardPack.cards.size
        this.president = gameState.president?.name ?: ""
        this.chancellor = gameState.chancellor?.name ?: ""
        this.players = gameState.alivePlayerOrder.map { it.name }
        this.nullGovernments = gameState.failedGovernments
        this.extraInfo = gameState.extraInfo
    }

    fun toJSON(): String {
        return Gson().toJson(this)
    }
}

class CardPack {

    var cards: MutableList<String> = (MutableList(11) { "fascist" } + MutableList(6) { "liberal" })
            .shuffled() as MutableList<String>

    var discardPile: MutableList<String> = mutableListOf()

    fun drawN(n: Int): List<String> {
        val takenCards = cards.toList().subList(0, n)
        for (i in 0 until n) {
            cards.removeAt(0)
            if (cards.isEmpty()) newPack()
        }
        return takenCards
    }

    fun drawThree(): List<String> {
        if (cards.size < 3) {
            val howManyNew = 3 - cards.size
            val oldCards = drawN(cards.size)
            return oldCards.map { it.toUpperCase() } + drawN(howManyNew)
        }
        return drawN(3)
    }

    fun addToDiscardPile(card: String) {
        discardPile.add(card)
        if (cards.size < 3) discardPile = discardPile.shuffled() as MutableList<String>
    }

    fun peekThree(): List<String> {
        return if (cards.size < 3)
            listOf(cards, discardPile.subList(0, 3-cards.size)).flatten()
         else cards.subList(0, 3)

    }

    private fun newPack() {
        cards = discardPile //already shuffled in addToDiscardPile
        discardPile = mutableListOf()
    }
}
