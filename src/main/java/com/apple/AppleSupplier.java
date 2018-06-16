package com.apple;

public class AppleSupplier {
	
	private String supplierName;
	private String supplierAddress;
	
	public AppleSupplier() {
		supplierName = "";
		supplierAddress = "";
	}
	
	public AppleSupplier(String supplierName, String supplierAddres) {
		super();
		this.supplierName = supplierName;
		this.supplierAddress = supplierAddres;
	}
	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public String getSupplierAddress() {
		return supplierAddress;
	}
	public void setSupplierAddress(String supplierAddress) {
		this.supplierAddress = supplierAddress;
	}

	@Override
	public String toString() {
		return "AppleSupplier [supplierName=" + supplierName
				+ ", supplierAddres=" + supplierAddress + "]";
	}
	
	

}
