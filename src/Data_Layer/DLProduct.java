/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Payment;
import Entities_Layer.Product;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLProduct {
	public static ArrayList<Product> getProducts(){
	
		ArrayList<Product> ProductList = new ArrayList<>();
		String sql = "Select * From products";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Product product = new Product(
				rs.getInt("product_id"),
				rs.getString("name"),
				rs.getString("description"),
				rs.getString("product_code"),
				rs.getInt("stock"),
				rs.getDouble("wholesale_price"),
				rs.getDouble("retail_price")
			);
			
			ProductList.add(product);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return ProductList;
	
	
	}
	
	
	
}
