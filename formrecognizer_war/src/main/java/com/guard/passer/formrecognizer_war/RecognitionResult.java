package com.guard.passer.formrecognizer_war;

import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "recognitionResults")
public class RecognitionResult {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "receiptFileName")
    private String receiptFileName;

    @Column(name = "merchantName")
    private String merchantName;

    @Column(name = "transactionDate")
    private LocalDate transactionDate;

    @Column(name = "transactionTime")
    private LocalTime transactionTime;

    @Column(name = "total")
    private double total;

    @Column(name = "fn")
    private Long fn;

    @Column(name = "fd")
    private Long fd;

    @Column(name = "fp")
    private Long fp;


    public Long getId() {
        return id;
    }

    public String getReceiptFileName() {
        return receiptFileName;
    }

    public void setReceiptFileName(String receiptFileName) {
        this.receiptFileName = receiptFileName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Long getFn() {
        return fn;
    }

    public Long getFd() {
        return fd;
    }

    public Long getFp() {
        return fp;
    }

    public String toString() {
        return String.format("Merchant name: %s\nTransaction date: %s\nTotal:%.2f",
                getMerchantName(), getTransactionDate(), getTotal());
    }
}
