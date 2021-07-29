# Secret Hitler Online
## Web application for the board game

[![version](https://img.shields.io/badge/version-0.0.1-yellow.svg)](https://semver.org)

This is a project for playing the [Secret Hitler board game](https://www.secrethitler.com/) online. This is a study project for me to familiarise myself to websockets, kotlin, react, spring boot and build tools (gradle kts and react-scripts).

## Current progress

A most basic 0.0.1 version of the game is now completed and [is hosted in aws](http://secrethit-env.eba-3s67uuww.eu-north-1.elasticbeanstalk.com/).

## License and Attribution

Secret Hitler is designed by Max Temkin (Cards Against Humanity, Humans vs. Zombies), Mike Boxleiter (Solipskier, TouchTone), Tommy Maranges (Philosophy Bro) and illustrated by Mackenzie Schubert (Letter Tycoon, Penny Press).

This game is licensed as per the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license.

### TODO
- Make connections more sturdy - if person disconnects then game should not break
- Make UI more usable, beautiful
- Make application INFO level logs accessible after game for analysis
- The recommended changes (there are less fascist policies when there are certain number of players)
- Add socialists to game dynamic

## Building for local development

For local development one needs to:
- run `./gradlew clean build`
- change frontend url to `http://localhost:8080` in `.env`
- change CorsFilter.kt to allow `http://localhost:3000` CORS headers
- run `yarn install` and `yarn start` in `src/main/webapp`
- either run application in intelliJ or run `./gradlew bootRun`

## Deploying

I used the following process for building:
- run `./gradlew copyWebApp`
- add `index.css` to `build/resources/main/static/static` and change `index.html` to include that css file
- run `./gradlew build`
- make sure that `.env` in `src/main/webapp` is correct and the correct url and port is set in CorsFilter.kt
- deploy built jar to beanstalk (needs 5000 port to be opened)
