import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";
import CurrentGameStateComponent from "../CurrentGameStateComponent/CurrentGameStateComponent";
import DiscardCard from "../DiscardCard/DiscardCard";
import PlayersCard from "../PlayersCard/PlayersCard";

export default class VoteResultsPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    {this.renderCards()}
                    {this.renderPlayers()}
                    <div className="secret-gitter-row">
                        {this.renderResults()}
                        <hr/>
                    </div>
                </div>
            </section>
            <CurrentGameStateComponent/>
        </Fragment>
    );

    renderPlayers = () => {
        if (this.context.presidentialPower === 'killPlayer' ||
            this.context.presidentialPower === 'peekLoyalty' ||
            this.context.presidentialPower === 'pickPresident' ||
            this.context.presidentialPower === 'peekedLoyalty')
            return <PlayersCard/>;
        else return null
    };

    renderCards = () => {
        console.log('presPower:' + this.context.presidentialPower);
        if (this.context.cards.length !== 0)
            return <DiscardCard setCards={this.props.setCards}/>;
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
