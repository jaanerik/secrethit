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
        presidentialPowerObj: null,
        cards: []
    };

    render = () => <Fragment>
        <SockJsClient
            url={`${process.env.REACT_APP_SERVER_URL}/secrethit`}
            // url='http://localhost:8080/secrethit' //development only
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
            case 'Dead':
                return 'You are dead!';

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
        console.log('Extra info JSON: ' + extraInfoJSON.toString())
        this.setState({extraInfoJSON: extraInfoJSON.toString()})
    };

    setCards = (cards) => {
        console.log('Card set to ' + cards);
        this.setState({cards: cards})
    };

    setPresidentialPower = (power) => {
        this.setState({presidentialPower: power})
    };

    setPresidentialPowerObj = (obj) => {
        this.setState({presidentialPowerObj: JSON.stringify(obj)});
    };

    contextValue = () => ({
        ...this.state,
        sendMessage: this.sendMessage,
        setMyName: this.setMyName,
        setCards: this.setCards,
        setExtraInfoJSON: this.setExtraInfoJSON,
        setPresidentialPower: this.setPresidentialPower,
        setPresidentialPowerObj: this.setPresidentialPowerObj
    });

    handleMessage = (msg, topic) => {
        console.log(topic + " received!");
        if (topic === '/topic/gameState') {
            this.setState({...msg});
            console.log(this.state);
            console.log('Currently state: ', this.state);
        }

        if (topic === '/user/queue/reply') {
            console.log({...msg});
            const key = Object.keys({...msg})[0];
            console.log('key is ', key);
            switch (key) {
                case 'Introduction':
                    this.setMyRole(msg[key]['role'].toString());
                    this.setExtraInfoJSON(msg[key]['extraInfo'].toString());
                    break;
                case 'presidentialPower':
                    const presidentialPowerObj = {...msg[key]};
                    console.log('Presidential power obj: ' + presidentialPowerObj.toString())
                    if (Object.keys({...presidentialPowerObj})[0] === 'peekedCards') {
                        this.setPresidentialPower('peekedCards');
                        console.log('Peeked cards: ', presidentialPowerObj['peekedCards']);
                        this.setCards(presidentialPowerObj['peekedCards']);
                    }
                    if (Object.keys({...presidentialPowerObj})[0] === 'killPlayer') {
                        console.log('Can kill one of these players: ', presidentialPowerObj['killPlayer']);
                        this.setPresidentialPower('killPlayer');
                    }
                    if (Object.keys({...presidentialPowerObj})[0] === 'peekLoyalty') {
                        console.log('Choosing whose loyalty to peek.');
                        this.setPresidentialPower('peekLoyalty');
                    }
                    if (Object.keys({...presidentialPowerObj})[0] === 'pickPresident') {
                        console.log('Picking next president.');
                        this.setPresidentialPower('pickPresident');
                    }
                    if (Object.keys({...presidentialPowerObj})[0] === 'peekedLoyalty') {
                        this.setPresidentialPower('peekedLoyalty');
                        const playerName = presidentialPowerObj['peekedLoyalty']['playerName'];
                        const playerRole = presidentialPowerObj['peekedLoyalty']['playerRole'];
                        const presPowerObj = {playerName: playerName, playerRole: playerRole};
                        this.setPresidentialPowerObj(presPowerObj);
                        console.log('Pres power obj: ' + this.state.presidentialPowerObj)
                    }
                    break;
                case 'Cards':
                    this.setCards(msg['Cards']);
                    break;
                case 'dead':
                    this.setState({'gameState': 'Dead'});
                    this.clientRef.disconnect();
                    break;
                default:
                    console.error('Weirdness handling queue reply.')
            }
        }
    };

    sendMessage = (url, msg) => {
        console.log(url + ': ' + JSON.stringify(msg));
        this.clientRef.sendMessage(url, JSON.stringify(msg));
    };
}

export default App;
