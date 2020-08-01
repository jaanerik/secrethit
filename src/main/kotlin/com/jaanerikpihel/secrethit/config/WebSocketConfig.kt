package com.jaanerikpihel.secrethit.config

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import java.security.Principal
import java.util.*


@Configuration
@EnableWebSocketMessageBroker
open class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    private val logger = KotlinLogging.logger {}

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        logger.debug { "Configuring message broker" }
        config.setApplicationDestinationPrefixes("/app")
        config.enableSimpleBroker("/topic", "/queue")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/secrethit")
//                .addInterceptors(var object : HandshakeInterceptor {
//                    @Throws(Exception::class)
//                    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, attributes: Map<String, Any>): Boolean {
//                        if (request is ServletServerHttpRequest) {
//                            println(attributes)
//                        }
//                        return true
//                    }
//
//                    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, exception: Exception?) {}
//                })
                .setHandshakeHandler(MyHandshakeHandler())
                .addInterceptors(HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS()
    }

}

class StompPrincipal(uuid: String) : Principal {
    private var name: String = uuid

    override fun getName(): String {
        return name
    }
}

class MyHandshakeHandler : DefaultHandshakeHandler() {
    @Override
    override fun determineUser(request: org.springframework.http.server.ServerHttpRequest, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>): Principal? {
//        return super.determineUser(request, wsHandler, attributes)
        val uuid = UUID.randomUUID().toString()
        logger.info("UUID: $uuid")
        return StompPrincipal(uuid)
    }
}