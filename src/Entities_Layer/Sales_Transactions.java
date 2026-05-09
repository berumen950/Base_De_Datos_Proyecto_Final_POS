/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Sales_Transactions {
	
	private int transaction_id;
	private String transaction_datetime;
	private double wholesale_price;
	private double retail_price;
	private int customer_id;
	private int staff_id;
	private int sales_outlet_id;

	public Sales_Transactions(int transaction_id, String transaction_datetime, double wholesale_price, double retail_price, int customer_id, int staff_id, int sales_outlet_id) {
		this.transaction_id = transaction_id;
		this.transaction_datetime = transaction_datetime;
		this.wholesale_price = wholesale_price;
		this.retail_price = retail_price;
		this.customer_id = customer_id;
		this.staff_id = staff_id;
		this.sales_outlet_id = sales_outlet_id;
	}

	public int getTransaction_id() {
		return transaction_id;
	}

	public String getTransaction_datetime() {
		return transaction_datetime;
	}

	public double getWholesale_price() {
		return wholesale_price;
	}

	public double getRetail_price() {
		return retail_price;
	}

	public int getCustomer_id() {
		return customer_id;
	}

	public int getStaff_id() {
		return staff_id;
	}

	public int getSales_outlet_id() {
		return sales_outlet_id;
	}
	
	
	
	
	
}
