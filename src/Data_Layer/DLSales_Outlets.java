/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Sales_Outlets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLSales_Outlets {
	public static ArrayList<Sales_Outlets> getSales_Outlets(){
	
		ArrayList<Sales_Outlets> Sales_Outlets_List = new ArrayList<>();
		String sql = "Select * From sales_outlets";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Sales_Outlets sales_outlets = new Sales_Outlets(
				rs.getInt("sales_outlet_id"),
				rs.getString("name"),
				rs.getString("address"),
				rs.getString("phone")
			);
			
			Sales_Outlets_List.add(sales_outlets);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return Sales_Outlets_List;
	
	
	}
}
