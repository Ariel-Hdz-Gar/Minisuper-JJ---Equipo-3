import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.Scanner;

public class ISWConexion
{
    private static final String URL      = "jdbc:mariadb://127.0.0.1:3306/isw_db";
    private static final String DB_USER  = "app_readonly";
    private static final String DB_PASS  = "Password";

    private static Connection conectarDB()
    {
        try
        {
            return DriverManager.getConnection(URL, DB_USER, DB_PASS);
        }
        catch (SQLException e)
        {
            System.out.println("Error al conectar con la BD: " + e.getMessage());
            return null;
        }
    }

    public static String validarLogin(String Nombre_Completo, String Contraseña)
    {
        String sql = "SELECT r.Nombre_rol FROM usuarios u " +
                "JOIN roles r ON u.ID_Roles = r.Id_roles " +
                "WHERE u.Nombre_Completo = ? AND u.Contraseña = ?";
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, Nombre_Completo);
            ps.setString(2, Contraseña);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getString("Nombre_rol");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error en la consulta: " + e.getMessage());
        }
        return null;
    }

    public static Connection conectar()
    {
        Scanner sc = new Scanner(System.in);
        System.out.print("Usuario: ");
        String usuario = sc.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = sc.nextLine();
        String rol = validarLogin(usuario, contrasena);
        if (rol != null) {
            System.out.println("Bienvenido " + usuario + " Rol:" + rol);
            return conectarDB();
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
            return null;
        }
    }

    public static void main(String[] args)
    {
        Connection con = conectar();
    }
}