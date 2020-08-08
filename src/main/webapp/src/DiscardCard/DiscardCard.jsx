import React, {Fragment, PureComponent} from 'react';
import Card from 'react-bootstrap/Card'
import Button from 'react-bootstrap/Button';
import Overlay from 'react-bootstrap/Overlay'
import GameStateContext from "../GameStateContext";

export default class DiscardCard extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            //TODO: <Overlay/>
            <Card style={{ width: '18rem' }}>
                <Card.Img variant="top" src="holder.js/100px180" />
                <Card.Body>
                    <Card.Title>Card Title</Card.Title>
                    <Card.Text>
                        Some quick example text to build on the card title and make up the bulk of
                        the card's content.
                    </Card.Text>
                    <Button variant="primary">Go somewhere</Button>
                </Card.Body>
            </Card>
        </Fragment>
    );

    sendResetRequest = () => {
        console.log('Sending reset request ');
        this.context.sendMessage('/app/reset', {message: "$reset$"})
    };
}
