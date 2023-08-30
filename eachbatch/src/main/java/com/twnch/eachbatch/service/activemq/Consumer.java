package com.twnch.eachbatch.service.activemq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Slf4j
@Component
public class Consumer {

    private final JmsTemplate jmsTemplate;

    public Consumer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Value("${myDestination}")
    private String myDestination;

    @JmsListener(destination = "${myDestination}")
    public void receive() {
        String message = (String) jmsTemplate.receiveAndConvert(myDestination);
        log.info("Queue返回訊息: " + message);
    }

}

