import React, { PureComponent } from 'react';
import SockJsClient from 'react-stomp';

class Websocket extends PureComponent {
    sendMessage = (msg) => {
        this.clientRef.sendMessage('/app/register', msg);
    };

    render = () => (
        <SockJsClient
            url='http://localhost:8080/secrethit'
            topics={['/topic/registrations']}
            onMessage={(msg) => { console.log(msg); }}
            ref={(client) => { this.clientRef = client }}
        />
    )
}

export default Websocket;
