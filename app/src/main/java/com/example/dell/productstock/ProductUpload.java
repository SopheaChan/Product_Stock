package com.example.dell.productstock;

import android.net.Uri;

public class ProductUpload {
    private String pImageUrl;
    private String pName;
    private String pQuantity;
    private String pImportDate;
    private String fileName;
    private String pID;

    public ProductUpload(){}
    public ProductUpload(String pImageUrl, String pName, String pQuantity, String pImportDate, String fileName, String productID) {
        this.pImageUrl = pImageUrl;
        this.pName = pName;
        this.pQuantity = pQuantity;
        this.pImportDate = pImportDate;
        this.fileName = fileName;
        this.pID = productID;
    }
    public void setpImageUrl(String pImageUrl) {
        this.pImageUrl = pImageUrl;
    }
    public void setpName(String pName) {
        this.pName = pName;
    }
    public void setpQuantity(String pQuantity) {
        this.pQuantity = pQuantity;
    }
    public void setpImportDate(String pImportDate) {
        this.pImportDate = pImportDate;
    }
    public void setFileName(String fileName) {this.fileName = fileName;}
    public void setpID(String productID) {this.pID = productID;}
    public String getpImageUrl() {
        return pImageUrl;
    }
    public String getpName() {
        return pName;
    }
    public String getpQuantity() {
        return pQuantity;
    }
    public String getpImportDate() {
        return pImportDate;
    }
    public String getFileName() {return fileName;}
    public String getpID() {return pID;}
}
