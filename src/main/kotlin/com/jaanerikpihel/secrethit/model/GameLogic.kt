package com.jaanerikpihel.secrethit.model

fun getShuffledRoles(size: Int): List<String> {
    val roles = when (size) {
        2 -> howManyLibFacWithOutHit(1, 0) //Testing
        3 -> howManyLibFacWithOutHit(1, 1) //Testing
        5 -> howManyLibFacWithOutHit(3, 1)
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