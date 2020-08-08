import React, {Fragment, PureComponent} from 'react';
import GameStateContext from "../GameStateContext";
import CurrentGameStateComponent from "../CurrentGameStateComponent/CurrentGameStateComponent";

export default class EnactingPage extends PureComponent {
    static contextType = GameStateContext;

    render = () => (
        <Fragment>
            <CurrentGameStateComponent/>
        </Fragment>
    );
};
