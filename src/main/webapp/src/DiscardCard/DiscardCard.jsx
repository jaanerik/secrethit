import React, {Fragment, PureComponent} from 'react';
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import GameStateContext from "../GameStateContext";
import Image from 'react-bootstrap/Image';

function renderCard(card, sendDiscardRequest, changeShow = false) {
    if (card === null) return null;
    return <div onClick={() => {
        changeShow();
        sendDiscardRequest(card.toLowerCase());
    }}>
        <Image src={card.toLowerCase() === 'liberal' ?
            "./img/liberalPolicy.png" : "./img/fascistPolicy.png"
        } rounded/>
        {() => {
            if (card === "LIBERAL" || card === "FASCIST") return <p>(OLD)</p>
        }}
    </div>
}

function CardsModal(props) {
    return (
        <Modal show={props.show} onHide={() => {
        }} aria-labelledby="contained-modal-title-vcenter">
            <Modal.Body className="show-grid">
                <Container>
                    <Row>
                        {props.cards.filter(it => it != null).map((card, index) => {
                            return <Col xs={4} md={3} key={index}>
                                {renderCard(
                                    card, props.sendDiscardRequest, props.changeShow
                                )}
                            </Col>
                        })}
                    </Row>
                </Container>
            </Modal.Body>
        </Modal>
    );
}

function renderButtonText(isPeekingCards) {
    if (isPeekingCards) return 'Peek next 3 cards';
    return 'Choose a card to discard';
}

export default class DiscardCard extends PureComponent {
    static contextType = GameStateContext;

    state = {
        show: false
    };

    changeShow = () => {
        this.setState({show: !this.state.show}, () => {
            console.log('Changed modal show: ', this.state.show);
        });
    };

    render = () => (
        <Fragment>
            <Button variant="primary" onClick={() => this.changeShow()} className="btn btn-default secret-button">
                {renderButtonText(this.context.presidentialPower === 'peekedCards')}
            </Button>

            <CardsModal
                show={this.state.show}
                sendDiscardRequest={(card) => this.sendDiscardRequest(card)}
                cards={this.context.cards}
                changeShow={this.changeShow}
            />
        </Fragment>
    );

    sendDiscardRequest = (card) => {
        const indexOfDiscardedCard = this.context.cards.indexOf(card);
        const discardedCard = this.context.cards[indexOfDiscardedCard];
        if (this.context.presidentialPower !== 'peekedCards') {
            this.context.cards.splice(indexOfDiscardedCard, 1);
            this.context.sendMessage('/app/discard', {cards: this.context.cards, discardedCard: discardedCard});
        } else {
            this.context.sendMessage('/app/discard', {cards: [''], discardedCard: ''})
        }
        console.log('Currently isPeek: ', this.context.presidentialPower === 'peekedCards');
        this.context.setCards([]);
        console.log('Now cards2 ', this.context.cards);
        this.context.setExtraInfoJSON('{}')
        this.context.setPresidentialPower('')
    }
}
