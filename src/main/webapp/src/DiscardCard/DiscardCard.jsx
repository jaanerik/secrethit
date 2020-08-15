import React, {Fragment, PureComponent} from 'react';
import Button from 'react-bootstrap/Button';
import Modal from 'react-bootstrap/Modal'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import GameStateContext from "../GameStateContext";
import Image from 'react-bootstrap/Image';

function renderCard(card, sendDiscardRequest, changeShow, setCardsEmpty) {
    if (card === null) return null;
    return <div onClick={() => {
        // setCardsEmpty();
        changeShow();
        sendDiscardRequest(card.toLowerCase());
    }}>
        <Image src={card.toLowerCase() === 'liberal' ?
            "./img/liberalPolicy.png" : "./img/fascistPolicy.png"
        } rounded/>
        {() => {
            if (card === "LIBERAL") return <p>(OLD)</p>
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
                                    card, props.sendDiscardRequest, props.changeShow, props.setCardsEmpty
                                )}
                            </Col>
                        })}
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
            console.log('Changed modal show: ', this.state.show);
        });
    };

    setCardsEmpty = () => {
        this.setState({cards: []}, () => {
            console.log('Changed cards: ', this.state.cards);
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
                cards={this.context.cards}
                changeShow={this.changeShow}
                setCardsEmpty={this.setCardsEmpty}
            />
        </Fragment>
    );

    sendDiscardRequest = (card) => {
        const indexOfDiscardedCard = this.context.cards.indexOf(card);
        this.context.cards.splice(indexOfDiscardedCard, 1);
        this.context.sendMessage('/app/discard', {cards: this.context.cards});
        console.log('Now cards ', this.context.cards);
        // this.context.cards = [];
        this.props.setCards([]);
        console.log('Now cards2 ', this.context.cards);
        this.context.extraInfo = ""
    }
}
