package com.twnch.eachbatch.model;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "FLCONTROLTAB", schema = "EACHUSER")
@IdClass(CompositeKey.class)
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int ID;
    @Id
    private int PROCSEQ;
    private String BIZDATE;
    private String BATCHSEQ;

    private Timestamp START_TS;
    // private Timestamp END_TS;
    private int TOTALCOUNT;
    private int TOTALAMT;
    private int REJECTCOUNT;
    private int REJECTAMT;
    private int ACCEPTCOUNT;
    private int ACCEPTAMT;
    private int PROCCOUNT;
    private int PROCAMT;
    private Timestamp LASTMODIFYDT;
    private String STATUS;
    private String PFILENAME;
    private String FILELAYOUT;
    private String SENDERBANK;
    private String CUT_IN_LINE;

}
