import React, { Fragment, PureComponent } from 'react';
import SockJsClient from 'react-stomp';

import GameStateContext from "./GameStateContext";
import RegisterPage from "./RegisterPage/RegisterPage";

class App extends PureComponent {
    state = {
        gameState: 'Register',
        fascistCardsPlayed: 0,
        liberalCardsPlayed: 0,
        president: '',
        chancellor: '',
        players: [],
        nullGovernments: 0,
    }

    render = () => (
        <Fragment>
            <SockJsClient
                url='http://localhost:8080/secrethit'
                topics={['/queue/reply', '/topic/registrations']}
                onMessage={this.handleMessage}
                ref={(client) => { this.clientRef = client }}
            />
            <GameStateContext.Provider value={this.contextValue()} >
                {this.renderCurrentPage()}
            </GameStateContext.Provider>
        </Fragment>
    )

    renderCurrentPage = () => {
        if (this.state.gameState === 'Register') {
            return (<RegisterPage />)
        } else {
            return 'EI OSKA VEEL'
        }
    }

    contextValue = () => ({
        ...this.state,
        sendMessage: this.sendMessage,
    })

    handleMessage = (msg, topic) => {
        console.log(topic + " received!")
        console.log({ ...msg })
        switch(topic) {
          case '/topic/registrations':
            this.setState({ players: msg })
            break;
          case 'topic/gamestate':
            this.setState({ ...msg })
            break
          default:
            console.err('Unknown topic received!')
        }
    };

    sendMessage = (url, msg) => {
        this.clientRef.sendMessage(url, JSON.stringify(msg));
    };
}

export default App;
