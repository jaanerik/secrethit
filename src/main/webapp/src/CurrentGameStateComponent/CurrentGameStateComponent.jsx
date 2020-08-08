import React, {PureComponent} from 'react';
import GameStateContext from "../GameStateContext";

export default class CurrentGameStateComponent extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        this.renderCurrentState()
    );

    renderCurrentState = () => {
        return (
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        <table className="gameStateTable">
                            <thead>
                            <tr>
                                <th>Voter</th>
                                <th>Result</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td className="attr">President</td>
                                <td>{this.context.president}</td>
                            </tr>
                            <tr>
                                <td className="attr">Chancellor</td>
                                <td>{this.context.chancellor}</td>
                            </tr>
                            <tr>
                                <td className="attr">Cards left in pack</td>
                                <td>{this.context.cardPackSize}</td>
                            </tr>
                            <tr>
                                <td className="attr">Failed governments</td>
                                <td>{this.context.nullGovernments}</td>
                            </tr>
                            <tr>
                                <td className="attr">Fascist policies</td>
                                <td>{this.context.fascistCardsPlayed}</td>
                            </tr>
                            <tr>
                                <td className="attr">Liberal policies</td>
                                <td>{this.context.liberalCardsPlayed}</td>
                            </tr>
                            <tr>
                                <td>Players</td>
                                <td>{this.context.players.length}</td>
                            </tr>
                            <tr>
                                <td className="attr">Last government</td>
                                <td>{this.context.lastGovernment}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        )
    };
}
