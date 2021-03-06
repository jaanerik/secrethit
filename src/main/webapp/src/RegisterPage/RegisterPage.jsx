import React, { PureComponent } from 'react';

import JoinGame from "./JoinGame";
import PlayersList from "./PlayersList";

export default class RegisterPage extends PureComponent {
    // constructor(props) {
    //     super(props);
    // }

    render = () => (
        <div>
            <section className="secret-slice" id="boxes">
                <div className="secret-container">
                    <div className="secret-gitter-row">
                        <div id="title-content" className="container secret-gitter-medium secret-card-gitter">
                            <h1>Secret Hitler</h1>
                        </div>
                    </div>
                    <hr />
                    <div className="secret-gitter-row">
                        <div id="join-content" className="container secret-gitter-small secret-card-gitter">
                            <h2>Join</h2>
                            <div className="row">
                                <div className="col-md-6">
                                    <JoinGame/>
                                </div>
                            </div>
                        </div>
                        <div id="joined-content" className="container secret-gitter-small secret-card-gitter">
                            <h2>Joined</h2>
                            <div className="row">
                                <div className="col-md-12">
                                    <PlayersList />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>


            <section className="secret-slice" id="about">
                <div className="secret-container">
                    <hr />
                    <div className="secret-gitter-row">
            			<div className="secret-gitter-medium">
            				<h2>What Is Secret<span className="secret-hide-medium secret-hide-big"> </span><span
            					className="secret-hide-small">&nbsp;</span>Hitler?</h2>
            				<div className="secret-split">
            					<div className="secret-split-content">
            						<p>Secret Hitler is a social deduction game for 5-10 people about finding and
            							stopping the Secret Hitler.</p><p>Players
            						are secretly divided into two teams: the liberals, who have a majority,
            						and the fascists, who are hidden to everyone but each other. If the
            						liberals can learn to trust each other, they have enough votes to
            						control the elections and save the day. But the fascists will say
            						whatever it takes to get elected, advance their agenda, and win the
            						game.</p>
            					</div>
            				</div>
            			</div>
            		</div>
            	</div>
            </section>
        </div>
    )
};
