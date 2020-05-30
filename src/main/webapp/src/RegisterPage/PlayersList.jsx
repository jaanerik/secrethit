import React, { PureComponent } from 'react';

import GameStateContext from "../GameStateContext";

export default class PlayersList extends PureComponent {
  static contextType = GameStateContext;

  render = () => (
    <table id="conversation" className="table table-striped">
      <tbody id="registrations">
        {this.context.players.map(this.renderPlayer)}
      </tbody>
    </table>
  )

  renderPlayer = (player, index) => (
    <tr key={index}>
      <td>
        {player.name}
      </td>
    </tr>
  )
}
