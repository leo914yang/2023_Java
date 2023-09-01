package com.twnch.eachbatch.service.activemq;

import com.google.gson.Gson;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.util.SelectData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SendToQueue {
    private List<MyEntity2> cutInLineList = new ArrayList<>();
    private List<MyEntity2> processingList = new ArrayList<>();
    @Autowired
    private SelectData selectData;
    @Autowired
    private MessageSender messageSender;

    public void messageToQueue(List<MyEntity2> cutInLineList, List<MyEntity2> processingList){
        if(!cutInLineList.isEmpty())
            this.cutInLineList.addAll(cutInLineList);
        if(!processingList.isEmpty())
            this.processingList.addAll(processingList);
        List<MyEntity2> jsonList = new ArrayList<>();
        if(!this.cutInLineList.isEmpty() || !this.processingList.isEmpty())
            jsonList = selectData.selected(this.cutInLineList, this.cutInLineList);
        Gson gson = new Gson();
        String json = gson.toJson(jsonList);
        messageSender.sendMessage("myQueue", json);
        log.info("資料傳送至Queue, 資料為: " + json);
    }
}
