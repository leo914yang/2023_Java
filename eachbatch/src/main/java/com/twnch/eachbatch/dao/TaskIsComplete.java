package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.service.activemq.MyService;
import com.twnch.eachbatch.util.MyMoveFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Component
public class TaskIsComplete {

    @Autowired
    private MyService myService;

    @Autowired
    private MyMoveFile myMoveFile;

    @Value("${nowDate}")
    private String myBizdate;
    @Value("${pending}")
    private String pending;
    @Value("${result}")
    private String result;

    public void isComplete(List<MyEntity2> processingList, List<MyEntity2> cutInLineList){
        try {
            if (processingList.isEmpty() && cutInLineList.isEmpty()) {
                List<MyEntity> completeList = myService.getComplete(myBizdate);
                MyEntity myEntity = completeList.get(0);
                myEntity.setSTATUS("5");
                Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());
                myEntity.setLASTMODIFYDT(endTimestamp);
                myService.update(myEntity);
                myMoveFile.myMove(pending, result);
                log.info("資料完成, 狀態更新為5, 資料為: " + myEntity);
                log.info("呼叫MyMoveFile檢查重複後移動檔案至result");
            }
        }catch (Exception e){
            log.error("TaskIsComplete發生異常: " + e);
        }
    }
}
