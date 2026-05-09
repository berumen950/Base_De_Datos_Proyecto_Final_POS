/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities_Layer;

/**
 *
 * @author acer
 */
public class Ref_Payment_Methods {
	
	private String payment_method_code;
	private String payment_method_name;
	private String payment_method_description;

	public Ref_Payment_Methods(String payment_method_code, String payment_method_name, String payment_method_description) {
		this.payment_method_code = payment_method_code;
		this.payment_method_name = payment_method_name;
		this.payment_method_description = payment_method_description;
	}

	public String getPayment_method_code() {
		return payment_method_code;
	}

	public String getPayment_method_name() {
		return payment_method_name;
	}

	public String getPayment_method_description() {
		return payment_method_description;
	}
	
	
	
	
	
	
	
	
}
