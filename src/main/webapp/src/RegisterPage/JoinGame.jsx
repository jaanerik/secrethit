import React, {PureComponent} from 'react';

import GameStateContext from "../GameStateContext";

export default class JoinGame extends PureComponent {
    static contextType = GameStateContext;

    state = {
        name: '',
    };

    render = () => (
        <div className="form-inline">
            <div className="form-group">
                <input
                    type="text"
                    className="form-control secret-button"
                    value={this.state.name}
                    onChange={this.handleNameChange}
                    placeholder="Your name here..."
                />
            </div>
            <button
                type="button"
                className="btn btn-default secret-button"
                onClick={this.register}
            >
                <span>Join</span>
            </button>
            <button
                type="button"
                disabled={this.context.players.length < 5}
                className="btn btn-default secret-button"
                onClick={this.startGame}
            >
                <span>Start the Game</span>
            </button>
        </div>
    );

    handleNameChange = (event) => {
        this.setState({name: event.target.value});
    };

    startGame = () => {
        console.log('Sending start message');
        this.context.sendMessage('/app/register', {name: '$start$'})
    };

    register = () => {
        if (this.state.name !== '$start$') this.context.setMyName(this.state.name);
        this.context.sendMessage('/app/register', {name: this.state.name})
    }
}
