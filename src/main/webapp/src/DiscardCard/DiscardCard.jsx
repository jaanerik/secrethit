import React, {Fragment, PureComponent, useState} from 'react';
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import GameStateContext from "../GameStateContext";
import Image from 'react-bootstrap/Image';

function renderCard(card, sendDiscardRequest) {
    if (card === null) return null;
    if (card.toLowerCase() === "liberal") {
        return <div onClick={() => sendDiscardRequest('liberal')}>
            <Image src="./img/liberalPolicy.png" rounded/>
            {() => {if (card === "LIBERAL") return <p>(OLD)</p>}}
        </div>
    }
    if (card.toLowerCase() === 'fascist') {
        return (<div onClick={() => sendDiscardRequest('fascist')}>
            <Image src="./img/fascistPolicy.png" rounded/>
            {() => {if (card === "FASCIST") return <p>(OLD)</p>}}
        </div>)
    }
    return null
}

function CardsModal(props) {
    return (
        <Modal show={props.show} onHide={() => {
        }} aria-labelledby="contained-modal-title-vcenter">
            <Modal.Body className="show-grid">
                <Container>
                    <Row>
                        <Col xs={4} md={3}>
                            {renderCard(props.firstCard, props.sendDiscardRequest)}
                        </Col>
                        <Col xs={4} md={3}>
                            {renderCard(props.secondCard, props.sendDiscardRequest)}
                        </Col>
                        <Col xs={4} md={3}>
                            {renderCard(props.thirdCard, props.sendDiscardRequest)}
                        </Col>
                    </Row>
                </Container>
            </Modal.Body>
        </Modal>
    );
}
export default class DiscardCard extends PureComponent {
    static contextType = GameStateContext;

    state = {
        show: false
    };

    changeShow = () => {
        this.setState({show: !this.state.show}, () => {
            console.log('Changed: ', this.state.show);
        });
    };


    render = () => (
        <Fragment>
            <Button variant="primary" onClick={() => this.changeShow()} className="btn btn-default secret-button">
                Choose which card to discard
            </Button>

            <CardsModal
                show={this.state.show}
                sendDiscardRequest={(card) => this.sendDiscardRequest(card)}
                firstCard={this.context.cards[0]}
                secondCard={this.context.cards[1]}
                thirdCard={this.sendThirdCard()}
            />
        </Fragment>
    );

    sendThirdCard = () => {
        if (this.context.cards.length === 2) return null;
        else return this.context.cards[2];
    };

    sendDiscardRequest = (card) => {
        const indexOfDiscardedCard = this.context.cards.indexOf(card);
        this.context.cards.splice(indexOfDiscardedCard, 1);
        this.context.sendMessage('/app/discard', {cards: this.context.cards});
        console.log('Discarded ', this.context.cards);
        this.context.cards = [];
        this.context.extraInfo = ""
    }
}
