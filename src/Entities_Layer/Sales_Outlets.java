/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Sales_Outlets {
	
	
	private int sales_outlet_id;
	private String name;
	private String address;
	private String phone;

	public Sales_Outlets(int sales_outlet_id, String name, String address, String phone) {
		this.sales_outlet_id = sales_outlet_id;
		this.name = name;
		this.address = address;
		this.phone = phone;
	}

	public int getSales_outlet_id() {
		return sales_outlet_id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}
	
	
	
	
	
	
	
	
}
