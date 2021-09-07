package com.company.dto;

public class CatalogA {
	
	private String sku;
	private String description;
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "CatalogA [sku=" + sku + ", description=" + description + "]";
	}
	
}
