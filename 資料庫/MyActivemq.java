package com.twnch.eachbatch.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.util.PattenFinder;
import com.twnch.eachbatch.util.PropertiesLoader;
import com.twnch.eachbatch.service.activemq.MessageSender;
import com.twnch.eachbatch.service.activemq.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Controller
public class MyActivemq {
    private final MyService myService;
    private final MessageSender messageSender;

    @Autowired
    public MyActivemq(MyService myService, MessageSender messageSender, MessageSender messageSender1) {
        this.myService = myService;
        this.messageSender = messageSender;
    }

    @Autowired
    PattenFinder pattenFinder;

    public void pushToActivemq(){
        Properties properties = PropertiesLoader.loadProperties("application.properties");
        if (properties == null) {
            log.info("Failed to load configuration.");
            return;
        }

        int threadSleepTime = Integer.parseInt(properties.getProperty("sleepDurationForActivemq"));
        try {
            Thread.sleep(threadSleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<MyEntity2> processingList = new ArrayList<>();
        List<MyEntity2> cutInLineList = new ArrayList<>();

        int intervalInSecondsForActivemq = Integer.parseInt(properties.getProperty("intervalInSecondsForActivemq"));
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        // Schedule a task to run every X seconds
        executorService.scheduleAtFixedRate(() -> {
            String myBizdate = properties.getProperty("nowDate");
            List<MyEntity> myCutList;
            String myProcseq = "";
            // 確保我一筆資料全部做完才會撈新的
            if(processingList.isEmpty() && cutInLineList.isEmpty()) {

                myCutList = myService.getCutByTable1(myBizdate);
                List<MyEntity> myEntityList;
                MyEntity myEntity;


                // 從FLCONTROLTAB撈優先或撈順序
                if (!myCutList.isEmpty()) {
                    myEntity = myCutList.get(0);
                    myProcseq = myCutList.toString();
                } else {
                    myEntityList = myService.getOne(myBizdate);
                    myEntity = myEntityList.get(0);
                    myProcseq = myEntityList.toString();
                }

                // 更改狀態避免重複撈
                myEntity.setSTATUS("4");
                myService.update(myEntity);

                // 資料撈回來找PROCSEQ
                Pattern procseqPattern = Pattern.compile("PROCSEQ=(\\d+)");
                Matcher procseqMatcher = procseqPattern.matcher(myProcseq);

                int procSeqValue = 0;
                if (procseqMatcher.find()) {
                    procSeqValue = Integer.parseInt(procseqMatcher.group(1));
                    log.info("First PROCSEQ value: " + procSeqValue);
                } else {
                    log.info("PROCSEQ value not found");
                }

                // ***不會去抓procseq 2 導致allList為空?***
                List<MyEntity2> allList;
//            try {
                allList = myService.getSelected(myBizdate, procSeqValue);
                //log.info("allList: " + allList.toString());
//            } catch (DataAccessException e) {
//                log.info(String.valueOf(e));
//            }

                if (!myCutList.isEmpty()) {
                    cutInLineList.addAll(allList);
                } else {
                    processingList.addAll(allList);
                }
            }

            // 撈FLCONTROLTAB的PROCCOUNT PROCAMT
            String procCountString = pattenFinder.findMyPatten("PROCCOUNT", myProcseq);
            log.info(procCountString);

            //Pattern procAmtPattern = Pattern.compile("PROCAMT=(\\d+)");
            String procAmtString = pattenFinder.findMyPatten("PROCAMT", myProcseq);
            log.info(procAmtString);

            // cutInLineList空與否決定處理哪一個list
            MyEntity2 myEntity2;
            if(!cutInLineList.isEmpty()) {
                myEntity2 = cutInLineList.get(0);
                cutInLineList.remove(0);
            }
            else{
                myEntity2 = processingList.get(0);
                processingList.remove(0);
            }

            // processingList首筆資料處理後發送
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String json = objectMapper.writeValueAsString(myEntity2);
                //log.info(json);
                messageSender.sendMessage("myQueue", json);
            } catch (JsonProcessingException e) {
                log.info(String.valueOf(e));
            }

            // json送到Queue後完成 FLACHPTAB狀態改成9
            myEntity2.setSTATUS("9");
            myService.update(myEntity2);
            if(processingList.isEmpty() && cutInLineList.isEmpty()){
                List<MyEntity> completeList = myService.getComplete(myBizdate);
                log.info(completeList.toString());
                // 我一次只會把一筆status 4 改成5 如果有殘留的status 4會改不了
                MyEntity myEntity = completeList.get(0);
                myEntity.setSTATUS("5");
                myService.update(myEntity);
            }
        }, 0, intervalInSecondsForActivemq, TimeUnit.SECONDS);
    }


}







