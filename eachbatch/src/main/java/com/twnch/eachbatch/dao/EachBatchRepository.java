package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity;
import com.twnch.eachbatch.model.MyEntity2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EachBatchRepository extends JpaRepository<MyEntity, Integer> {
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '2' AND f.BIZDATE = :bizdate ORDER BY f.PROCSEQ")
    List<MyEntity> findS2ByDate(@Param("bizdate") String bizdate);
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '2' AND f.BIZDATE = :bizdate AND f.CUT_IN_LINE = 'Y' ORDER BY f.PROCSEQ")
    List<MyEntity> findCutInLine(@Param("bizdate") String bizdate);
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '4' AND f.BIZDATE = :bizdate ORDER BY f.PROCSEQ")
    List<MyEntity> findComplete(@Param("bizdate") String bizdate);
//    @Query("SELECT f, f2, f.PROCSEQ FROM MyEntity f LEFT JOIN f.myEntity2 f2")
//    Map<MyEntity, MyEntity2> leftJoinTest();
}
