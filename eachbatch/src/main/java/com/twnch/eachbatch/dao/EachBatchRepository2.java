package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EachBatchRepository2 extends JpaRepository<MyEntity2, Integer> {
    @Query("SELECT f FROM MyEntity2 f WHERE f.STATUS = '1' AND f.BIZDATE = :bizdate AND f.PROCSEQ = :procseq ORDER BY f.PROCSEQ")
    List<MyEntity2> getSelectedData(@Param("bizdate") String bizdate, @Param("procseq") Integer procseq);
    @Query("SELECT f FROM MyEntity2 f WHERE f.STATUS = '1' AND f.BIZDATE = :bizdate ORDER BY f.PROCSEQ")
    List<MyEntity2> findAllDataByDate(@Param("bizdate") String bizdate);

}

