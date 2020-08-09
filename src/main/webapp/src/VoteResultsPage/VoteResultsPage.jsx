import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";
import CurrentGameStateComponent from "../CurrentGameStateComponent/CurrentGameStateComponent";
import DiscardCard from "../DiscardCard/DiscardCard";

export default class VoteResultsPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            {this.renderCards()}
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        {this.renderResults()}
                        <hr/>
                    </div>
                </div>
            </section>
            <CurrentGameStateComponent/>
        </Fragment>
    );

    renderCards = () => {
        if (this.context.cards.length !== 0) return <DiscardCard />;
        else return null
    };

    renderResults = () => {
            return <table id="conversation" className="gameStateTable">
                <thead>
                <tr>
                    <th>Voter</th>
                    <th>Result</th>
                </tr>
                </thead>
                <tbody id="registrations">
                {JSON.parse(this.context.extraInfo).map(this.renderVote)}
                </tbody>
            </table>
    };

    renderVote = (playerJSON, index) => (
        <tr key={index}>
            <td>
                {Object.keys(playerJSON)[0]}
            </td>
            <td>
                {playerJSON[Object.keys(playerJSON)[0]]}
            </td>
        </tr>
    )
};
