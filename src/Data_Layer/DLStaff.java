/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import Entities_Layer.Customer;
import Entities_Layer.Staff;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author acer
 */
public class DLStaff {
	public static ArrayList<Staff> getStaff(){
	
		ArrayList<Staff> Staff_List = new ArrayList<>();
		String sql = "Select * From staff";
		
		
	
	try(
		Connection con = Conexion.conectar_bsd();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
	){
		
		while(rs.next()){
			Staff staff = new Staff(
				rs.getInt("staff_id"),
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getString("role"),
				rs.getInt("age"),
				rs.getString("address"),
				rs.getString("phone"),
				rs.getString("email")
			);
			
			Staff_List.add(staff);
		}
			
		
		
		
	}catch(Exception e){
		System.out.println("Error");
		e.printStackTrace();
	}
	
	return Staff_List;
	
	
	}
}
