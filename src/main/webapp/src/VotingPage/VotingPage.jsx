import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";

export default class VotingPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        <h2>Choose your chancellor!</h2>
                        <table id="choices" className="table table-striped">
                            <tbody id="registrations">
                            {this.context.players.map(this.renderPlayer)}
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

        </Fragment>
    );

    renderPlayer = (player, index) => (
        <tr key={index}>
            <td>
                <button className="secret-button">{player}</button>
            </td>
        </tr>
    )
};
