/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Customer {
	
	private int customer_id;
	private String first_name;
	private String last_name;
	private String address;
	private String phone;
	private String email;

	public Customer(int customer_id, String first_name, String last_name, String address, String phone, String email) {
		this.customer_id = customer_id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.address = address;
		this.phone = phone;
		this.email = email;
	}

	public int getCustomer_id() {
		return customer_id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}
	
	
	
	
	
	
	
	
}
