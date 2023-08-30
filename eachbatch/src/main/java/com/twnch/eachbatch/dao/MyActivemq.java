package com.twnch.eachbatch.dao;

import com.google.gson.Gson;
import com.twnch.eachbatch.util.SelectData;
import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.util.PattenFinder;
import com.twnch.eachbatch.service.activemq.MessageSender;
import com.twnch.eachbatch.service.activemq.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class MyActivemq {
    private final MyService myService;
    private final MessageSender messageSender;
    @Value("${sleepDurationForActivemq}")
    private int threadSleepTime;
    @Value("${intervalInSecondsForActivemq}")
    private int intervalInSecondsForActivemq;
    @Value("${nowDate}")
    private String myBizdate;

    @Autowired
    public MyActivemq(MyService myService, MessageSender messageSender) {
        this.myService = myService;
        this.messageSender = messageSender;
    }

    @Autowired
    private TaskIsComplete taskIsComplete;
    @Autowired
    private PattenFinder pattenFinder;
    @Autowired
    private SelectData selectData;

    public void pushToActivemq() throws InterruptedException {
        Thread.sleep(threadSleepTime);
        List<MyEntity2> processingList = new ArrayList<>();
        List<MyEntity2> cutInLineList = new ArrayList<>();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        // Schedule a task to run every X seconds
        executorService.scheduleAtFixedRate(() -> {
            // 確保我一筆資料全部做完才會撈新的
            //log.info("pList: " + processingList.size() + " cList: " + cutInLineList.size());
            if (processingList.isEmpty() && cutInLineList.isEmpty()) {
                List<MyEntity> myCutList;
                List<MyEntity> myEntityList;

                String myProcseq = "";
                // myCutList存放Cut_In_Line = T的資料
                myCutList = myService.getCutByTable1(myBizdate);
                //log.info(myCutList.toString());
                MyEntity myEntity = new MyEntity();

                String tempProcseq;
                int procSeqValue;
                // 從FLCONTROLTAB撈優先或撈順序
                if (!myCutList.isEmpty()) {
                    myEntity = myCutList.get(0);
                    myProcseq = myCutList.toString();
                } else {
                    myEntityList = myService.getS2ByDate(myBizdate);
                    if (!myEntityList.isEmpty()) {
                        //log.info("!!!" + myEntityList.toString());
                        myEntity = myEntityList.get(0);
                        myProcseq = myEntityList.toString();
                    } else
                        return;
                }

                // Demo時秀出myProcseq
                //log.info("!!!: " + myProcseq);

                // 更改狀態避免重複撈
                myEntity.setSTATUS("4");
                myService.update(myEntity);
                log.info("資料處理中, 狀態為4, 資料為: " + myEntity);

                // 資料撈回來找PROCSEQ
                tempProcseq = pattenFinder.findMyPatten("PROCSEQ", myProcseq);
                procSeqValue = Integer.parseInt(tempProcseq.split("PROCSEQ=")[1]);
                //log.info("!!!First PROCSEQ value: " + procSeqValue);

                List<MyEntity2> allList;
                // 根據前面找出的順序(Cut_In_Line和ProcessSeq)去撈回指定資料存入allList
                allList = myService.getSelected(myBizdate, procSeqValue);

                // myCutList存入從資料庫撈回來 Cut_In_Line欄位為Y的List
                // 決定資料是存入插隊List還是一般List
                if (!myCutList.isEmpty()) {
                    cutInLineList.addAll(allList);
                } else {
                    processingList.addAll(allList);
                }
            }
            // 撈FLCONTROLTAB的PROCCOUNT PROCAMT 即時處理後更新資料
            MyEntity myEntityForCount = null;
            int procAmtInt = 0;
            int procCountInt = 0;
            Map<String, Object> countAndAmtMap = processCountAndAmt(myEntityForCount, procAmtInt, procCountInt);
            myEntityForCount = (MyEntity) countAndAmtMap.get("myEntityForCount");
            procAmtInt = (int) countAndAmtMap.get("procAmtInt");
            procCountInt = (int) countAndAmtMap.get("procCountInt");
            // cutInLineList為優先list
            List<MyEntity2> jsonList;
            // 呼叫selectData.selected處理優先判斷
            jsonList = selectData.selected(cutInLineList, processingList);
            // 利用Gson將最多兩筆json資料合併
            Gson gson = new Gson();
            String json = gson.toJson(jsonList);
            messageSender.sendMessage("myQueue", json);
            log.info("資料傳送至Queue, 資料為: " + json);
            // 傳送參數1去取出交易金額, 用參數3去扣掉取出的金額得出當前交易結果
            // 參數2用於指定要更新的FLCONTROLTAB資料
            selectData.sendAndUpdate(jsonList, myEntityForCount, procAmtInt, procCountInt);

            taskIsComplete.isComplete(processingList, cutInLineList);

        }, 0, intervalInSecondsForActivemq, TimeUnit.SECONDS);
    }

    public Map<String, Object> processCountAndAmt(MyEntity myEntityForCount, int procAmtInt, int procCountInt) {
        List<MyEntity> myEntityForCountList;
        String myEntityForCountString;
        String procCountString;
        String myEntityForProcAmt;
        String procAmtString;
        Map<String, Object> countAndAmtMap = new HashMap<>();
        try {
            myEntityForCountList = myService.getComplete(myBizdate);
            myEntityForCount = myEntityForCountList.get(0);
            myEntityForProcAmt = myEntityForCount.toString();
            // pattenFinder返回值為null 導致程式執行錯誤
            procAmtString = pattenFinder.findMyPatten("PROCAMT", myEntityForProcAmt);
            // 交易總金額
            procAmtInt = Integer.parseInt(procAmtString.split("PROCAMT=")[1]);
            myEntityForCountString = myEntityForCount.toString();
            procCountString = pattenFinder.findMyPatten("PROCCOUNT", myEntityForCountString);
            // procCountString的值為PROCCOUNT=X 因此將他split開
            procCountInt = (Integer.parseInt(procCountString.split("PROCCOUNT=")[1]));

        } catch (IndexOutOfBoundsException e) {
            // 如果無法從 myEntityForCountList 中獲取資料
            // 處理例外，記錄或顯示錯誤訊息，執行適當的後續處理
            log.info(e.toString());
        } catch (NullPointerException e) {
            // 如果 pattenFinder 返回 null
            // 處理例外，記錄或顯示錯誤訊息，執行適當的後續處理
            log.info(e.toString());
        } catch (NumberFormatException e) {
            // 如果轉換成整數失敗
            // 處理例外，記錄或顯示錯誤訊息，執行適當的後續處理
            log.info(e.toString());
        }
        countAndAmtMap.put("myEntityForCount", myEntityForCount);
        countAndAmtMap.put("procAmtInt", procAmtInt);
        countAndAmtMap.put("procCountInt", procCountInt);
        return countAndAmtMap;
    }


}







