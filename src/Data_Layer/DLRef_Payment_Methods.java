/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Ref_Payment_Methods;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLRef_Payment_Methods {
	public static ArrayList<Ref_Payment_Methods> getRef_Payment_Methods(){
	
		ArrayList<Ref_Payment_Methods> ref_Payment_MethodsList = new ArrayList<>();
		String sql = "Select * From ref_payment_methods";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Ref_Payment_Methods ref_Payment_Methods = new Ref_Payment_Methods(
				rs.getString("payment_method_code"),
				rs.getString("payment_method_name"),
				rs.getString("payment_method_description")
			);
			
			ref_Payment_MethodsList.add(ref_Payment_Methods);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return ref_Payment_MethodsList;
	
	
	}
}
