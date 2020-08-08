import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";
import CurrentGameStateComponent from "../CurrentGameStateComponent/CurrentGameStateComponent";

export default class VotingPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        {this.renderAfterCandidateIsChosenOrDefault(this.context.extraInfo === "Candidate")}
                    </div>
                </div>
            </section>
            <CurrentGameStateComponent />
        </Fragment>
    );

    renderPlayer = (player, index) => (
        <tr key={index}>
            <td>
                <button
                    className="secret-button"
                    onClick={() => this.sendChancellorCandidate(player)}
                >{player}</button>
            </td>
        </tr>
    );

    sendChancellorCandidate = (playerName) => {
        console.log('Sending chancellor candidate ' + playerName);
        this.context.sendMessage('/app/voting', {chancellor: playerName, yayOrNay: ""})
    };

    sendYayOrNay = (yayOrNay) => {
        console.log('I voted ' + yayOrNay);
        this.context.sendMessage('/app/voting', {chancellor: "", yayOrNay: yayOrNay})
    };

    renderYayOrNayWithChoices = () => {
        return <Fragment>
            <h3>Proposed government is</h3>
            <p>President: {this.context.president}, Chancellor: {this.context.chancellor}</p>
            <button
                className="secret-button"
                onClick={() => this.sendYayOrNay("Yay")}
            >Yay
            </button>
            <button
                className="secret-button"
                onClick={() => this.sendYayOrNay("Nay")}
            >Nay
            </button>
        </Fragment>
    };

    renderAfterCandidateIsChosenOrDefault = (isChancellorChosen) => {
        if (isChancellorChosen) {
            return this.renderYayOrNayWithChoices()
        } else {
            return this.renderForPresidentOrDefault(this.context.myName === this.context.president)
        }
    };

    renderForPresidentOrDefault = (isPresident) => {
        if (isPresident) {
            return <Fragment>
                <h2>Choose your chancellor!</h2>
                <table id="choices" className="table table-striped">
                    <tbody id="registrations">
                    {this.context.players.filter((s) => {
                        return s !== this.context.myName
                    }).map(this.renderPlayer)}
                    </tbody>
                </table>
            </Fragment>
        } else {
            return <Fragment><h3>President is choosing a chancellor</h3></Fragment>
        }
    }
};
