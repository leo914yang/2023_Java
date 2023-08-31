package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.util.FileNameAndContent;
import com.twnch.eachbatch.util.MyMoveFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
public class MyDao {
    @Autowired
    private EachBatchRepository repository;
    @Autowired
    private EachBatchRepository2 repository2;
    @Autowired
    private MyMoveFile myMoveFile;
    @Autowired
    private FileNameAndContent fileNameAndContent;
    @Value("${sleepDurationForDao}")
    private int threadSleepTime;
    @Value("${intervalInSecondsForDao}")
    private int intervalInSecondsForDao;
    @Value("${source}")
    private String filePath;
    @Value("${source}")
    private String source;
    @Value("${historydata}")
    private String historydata;
    @Value("${pending}")
    private String pending;
    private List<String> fileNameArrayList = new ArrayList<>();
    private List<String> fileContentArrayList = new ArrayList<>();

    public void dataToFlcontroltab() throws InterruptedException {
        Thread.sleep(threadSleepTime);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        // Schedule a task to run every X seconds
        executorService.scheduleAtFixedRate(() -> {

            // 一般的counter++再多執行緒底下不是很安全
            AtomicInteger myCounter = new AtomicInteger(1);
            // 檢查是否有隱藏檔.DS_Store後將正確的檔案名稱加入fileNameArrayList
            // 正確的內容加入fileContentArrayList
            File directory = new File(filePath);
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().equals(".DS_Store")) {
                        fileNameArrayList.add(file.getName());
                    }
                }
                // 檔案讀取順序錯亂 因此先依照檔名進行排序後再將檔案讀出來
                Collections.sort(fileNameArrayList);
                fileNameArrayList.forEach(list -> fileContentArrayList = fileNameAndContent.readAndProcessFileContent(
                        filePath + "/" + list, fileContentArrayList));
            }

            // 雙層for迴圈 j控制第幾個檔案 i控制第幾筆交易
            for (int j = 0; j < fileNameArrayList.size(); j++) {
                String concatenatedLines = fileContentArrayList.get(j);
                String[] columns = concatenatedLines.split("\n");

                // columns[0] == "控制首錄", 1~columns.length - 2 == "明細錄", columns.length - 1 == "控制尾錄"
                MyEntity myEntity = new MyEntity();
                int indexStart = fileNameArrayList.get(j).lastIndexOf("/") + 1;
                int indexEnd = fileNameArrayList.get(j).lastIndexOf(".");
                // 控制procSeq, 每次讀取下一個檔案會+1
                int changePerCaseCounter = myCounter.getAndIncrement();

                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                myEntity.setSTART_TS(currentTimestamp);
                myEntity.setBATCHSEQ(fileNameArrayList.get(j).substring(indexStart, indexEnd));
                myEntity.setBIZDATE(columns[0].substring(9, 17));
                myEntity.setPROCSEQ(changePerCaseCounter);
                myEntity.setTOTALCOUNT(columns.length - 2);
                myEntity.setTOTALAMT(Integer.parseInt(columns[columns.length - 1].substring(39, 55)));
                myEntity.setREJECTCOUNT(0);
                myEntity.setREJECTAMT(0);
                myEntity.setACCEPTCOUNT(myEntity.getTOTALCOUNT() - myEntity.getREJECTCOUNT());
                myEntity.setACCEPTAMT(myEntity.getTOTALAMT() - myEntity.getREJECTAMT());
                myEntity.setPROCCOUNT(columns.length - 2);
                // 後續每一筆交易完成要扣除 先用TOTALAMT
                myEntity.setPROCAMT(Integer.parseInt(columns[columns.length - 1].substring(39, 55)));
                // 存放時狀態為2
                myEntity.setSTATUS("2");
                myEntity.setPFILENAME(fileNameArrayList.get(j).substring(indexStart));
                myEntity.setFILELAYOUT("01");
                myEntity.setSENDERBANK(columns[1].substring(14, 21));
                myEntity.setCUT_IN_LINE("N");

                for (int i = 1; i < columns.length - 1; i++) {
                    MyEntity2 myEntity2 = new MyEntity2();
                    // 文件序號從1開始
                    myEntity2.setBIZDATE(columns[0].substring(9, 17));
                    myEntity2.setPROCSEQ(changePerCaseCounter);
                    myEntity2.setBATCHSEQ(fileNameArrayList.get(j).substring(indexStart, indexEnd));
                    myEntity2.setACH_TXTYPE(columns[i].substring(1, 3));
                    myEntity2.setACH_TXID(columns[i].substring(3, 6));
                    myEntity2.setACH_SEQ(columns[i].substring(6, 14));
                    myEntity2.setACH_PBANK(columns[i].substring(14, 21));
                    myEntity2.setACH_PCLNO(columns[i].substring(21, 37));
                    myEntity2.setACH_RBANK(columns[i].substring(37, 44));
                    myEntity2.setACH_RCLNO(columns[i].substring(44, 60));
                    myEntity2.setACH_AMT(columns[i].substring(60, 70));
                    myEntity2.setACH_SCHD(columns[i].substring(72, 73));
                    myEntity2.setACH_CID(columns[i].substring(73, 83));
                    myEntity2.setACH_PID(columns[i].substring(83, 93));
                    myEntity2.setACH_CNO(columns[i].substring(116, 136));
                    myEntity2.setACH_CFEE(columns[i].substring(186, 191));
                    myEntity2.setTXN_AMT(columns[i].substring(60, 70));
                    myEntity2.setSTATUS("1");
                    myEntity2.setACH_TYPE("N");
                    myEntity2.setACH_TXDATE(columns[0].substring(9, 17));
                    repository2.save(myEntity2);
                }
                Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());
                myEntity.setLASTMODIFYDT(endTimestamp);
                repository.save(myEntity);
                log.info("第" + changePerCaseCounter + "個批次檔上傳至FLCOMTROLTAB");
            }
            fileNameArrayList.clear();
            fileContentArrayList.clear();

            myMoveFile.myMove(source, historydata);
            myMoveFile.myMove(historydata, pending);
            log.info("呼叫MyMoveFile檢查重複後移動檔案至pending和historydata");
            // 資料刪除後就沒辦法繼續上傳了因此先註解掉
            //myMoveFile.myDelete();
        }, 0, intervalInSecondsForDao, TimeUnit.SECONDS);
    } //dataToFlcontroltab() end
}
