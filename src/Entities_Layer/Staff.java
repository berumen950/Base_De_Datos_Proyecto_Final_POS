/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Staff {
	
	private int staff_id;
	private String first_name;
	private String last_name;
	private String role;
	private int age;
	private String address;
	private String phone;
	private String email;

	public Staff(int staff_id, String first_name, String last_name, String role, int age, String address, String phone, String email) {
		this.staff_id = staff_id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.role = role;
		this.age = age;
		this.address = address;
		this.phone = phone;
		this.email = email;
	}

	public int getStaff_id() {
		return staff_id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getRole() {
		return role;
	}

	public int getAge() {
		return age;
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
