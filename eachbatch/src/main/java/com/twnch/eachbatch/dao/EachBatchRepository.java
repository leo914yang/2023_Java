package com.twnch.eachbatch.dao;

import com.twnch.eachbatch.model.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EachBatchRepository extends JpaRepository<MyEntity, Integer> {
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '2' AND f.BIZDATE = :bizdate ORDER BY f.PROCSEQ")
    List<MyEntity> findS2ByDate(@Param("bizdate") String bizdate);
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '2' AND f.BIZDATE = :bizdate AND f.CUT_IN_LINE = 'Y' ORDER BY f.PROCSEQ")
    List<MyEntity> findCutInLine(@Param("bizdate") String bizdate);
    @Query("SELECT f FROM MyEntity f WHERE f.STATUS = '4' AND f.BIZDATE = :bizdate ORDER BY f.PROCSEQ")
    List<MyEntity> findComplete(@Param("bizdate") String bizdate);

    @Query("select f , (select MIN(f2) from MyEntity2 f2 where f2.PROCSEQ = f.PROCSEQ) from MyEntity f ORDER BY f.PROCSEQ")
    List<Object[]> findTest();
    @Query(value =
            "SELECT e1.PROCSEQ as p1,\n" +
                    "       e2.*\n" +
                    "FROM EACHUSER.FLCONTROLTAB as e1\n" +
                    "INNER JOIN EACHUSER.FLACHPTAB as e2 ON e1.PROCSEQ = e2.PROCSEQ\n" +
                    "WHERE e1.CUT_IN_LINE = 'Y' AND e1.STATUS = 2\n" +
                    "ORDER BY e1.PROCSEQ;"
            , nativeQuery = true)
    List<Object[]> findPairingCut();

    @Query(value =
            "SELECT e1.PROCSEQ as p1,\n" +
                    "       e2.*\n" +
                    "FROM EACHUSER.FLCONTROLTAB as e1\n" +
                    "INNER JOIN EACHUSER.FLACHPTAB as e2 ON e1.PROCSEQ = e2.PROCSEQ\n" +
                    "WHERE e1.CUT_IN_LINE != 'Y' AND e1.STATUS = 2 AND e1.PROCSEQ = (SELECT MIN(PROCSEQ) FROM EACHUSER.FLCONTROLTAB WHERE STATUS = 2)\n" +
                    "ORDER BY e1.PROCSEQ;\n"
            , nativeQuery = true)
    List<Object[]> findPairingNotCut();
}
