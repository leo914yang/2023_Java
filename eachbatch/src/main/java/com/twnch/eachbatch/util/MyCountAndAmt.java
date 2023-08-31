package com.twnch.eachbatch.util;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.service.activemq.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MyCountAndAmt {
    @Autowired
    private MyService myService;
    @Autowired
    private PattenFinder pattenFinder;
    @Value("${nowDate}")
    private String myBizdate;

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
