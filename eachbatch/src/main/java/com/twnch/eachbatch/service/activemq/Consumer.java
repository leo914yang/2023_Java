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

    // 創建一個 JMS 監聽器, 並且這個監聽器會訂閱特定的 JMS 目的地
    // 當有消息到達該目的地時, @JmsListener 所標註的方法將會被自動觸發, 讓我能夠處理接收到的消息
    @JmsListener(destination = "${myDestination}")
    public void receive() {
        String message = (String) jmsTemplate.receiveAndConvert(myDestination);
        log.info("Queue返回訊息: " + message);
    }

}

