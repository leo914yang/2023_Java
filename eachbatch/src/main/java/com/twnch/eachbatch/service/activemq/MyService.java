package com.twnch.eachbatch.service.activemq;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import com.twnch.eachbatch.dao.EachBatchRepository;
import com.twnch.eachbatch.dao.EachBatchRepository2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MyService {

    @Autowired
    private EachBatchRepository repository;

    @Autowired
    private EachBatchRepository2 repository2;

    public List<MyEntity> getS2ByDate(String bizdate) {
        return repository.findS2ByDate(bizdate);
    }
    public List<MyEntity> getCutByTable1(String bizdate) {
        return repository.findCutInLine(bizdate);
    }
    public List<MyEntity> getComplete(String bizdate) {
        return repository.findComplete(bizdate);
    }
    public List<MyEntity2> getSelected(String myBizdate, int procSeqValue){
        return repository2.getSelectedData(myBizdate, procSeqValue);
    }
    public List<MyEntity2> getAllDataByDate(String bizdate){ return  repository2.findAllDataByDate(bizdate);}
    public void update(MyEntity entity) {
        repository.save(entity);
    }
    public void update(MyEntity2 entity) {
        repository2.save(entity);
    }
    public void update(List<MyEntity2> myEntity2List) {
        repository2.saveAll(myEntity2List);
    }

    public List<Object[]> getTest(){return repository.findTest(); }
    public List<Object[]> getPairingCut(){return repository.findPairingCut(); }
    public List<Object[]> getPairingNotCut(){return repository.findPairingNotCut(); }

}