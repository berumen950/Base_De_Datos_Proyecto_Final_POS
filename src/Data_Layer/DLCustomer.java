/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLCustomer {
	
	public static ArrayList<Customer> getCustomers(){
	
		ArrayList<Customer> CustomerList = new ArrayList<>();
		String sql = "Select * From customers";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Customer customer = new Customer(
				rs.getInt("customer_id"),
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getString("address"),
				rs.getString("phone"),
				rs.getString("email")
			);
			
			CustomerList.add(customer);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return CustomerList;
	
	
	}
}
