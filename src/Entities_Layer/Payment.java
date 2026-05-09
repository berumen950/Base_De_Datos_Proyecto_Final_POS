/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Payment {
	
	private int payment_id;
	private double amount;
	private String payment_date;
	private String payment_reference;
	
	private int transaction_id;
	private String payment_method_code;

	public Payment(int payment_id, double amount, String payment_date, String payment_reference, int transaction_id, String payment_method_code) {
		this.payment_id = payment_id;
		this.amount = amount;
		this.payment_date = payment_date;
		this.payment_reference = payment_reference;
		this.transaction_id = transaction_id;
		this.payment_method_code = payment_method_code;
	}

	public int getPayment_id() {
		return payment_id;
	}

	public double getAmount() {
		return amount;
	}

	public String getPayment_date() {
		return payment_date;
	}

	public String getPayment_reference() {
		return payment_reference;
	}

	public int getTransaction_id() {
		return transaction_id;
	}

	public String getPayment_method_code() {
		return payment_method_code;
	}
	
	
	
	
	
}
