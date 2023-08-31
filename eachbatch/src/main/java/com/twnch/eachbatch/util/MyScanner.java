package com.twnch.eachbatch.util;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.service.activemq.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class MyScanner {
    @Value("${nowdate}")
    private String nowDate;
    @Autowired
    private MyService myService;

    @Scheduled(fixedRateString = "${intervalInSecondsForScanner}")
    public void tableScanner() {
        List<MyEntity> myEntityList = myService.getS2ByDate(nowDate);
        log.info("Automatic checking(Unprocessed files): " + myEntityList.size());
//        List<Object[]> myTest = myService.getPairingNotCut();
//        if (!myTest.isEmpty()) {
//            for (Object[] element : myTest) {
//                log.info(Arrays.toString(element));
//            }
//        }
    }
}
