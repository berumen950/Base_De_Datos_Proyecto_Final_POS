/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Products_In_Transaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLProducts_In_Transaction {
	public static ArrayList<Products_In_Transaction> getProducts_In_Transaction(){
	
		ArrayList<Products_In_Transaction> products_In_Transactions_List = new ArrayList<>();
		String sql = "Select * From products_in_transaction";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Products_In_Transaction p = new Products_In_Transaction(
				rs.getInt("transaction_id"),
				rs.getInt("product_id"),
				rs.getInt("quantity")
			);
			
			products_In_Transactions_List.add(p);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return products_In_Transactions_List;
	
	
	}
}
