import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.Scanner;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

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
        String sql = "SELECT r.Id_roles FROM usuarios u " +
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
                return rs.getString("Id_roles");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error en la consulta: " + e.getMessage());
            e.printStackTrace();
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
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/login", new LoginHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Servidor iniciado en http://localhost:8080");
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Leer el body de la petición
                InputStream is = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                
                // Parsear los parámetros (usuario=X&contrasena=Y)
                Map<String, String> params = parseParams(body.toString());
                String nombreCompleto = params.get("usuario");
                String contrasena = params.get("contrasena");
                
                System.out.println("Intento de login - Usuario: " + nombreCompleto);
                
                // Validar contra la BD
                String rol = validarLogin(nombreCompleto, contrasena);
                
                // Preparar respuesta JSON
                String response;
                int statusCode;
                if (rol != null) {
                    response = "{\"success\": true, \"rol\": \"" + rol + "\", \"mensaje\": \"Bienvenido\"}";
                    statusCode = 200;
                } else {
                    response = "{\"success\": false, \"mensaje\": \"Usuario o contraseña incorrectos\"}";
                    statusCode = 401;
                }
                
                // Enviar respuesta
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(statusCode, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        }
    }

    static Map<String, String> parseParams(String body) {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}