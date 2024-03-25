import java.sql.*;

public class Main {
    public static void main (String[] args)
    {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/school_manager","root","password");
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
