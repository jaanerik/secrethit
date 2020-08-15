import React, {Fragment, PureComponent} from 'react';
import SockJsClient from 'react-stomp';

import GameStateContext from "./GameStateContext";
import RegisterPage from "./RegisterPage/RegisterPage";
import VotingPage from "./VotingPage/VotingPage";
import IntroductionPage from "./IntroductionPage/IntroductionPage";
import EnactingPage from "./EnactingPage/EnactingPage";
import ResetComponent from "./ResetComponent/ResetComponent";
import VoteResultsPage from "./VoteResultsPage/VoteResultsPage";

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
        extraInfo: '',
        cards: []
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
            <Fragment>
                {this.renderCurrentPage()}
                <ResetComponent/>
            </Fragment>
        </GameStateContext.Provider>
    </Fragment>;

    renderCurrentPage = () => {
        switch (this.state.gameState) {
            case 'Register':
                return (<RegisterPage/>);
            case 'Voting':
                return (<VotingPage/>);
            case 'VoteResults':
                return (<VoteResultsPage setCards={this.setCards}/>);
            case 'Introduction':
                return (<IntroductionPage/>);
            case 'Enacting':
                return (<EnactingPage/>);

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

    setCards = (cards) => {
        this.setState({cards: cards})
    };

    contextValue = () => ({
        ...this.state,
        sendMessage: this.sendMessage,
        setMyName: this.setMyName
    });

    handleMessage = (msg, topic) => {
        console.log(topic + " received!");
        switch (topic) {
            case '/topic/gameState':
                this.setState({...msg});
                console.log(this.state);
                console.log('Currently cards: ', this.state.cards);
                break;
            case '/user/queue/reply':
                console.log({...msg});
                if ('Introduction' in {...msg}) {
                    this.setMyRole(msg['Introduction']['role'].toString());
                    this.setExtraInfo(msg['Introduction']['extraInfo'].toString());
                }
                if ('Cards' in {...msg}){
                    this.setCards(msg['Cards'])
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
