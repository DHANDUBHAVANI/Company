package com.company.dto;

public class BarCodesB {
	
	private String supplierID;
	private String sku;
	private String barcode;
	
	public String getSupplierID() {
		return supplierID;
	}
	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	@Override
	public String toString() {
		return "BarCodesB [supplierID=" + supplierID + ", sku=" + sku + ", barcode=" + barcode + "]";
	}
}
