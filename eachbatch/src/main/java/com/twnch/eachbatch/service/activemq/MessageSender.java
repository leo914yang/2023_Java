package com.twnch.eachbatch.service.activemq;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private final JmsTemplate jmsTemplate;

    public MessageSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(String destination, String jsonMessage) {
        jmsTemplate.convertAndSend(destination, jsonMessage);
    }
}
