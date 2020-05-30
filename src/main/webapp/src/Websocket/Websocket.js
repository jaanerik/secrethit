import React from 'react';
import SockJsClient from 'react-stomp';

class Websocket extends React.Component {
    constructor(props) {
        super(props);
    }

    sendMessage = (msg) => {
        this.clientRef.sendMessage('/app/register', msg);
    };

    render() {
        return (
            <div>
                <SockJsClient url='http://localhost:8080/secrethit' topics={['/topic/registrations']}
                              onMessage={(msg) => { console.log(msg); }}
                              ref={ (client) => { this.clientRef = client }} />
            </div>
        );
    }
}

export default Websocket;
