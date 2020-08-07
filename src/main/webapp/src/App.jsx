import React, {Fragment, PureComponent} from 'react';
import SockJsClient from 'react-stomp';

import GameStateContext from "./GameStateContext";
import RegisterPage from "./RegisterPage/RegisterPage";
import VotingPage from "./VotingPage/VotingPage";
import IntroductionPage from "./IntroductionPage/IntroductionPage";

class App extends PureComponent {
    state = {
        myName: '',
        myRole: '',
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
                return (<IntroductionPage/>);

            default:
                return 'EI OSKA VEEL'
        }
    };

    setMyName = (name) => {
        this.setState({myName: name})
    };

    setMyRole = (role) => {
        this.setState({myRole: role})
    };

    setExtraInfo = (extraInfo) => {
        this.setState({extraInfo: extraInfo})
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
                if ('Introduction' in {...msg}) {
                    console.log(msg['Introduction']);
                    this.setMyRole(msg['Introduction']['role'].toString());
                    this.setExtraInfo(msg['Introduction']['extraInfo'].toString());
                }
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
