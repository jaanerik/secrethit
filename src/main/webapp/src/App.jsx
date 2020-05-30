import React, { PureComponent } from 'react';
import NewIndex from "./Index/NewIndex";
import Websocket from "./Websocket/Websocket";

class App extends PureComponent {

    state = {
        gameState: 'Register',
        fascistCardsPlayed: 0,
        liberalCardsPlayed: 0,
        president: '',
        chancellor: '',
        players: [],
        nullGovernments: 0
    };

    handleMessage = message => {
        console.log(message + " received!")
    };

    render() {
        return (
            <Websocket>
                <NewIndex/>
            </Websocket>
        );
    }
}

export default App;
