package com.twnch.eachbatch.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "FLACHPTAB", schema = "EACHUSER")
@IdClass(CompositeKey.class)
public class MyEntity2 {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int ID;
    private String BIZDATE;
    @Id
    private int PROCSEQ;
    private String BATCHSEQ;
    private String ACH_TXDATE;
    private String ACH_TYPE;
    private String ACH_TXTYPE;
    private String ACH_TXID;
    private String ACH_SEQ;
    private String ACH_PBANK;
    private String ACH_PCLNO;
    private String ACH_RBANK;
    private String ACH_RCLNO;
    private String ACH_AMT;
    private String ACH_SCHD;
    private String ACH_CID;
    private String ACH_PID;
    private String ACH_CNO;
    private String ACH_CFEE;
    private String TXN_AMT;
    private String STATUS;
}
