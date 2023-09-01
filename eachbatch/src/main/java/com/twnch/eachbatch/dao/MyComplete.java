package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.service.activemq.MyService;
import com.twnch.eachbatch.util.PattenFinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MyComplete {
    @Autowired
    private PattenFinder pattenFinder;
    @Autowired
    private MyService myService;
    public void myEntity2StatusChangeTo9(List<MyEntity2> sendToComplete){
        for(MyEntity2 e2 : sendToComplete){
            e2.setSTATUS("9");
        }
        myService.update(sendToComplete);
    }

}
