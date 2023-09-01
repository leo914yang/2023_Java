package com.twnch.eachbatch.controller;

import com.twnch.eachbatch.dao.MyActivemq;
import com.twnch.eachbatch.dao.MyActivemq2;
import com.twnch.eachbatch.dao.MyDao;
import com.twnch.eachbatch.service.activemq.Consumer;
import com.twnch.eachbatch.util.MyScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Controller;

import javax.jms.TextMessage;
import java.io.IOException;

@Slf4j
@Controller
@EnableJms
public class MyController implements CommandLineRunner {
    @Autowired
    private MyDao myDao;
    @Autowired
    private MyActivemq myActivemq;
    @Autowired
    private MyActivemq2 myActivemq2;
    @Autowired
    private Consumer consumer;
    @Autowired
    private MyScanner myScanner;

    @Override
    public void run(String... args) {
        try {
            myActivemq.pushToActivemq();
            myDao.dataToFlcontroltab();
        } catch (InterruptedException e) {
            log.info(e.toString());
        }
        consumer.receive();
        myScanner.tableScanner();
    }

}



