package com.twnch.eachbatch.dao;

import com.google.gson.Gson;
import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.service.activemq.MessageSender;
import com.twnch.eachbatch.service.activemq.MyService;
import com.twnch.eachbatch.util.MyCountAndAmt;
import com.twnch.eachbatch.util.PattenFinder;
import com.twnch.eachbatch.util.SelectData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class MyActivemq2 {
    private final MyService myService;
    private final MessageSender messageSender;
    @Value("${sleepDurationForActivemq}")
    private int threadSleepTime;
    @Value("${intervalInSecondsForActivemq}")
    private int intervalInSecondsForActivemq;
    @Value("${nowDate}")
    private String myBizdate;

    @Autowired
    public MyActivemq2(MyService myService, MessageSender messageSender) {
        this.myService = myService;
        this.messageSender = messageSender;
    }

    @Autowired
    private TaskIsComplete taskIsComplete;
    @Autowired
    private PattenFinder pattenFinder;
    @Autowired
    private SelectData selectData;
    @Autowired
    private MyCountAndAmt myCountAndAmt;

    public void pushToActivemq() throws InterruptedException {
        Thread.sleep(threadSleepTime);
        List<MyEntity2> processingList = new ArrayList<>();
        List<MyEntity2> cutInLineList = new ArrayList<>();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        // Schedule a task to run every X seconds
        executorService.scheduleAtFixedRate(() -> {
            List<MyEntity2> myEntity2List = myService.getAllDataByDate(myBizdate);
            
            }, 0, intervalInSecondsForActivemq, TimeUnit.SECONDS);
    }
}







