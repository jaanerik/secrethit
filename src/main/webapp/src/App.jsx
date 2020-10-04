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
        lastGovernment: {first: '', second: ''},
        extraInfoJSON: '{}',
        presidentialPower: '',
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

    setExtraInfoJSON = (extraInfoJSON) => {
        this.setState({extraInfoJSON: extraInfoJSON})
    };

    setCards = (cards) => {
        console.log('I set cards to ' + cards)
        this.setState({cards: cards})
    };

    setPresidentialPower = (power) => {
        this.setState({presidentialPower: power})
    };

    contextValue = () => ({
        ...this.state,
        sendMessage: this.sendMessage,
        setMyName: this.setMyName,
        setCards: this.setCards,
        setExtraInfoJSON: this.setExtraInfoJSON,
        setPresidentialPower: this.setPresidentialPower
    });

    handleMessage = (msg, topic) => {
        console.log(topic + " received!");
        if (topic === '/topic/gameState') {
            this.setState({...msg});
            console.log(this.state);
            console.log('Currently cards: ', this.state.cards);
        }

        if (topic === '/user/queue/reply') {
            console.log({...msg});
            const key = Object.keys({...msg})[0];
            switch (key) {
                case 'Introduction':
                    this.setMyRole(msg[key]['role'].toString());
                    this.setExtraInfoJSON(msg[key]['extraInfo'].toString());
                    break;
                case 'presidentialPower':
                    const presidentialPowerObj = {...msg[key]};
                    if (Object.keys({...presidentialPowerObj})[0] === 'peekedCards') {
                        this.setPresidentialPower('peekedCards');
                        console.log('Peeked cards: ', presidentialPowerObj['peekedCards']);
                    }
                    this.setCards(presidentialPowerObj['peekedCards']);
                    break;
                case 'Cards':
                    this.setCards(msg['Cards']);
                    break;
                default:
                    console.error('Weirdness handling queue reply.')
            }
        }
    };

    sendMessage = (url, msg) => {
        console.log(url + ' :' + JSON.stringify(msg));
        this.clientRef.sendMessage(url, JSON.stringify(msg));
    };
}

export default App;
