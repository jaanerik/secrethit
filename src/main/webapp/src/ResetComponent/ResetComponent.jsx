import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";

export default class ResetComponent extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            {this.renderResetOrNone(
                this.context.myName === this.context.players[0]
            )}
        </Fragment>
    );

    renderResetOrNone = (isFirst) => {
        if (isFirst) {
            return (
                <section className="secret-slice" id="boxes">
                    <div className="secret-container">
                        <div className="secret-gitter-row">
                            <button
                                className="btn btn-default secret-button"
                                onClick={this.sendResetRequest}
                            >Reset game
                            </button>
                        </div>
                    </div>
                </section>
            )
        } else {
            return null
        }
    };

    sendResetRequest = () => {
        console.log('Sending reset request ');
        this.context.sendMessage('/app/reset', {message: "$reset$"})
    };
}
