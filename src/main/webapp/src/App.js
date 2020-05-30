import React, {Component} from 'react';
import NewIndex from "./Index/NewIndex";
import Websocket from "./Websocket/Websocket";

class App extends Component {

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
            <div>
                {/*<StompClient*/}
                {/*    endpoint="ws://localhost:8080/secrethit"*/}
                {/*    topic="registrations"*/}
                {/*    onMessage={this.handleMessage}*/}
                {/*><NewIndex/>*/}
                {/*</StompClient>*/}
                <Websocket>
                    <NewIndex/>
                </Websocket>
            </div>

        );
    }
}

export default App;
