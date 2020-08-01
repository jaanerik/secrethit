import React, {Fragment, PureComponent} from 'react';
import SockJsClient from 'react-stomp';

import GameStateContext from "./GameStateContext";
import RegisterPage from "./RegisterPage/RegisterPage";
import VotingPage from "./VotingPage/VotingPage";

class App extends PureComponent {
    state = {
        myName: '',
        gameState: 'Register',
        fascistCardsPlayed: 0,
        liberalCardsPlayed: 0,
        cardPackSize: 0,
        president: '',
        chancellor: '',
        players: [],
        nullGovernments: 0,
        lastGovernment: [],
        extraInfo: ''
    };

    render = () => <Fragment>
        <SockJsClient
            url='http://localhost:8080/secrethit'
            topics={['/user/queue/reply', '/queue/reply', '/topic/gameState']}
            onMessage={this.handleMessage}
            ref={client => {
                this.clientRef = client
            }}
        />
        <GameStateContext.Provider value={this.contextValue()}>
            {this.renderCurrentPage()}
        </GameStateContext.Provider>
    </Fragment>;

    renderCurrentPage = () => {
        switch (this.state.gameState) {
            case 'Register':
                return (<RegisterPage/>);
            case 'Voting':
                return (<VotingPage/>);
            case 'Introduction':
                return (<VotingPage/>);

            default:
                return 'EI OSKA VEEL'
        }
    };

    setMyName = (name) => {
        this.setState({myName: name})
    };

    contextValue = () => ({
        ...this.state,
        sendMessage: this.sendMessage,
        setMyName: this.setMyName
    });

    handleMessage = (msg, topic) => {
        console.log(topic + " received!");
        // console.log({...msg});
        switch (topic) {
            case '/topic/gameState':
                this.setState({...msg});
                console.log('Received gameState: ');
                console.log(this.state);
                break;
            case '/user/queue/reply':
                console.log('Received message');
                console.log({...msg});
                break;
            case '/queue/reply':
                console.log('Received message');
                console.log({...msg});
                break;
            default:
                console.error('Unknown topic received!');
                break;
        }
    };

    sendMessage = (url, msg) => {
        console.log(url + ' :' + JSON.stringify(msg));
        this.clientRef.sendMessage(url, JSON.stringify(msg));
    };
}

export default App;
