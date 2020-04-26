package com.jaanerikpihel.secrethit.config

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    private val logger = KotlinLogging.logger {}

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        logger.debug { "Configuring message broker" }
        config.setApplicationDestinationPrefixes("/app")
        config.enableSimpleBroker("/topic", "/queue")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/secrethit")
//                .addInterceptors(object : HandshakeInterceptor {
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
                .addInterceptors(HttpSessionHandshakeInterceptor())
                .withSockJS()
    }
}
