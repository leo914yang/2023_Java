package com.twnch.eachbatch.dao;

import com.google.gson.Gson;
import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.service.activemq.MessageSender;
import com.twnch.eachbatch.service.activemq.MyService;
import com.twnch.eachbatch.service.activemq.SendToQueue;
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
import java.util.concurrent.atomic.AtomicInteger;

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
    @Autowired
    private MyComplete myComplete;
    @Autowired
    private SendToQueue sendToQueue;

    public void pushToActivemq() throws InterruptedException {
        Thread.sleep(threadSleepTime);
        log.info("push start");
        List<MyEntity2> processingList = new ArrayList<>();
        List<MyEntity2> cutInLineList = new ArrayList<>();
        List<String> myCutProcseq = new ArrayList<>();
        List<String> myProcseq = new ArrayList<>();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        // Schedule a task to run every X seconds
        executorService.scheduleAtFixedRate(() -> {
            List<MyEntity> myEntityList = myService.getS2ByDate(myBizdate);
            if (myEntityList.isEmpty())
                return;
            List<MyEntity> tempMyEntityList = myEntityList;
            List<MyEntity> entitiesToRemove = new ArrayList<>();
            // 處理完cutinline排序
            try {
                for (int i=0;i<tempMyEntityList.size();i++) {
                    if (pattenFinder.findMyPatten("CUT_IN_LINE", tempMyEntityList.get(i).toString()).equals("Y")) {
                        myCutProcseq.add(pattenFinder.findMyPatten("PROCSEQ", tempMyEntityList.get(i).toString()));
                        entitiesToRemove.add(tempMyEntityList.get(i));
                        myEntityList.get(i).setSTATUS("4");
                        myService.update(myEntityList.get(i));
                        log.info("資料處理中, 狀態為4, 資料為: " + myEntityList.get(i));

                    }
                }
                tempMyEntityList.removeAll(entitiesToRemove);
                for (int j=0;j<tempMyEntityList.size();j++) {
                    myProcseq.add(pattenFinder.findMyPatten("PROCSEQ", tempMyEntityList.get(j).toString()));
                    myEntityList.get(j).setSTATUS("4");
                    myService.update(myEntityList.get(j));
                    log.info("資料處理中, 狀態為4, 資料為: " + myEntityList.get(j));
                }

                //log.info(myProcseq.toString());
            } catch (Exception e) {
                log.info(e.toString());
            }

            // 使用排序好的myProcseq來按照順序放資料
            List<MyEntity2> myEntity2List = myService.getAllDataByDate(myBizdate);
            List<MyEntity2> myEntity2DeleteList = new ArrayList<>();
            List<MyEntity2> sendToCompleteList = new ArrayList<>();
            for (int j=0;j<myCutProcseq.size();j++) {
                for (int i=0;i<myEntity2List.size();i++) {
                    if (pattenFinder.findMyPatten("PROCSEQ"
                            , myEntity2List.get(i).toString()).equals(myCutProcseq.get(j))) {
                        cutInLineList.add(myEntity2List.get(i));
                        sendToCompleteList.add(myEntity2List.get(i));
                        myEntity2DeleteList.add(myEntity2List.get(i));
                    }
                }
                // 完成後呼叫complete
                myComplete.myEntity2StatusChangeTo9(sendToCompleteList);
                sendToCompleteList.clear();
                myEntityList.get(j).setSTATUS("5");
            }
            myEntity2List.removeAll(myEntity2DeleteList);
            for (int j=0;j<myProcseq.size();j++) {
                for (int i=0;i<myEntity2List.size();i++) {
                    if (pattenFinder.findMyPatten("PROCSEQ"
                            , myEntity2List.get(i).toString()).equals(myProcseq.get(j))){
                        processingList.add(myEntity2List.get(i));
                        sendToCompleteList.add(myEntity2List.get(i));
                    }
                }
                // 每一圈s2都要呼叫一次complete
                myComplete.myEntity2StatusChangeTo9(sendToCompleteList);
                sendToCompleteList.clear();
                myEntityList.get(j).setSTATUS("5");
            }
            if(!cutInLineList.isEmpty() || !processingList.isEmpty())
                sendToQueue.messageToQueue(cutInLineList, processingList);

            cutInLineList.clear();
            processingList.clear();
            myEntityList.clear();
            entitiesToRemove.clear();
            myProcseq.clear();
            myCutProcseq.clear();
        }, 0, intervalInSecondsForActivemq, TimeUnit.SECONDS);
    }
}







