package com.twnch.eachbatch.util;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.service.activemq.MessageSender;
import com.twnch.eachbatch.service.activemq.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SelectData {

    @Autowired
    PattenFinder pattenFinder;

    @Autowired
    MyService myService;

    @Autowired
    private MessageSender messageSender;

    public List<MyEntity2> selected(List<MyEntity2> cutInLineList, List<MyEntity2> processingList) {
        List<MyEntity2> selectedData = new ArrayList<>();
        // 優先從list1選取資料
        while (selectedData.size() < 2 && !cutInLineList.isEmpty()) {
            selectedData.add(cutInLineList.remove(0));
        }

        // 如果list1的資料筆數不足兩筆，從list2選取剩餘的資料
        while (selectedData.size() < 2 && !processingList.isEmpty()) {
            selectedData.add(processingList.remove(0));
        }
        //log.info("selectedData = " + selectedData);
        return selectedData;
    }

    public void sendAndUpdate(List<MyEntity2> myEntity2List, MyEntity myEntityForCount, int procAmtInt, int procCountInt){
        int totalAmt = procAmtInt;
        int totalCount = procCountInt;
        while(!myEntity2List.isEmpty()){
            String myEntityForAmtString = myEntity2List.get(0).toString();
            //log.info("myEntity2: " + myEntityForAmtString);
            String myEntity2ProcAmtString = pattenFinder.findMyPatten("ACH_AMT", myEntityForAmtString);
            int myEntity2ProcAmtInt = Integer.parseInt(myEntity2ProcAmtString.split("ACH_AMT=")[1]);
            //log.info("procAmtInt = " + procAmtInt);
            //log.info("myEntity2ProcAmtInt = " + myEntity2ProcAmtInt);
            totalAmt -= myEntity2ProcAmtInt;
            totalCount -= 1;
            //log.info("sum = " + sum);
            myEntityForCount.setPROCAMT(totalAmt);
            myEntityForCount.setPROCCOUNT(totalCount);
            myService.update(myEntityForCount);
            log.info("資料更新, PROCAMT = " + totalAmt + ", PROCCOUNT = " + totalCount);
            myEntity2List.get(0).setSTATUS("9");
            myService.update(myEntity2List.get(0));
            log.info("交易完成, 狀態更新為9, 交易為: " + myEntity2List.get(0));
            myEntity2List.remove(0);
        }
    }
}
