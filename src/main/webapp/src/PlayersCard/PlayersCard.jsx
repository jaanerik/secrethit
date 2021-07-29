import React, {Fragment, PureComponent} from 'react';
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import GameStateContext from "../GameStateContext";
import Image from 'react-bootstrap/Image';

function renderCard(player, sendRequest, changeShow = false, index, presidentialPowerObj) {
    if (player === null) return null;
    console.log('presPowerObj in rendercard: ', presidentialPowerObj);

    function getPlayerString(player, presidentialPowerObj) {
        presidentialPowerObj = JSON.parse(presidentialPowerObj);
        console.log('player: ', player, 'obj:', presidentialPowerObj);
        if (presidentialPowerObj !== null && typeof presidentialPowerObj !== 'undefined') {
            console.log("Has playername!", Object.keys(presidentialPowerObj));
            if (Object.keys(presidentialPowerObj).includes('playerName') &&
                presidentialPowerObj['playerName'] === player)
                return <p>{player} ({presidentialPowerObj['playerRole']})</p>
        }
        return <p>{player}</p>
    }

    return <div onClick={() => {
        changeShow();
        sendRequest(player);
    }}>
        <div><Image width="100" height="200" src={"./img/player" + index + ".png"
        } rounded/>
            <p>{getPlayerString(player, presidentialPowerObj)}</p>
        </div>
    </div>
}

function CardsModal(props) {
    return (
        <Modal show={props.show} onHide={() => {
        }} aria-labelledby="contained-modal-title-vcenter">
            <Modal.Body className="show-grid">
                <Container>
                    <Row>
                        {props.players.filter(it => it !== props.myName).map((player, index) => {
                            return <Col xs={4} md={3} key={index}>
                                {renderCard(
                                    player, props.sendRequest, props.changeShow, index, props.presidentialPowerObj
                                )}
                            </Col>
                        })}
                    </Row>
                </Container>
            </Modal.Body>
        </Modal>
    );
}

function renderButtonText(presidentialPower) {
    if (presidentialPower === 'killPlayer') return 'Kill a player';
    if (presidentialPower === 'peekLoyalty') return 'Choose whose loyalty to see';
    if (presidentialPower === 'peekedLoyalty') return 'See loyalty';
    if (presidentialPower === 'pickPresident') return 'Pick next president';
    return 'Something is wrong';
}

export default class PlayersCard extends PureComponent {
    static contextType = GameStateContext;

    state = {
        show: false
    };

    changeShow = () => {
        this.setState({show: !this.state.show}, () => {
            console.log('Changed modal show: ', this.state.show);
            console.log('Pres power obj:', this.context.presidentialPowerObj)
        });
    };

    render = () => (
        <Fragment>
            <Button variant="primary" onClick={() => this.changeShow()} className="btn btn-default secret-button">
                {renderButtonText(this.context.presidentialPower)}
            </Button>

            <CardsModal
                show={this.state.show}
                sendRequest={(card) => this.sendRequest(card)}
                players={this.context.players}
                myName={this.context.myName}
                presidentialPowerObj={this.context.presidentialPowerObj}
                changeShow={this.changeShow}
            />
        </Fragment>
    );

    sendRequest = (card) => {
        const indexOfChosenPlayer = this.context.players.indexOf(card);
        const chosenPlayer = this.context.players[indexOfChosenPlayer];
        if (this.context.presidentialPower === 'killPlayer') {
            this.context.sendMessage('/app/presidentPower', {power: 'killedPlayer', object: chosenPlayer});
        } else if (this.context.presidentialPower === 'peekLoyalty') {
            this.context.sendMessage('/app/presidentPower', {power: 'peekLoyalty', object: chosenPlayer})
        } else if (this.context.presidentialPower === 'peekedLoyalty') {
            this.context.sendMessage('/app/presidentPower', {power: 'peekedLoyalty', object: ''})
        } else if (this.context.presidentialPower === 'pickPresident') {
            this.context.sendMessage('/app/presidentPower', {power: 'pickedPresident', object: chosenPlayer})
        }
        this.context.setPresidentialPower('')
    }
}
