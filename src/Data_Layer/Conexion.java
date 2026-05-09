/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Layer;

import java.sql.*;


/**
 *
 * @author acer
 */
public class Conexion {
	private static final String URL = "jdbc:postgresql://localhost:5432/Pos_db";
	private static final String USER = "postgres";
	private static final String PASSWORD = "pablo123";
	
	public static Connection conectar_bsd(){
		
		Connection con = null;
		try{
			con = DriverManager.getConnection(
				URL,
				USER,
				PASSWORD
			);
			
			System.out.println("Exito");
			
			
		}catch(Exception e){
			System.out.println("Error de conexion");
			e.printStackTrace();
		}
		
		
		
		return con;
		
	}
	
	
	
	
	
	
	
	
}
