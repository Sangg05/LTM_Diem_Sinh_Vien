/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ltm_dt15;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class DBAccess {

    private Connection con;
    private Statement stmt;
    SQLServer_Connection myCon;

    public Connection getCon() {
        return con;
    }

    public DBAccess() {
        myCon = new SQLServer_Connection();
    }

    public Connection connect(String Url) throws SQLException {
        con = myCon.getConnection(Url);
        if (con != null) {
            stmt = con.createStatement();
        }
        
        return con;
    }

    public int Update(String str) {
        try {
            System.out.println(str);
            int i = stmt.executeUpdate(str);
            return i;
        } catch (Exception e) {
            return -1;
        }
    }

    public ResultSet Query(String str) {
        try {
            ResultSet rs = stmt.executeQuery(str);
            return rs;
        } catch (Exception e) {
            System.out.println("Error query!" + e);
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            new DBAccess().connect("jdbc:sqlserver://localhost:1433;Database=LapTrinhMang;user=sa;password=123456");
        } catch (SQLException ex) {
            Logger.getLogger(DBAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
