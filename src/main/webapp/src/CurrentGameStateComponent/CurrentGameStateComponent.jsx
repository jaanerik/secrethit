import React, {PureComponent} from 'react';
import GameStateContext from "../GameStateContext";

export default class CurrentGameStateComponent extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        this.renderCurrentState()
    );

    renderCurrentState = (lastGovernment) => {
        function formatLastGovernment() {
            if (lastGovernment.first !== '') { return '' }
            return lastGovernment.first +
            ' and ' + lastGovernment.second
        }

        return (
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        <table className="gameStateTable">
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
                                <td className="attr">Players</td>
                                <td>{this.context.players.length}</td>
                            </tr>
                            <tr>
                                <td className="attr">Last government</td>
                                <td>{() => formatLastGovernment(this.context.lastGovernment)}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        )
    };
}
