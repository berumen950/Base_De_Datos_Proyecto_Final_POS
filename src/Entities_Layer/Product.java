/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Product {
	
	private int product_id;
	private String name;
	private String description;
	private String product_code;
	private int stock;
	private double wholesale_price;
	private double retail_price;

	public Product(int product_id, String name, String description, String product_code, int stock, double wholesale_price, double retail_price) {
		this.product_id = product_id;
		this.name = name;
		this.description = description;
		this.product_code = product_code;
		this.stock = stock;
		this.wholesale_price = wholesale_price;
		this.retail_price = retail_price;
	}

	public int getProduct_id() {
		return product_id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getProduct_code() {
		return product_code;
	}

	public int getStock() {
		return stock;
	}

	public double getWholesale_price() {
		return wholesale_price;
	}

	public double getRetail_price() {
		return retail_price;
	}
	
	
	
	
	
	
	
	
	
}
