# Secret Hitler Online

This is a project for playing the [Secret Hitler boardgame](https://www.secrethitler.com/) online. This is a study project for me to familiarise myself to websockets, kotlin and spring boot.

## Current progress

Registering game works and when there are 5 people registered then you can start the game and your role is alerted. The registration assumes name to be unique and isn't secure. So when a player disconnects and joins with the same name, then messageheaders nad session id is updated.

The cardpack is implemented. It allows you to draw 3 card and if this is the end of a cardpack, then cards before the shuffle are returned in upper case.

GameState should hold all the logic that is sent to the front end. Entirety of the current state is broadcasted after each change to the gamestate.
