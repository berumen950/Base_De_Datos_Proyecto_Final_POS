/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Payment;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLPayment {
	
	public static ArrayList<Payment> getPayments(){
	
		ArrayList<Payment> PaymentList = new ArrayList<>();
		String sql = "Select * From payments";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Payment payment = new Payment(
				rs.getInt("payment_id"),
				rs.getDouble("amount"),
				rs.getString("payment_date"),
				rs.getString("payment_reference"),
				rs.getInt("transaction_id"),
				rs.getString("payment_method_code")
			);
			
			PaymentList.add(payment);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return PaymentList;
	
	
	}
	
	
	
	
	
}
