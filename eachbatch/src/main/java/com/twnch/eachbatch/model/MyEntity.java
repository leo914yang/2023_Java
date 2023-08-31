package com.twnch.eachbatch.model;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "FLCONTROLTAB", schema = "EACHUSER")
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int ID;
    private int PROCSEQ;
    private String BIZDATE;
    private String BATCHSEQ;
    private Timestamp START_TS;
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
