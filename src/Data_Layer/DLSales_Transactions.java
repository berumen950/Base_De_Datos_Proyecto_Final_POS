/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Sales_Transactions;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLSales_Transactions {
	
	public static ArrayList<Sales_Transactions> getSales_Transactions(){
	
		ArrayList<Sales_Transactions> Sales_Transactions_List = new ArrayList<>();
		String sql = "Select * From sales_transactions";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Sales_Transactions sales_Transactions = new Sales_Transactions(
				rs.getInt("transaction_id"),
				rs.getString("transaction_datetime"),
				rs.getDouble("wholesale_price"),
				rs.getDouble("retail_price"),
				rs.getInt("customer_id"),
				rs.getInt("staff_id"),
				rs.getInt("sales_outlet_id")
			);
			
			Sales_Transactions_List.add(sales_Transactions);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return Sales_Transactions_List;
	
	
	}
	
	
	
	
}
