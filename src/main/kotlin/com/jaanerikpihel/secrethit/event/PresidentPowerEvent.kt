package com.jaanerikpihel.secrethit.event

import com.jaanerikpihel.secrethit.model.PresidentPowerMessage
import com.jaanerikpihel.secrethit.model.RegisterMessage
import org.springframework.context.ApplicationEvent
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

class PresidentPowerEvent(source: Any, val presidentPowerMessage: PresidentPowerMessage) : ApplicationEvent(source) {}