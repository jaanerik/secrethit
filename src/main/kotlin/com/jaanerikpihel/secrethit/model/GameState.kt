package com.jaanerikpihel.secrethit.model


class GameState() {

    constructor(gameState: String) : this() {
        this.gameState = gameState
        this.facPolicies = 0
        this.libPolicies = 0
        this.lastGovernment = null
        this.cardPack = CardPack()
        this.noPlayers = 0
    }

    constructor(noPlayers: Int) : this() {
        this.gameState = STARTED
        this.facPolicies = 0
        this.libPolicies = 0
        this.lastGovernment = null
        this.cardPack = CardPack()
        this.noPlayers = noPlayers
    }

    companion object {
        const val REGISTER = "Register"
        const val STARTED = "Started"
        const val VOTING = "Voting"
        const val LIB_VICTORY = "LiberalVictory"
        const val FAC_VICTORY = "FascistVictory"
    }

    var gameState = REGISTER
    var facPolicies: Int = 0
    var libPolicies: Int = 0
    var cardPack: CardPack = CardPack()
    var lastGovernment: Pair<Player, Player>? = null
    var noPlayers: Int = 0

    init {
        this.gameState = STARTED
        this.facPolicies = 0
        this.libPolicies = 0
        this.lastGovernment = null
        this.cardPack = CardPack()
    }

}

class CardPack {

    var cards: MutableList<String> = (MutableList(11) {"fascist"} + MutableList(6) {"liberal"})
            .shuffled() as MutableList<String>

    fun takeN(n: Int): List<String> {
        val takenCards = cards.toList().subList(0,n)
        for (i in 0 until n)
            cards.removeAt(0)
        if (cards.isEmpty()) newPack()
        return takenCards
    }

    fun takeThree(): List<String> {
        if (cards.size < 3) {
            val howManyNew = 3 - cards.size
            val oldCards = takeN(cards.size)
            return oldCards.map { it.toUpperCase() } + takeN(howManyNew)
        }
        return takeN(3)
    }

    fun newPack() {
        cards = (MutableList(11) {"fascist"} + MutableList(6) {"liberal"}).shuffled() as MutableList<String>
    }

    fun packSize(): Int {
        return cards.size
    }

}
