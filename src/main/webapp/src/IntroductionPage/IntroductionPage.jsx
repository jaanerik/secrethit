import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";

export default class IntroductionPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        <h2>Introduction</h2>
                        <h4>Your role: <b>{this.context.myRole}</b></h4>
                        {this.context.extraInfoJSON.split(';').map(this.renderInfo)}
                    </div>
                </div>
            </section>

        </Fragment>
    );

    renderInfo = (info, index) => (
        <p key={index}>{info}</p>
    )
};
