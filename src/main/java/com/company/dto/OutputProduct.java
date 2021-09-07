package com.company.dto;

public class OutputProduct {

	private String sku;
	private String description;
	private String source;

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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "OutputProduct [sku=" + sku + ", description=" + description + ", source=" + source + "]";
	}


}
