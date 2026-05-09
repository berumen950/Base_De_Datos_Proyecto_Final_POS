/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Products_In_Transaction {
	
	
	private int transaction_id;
	private int product_id;
	private int quantity;

	public Products_In_Transaction(int transaction_id, int product_id, int quantity) {
		this.transaction_id = transaction_id;
		this.product_id = product_id;
		this.quantity = quantity;
	}

	public int getTransaction_id() {
		return transaction_id;
	}

	public int getProduct_id() {
		return product_id;
	}

	public int getQuantity() {
		return quantity;
	}
	
	
	
	
	
	
	
}
