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

    // Método para registrar un nuevo empleado
    public static boolean registrarEmpleado(String  Nombre_Completo, String Contraseña)
    {
        String sql = "INSERT INTO usuarios (ID_Roles, Nombre_Completo, Contraseña) VALUES (2, ?, ?)";
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1,  Nombre_Completo);
            ps.setString(2, Contraseña);
            int resultado = ps.executeUpdate();
            return resultado > 0;
        }
        catch (SQLException e)
        {
            System.out.println("Error al registrar empleado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método para obtener todos los proveedores
    public static String obtenerProveedores()
    {
        String sql = "SELECT Id_proveedores, Nombre_proveedor FROM proveedores ORDER BY Nombre_proveedor";
        StringBuilder json = new StringBuilder("[");
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{\"Id_Proveedores\":\"");
                json.append(rs.getString("Id_proveedores"));
                json.append("\",\"Nombre_Proveedor\":\"");
                json.append(rs.getString("Nombre_proveedor"));
                json.append("\"}");
                first = false;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error al obtener proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        json.append("]");
        return json.toString();
    }
    
    // Método para obtener todos los empleados
    public static String obtenerEmpleados()
    {
        String sql = "SELECT u.Nombre_Completo, u.Contraseña as pass, r.Nombre_Rol FROM usuarios u JOIN roles r ON u.ID_Roles = r.Id_roles";
        StringBuilder json = new StringBuilder("[");
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{\"nombre\":\"");
                json.append(rs.getString("Nombre_Completo"));
                json.append("\",\"password\":\"");
                json.append(rs.getString("pass"));
                json.append("\",\"cargo\":\"");
                json.append(rs.getString("Nombre_Rol"));
                json.append("\"}");
                first = false;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error al obtener empleados: " + e.getMessage());
            e.printStackTrace();
        }
        json.append("]");
        return json.toString();
    }

    // Método para obtener productos con stock bajo
    public static String obtenerProductosBajoStock()
    {
        String sql = "SELECT Nombre_Producto, Stock, Stock_Minimo FROM productos WHERE Stock <= Stock_Minimo ORDER BY Nombre_Producto";
        StringBuilder json = new StringBuilder("[");
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{\"Nombre_Producto\":\"");
                json.append(rs.getString("Nombre_Producto"));
                json.append("\",\"Stock\":");
                json.append(rs.getInt("Stock"));
                json.append(",\"Stock_Minimo\":");
                json.append(rs.getInt("Stock_Minimo"));
                json.append("}");
                first = false;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error al obtener productos bajo stock: " + e.getMessage());
            e.printStackTrace();
        }
        json.append("]");
        return json.toString();
    }

    // Método para registrar un nuevo producto
    public static String registrarProducto(String Id_Productos, String Nombre_Producto, 
        double Precio_Compra, double Precio_Venta, int Stock, int Stock_Minimo, int ID_Proveedores)
    {
        // Primero verificar si el producto ya existe
        String checkSql = "SELECT Id_Productos FROM productos WHERE Id_Productos = ?";
        try (Connection con = conectarDB();
             PreparedStatement psCheck = con.prepareStatement(checkSql))
        {
            psCheck.setString(1, Id_Productos);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                return "duplicate"; // El producto ya existe
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error al verificar producto: " + e.getMessage());
            return "error";
        }
        
        // Si no existe, insertar el nuevo producto
        String sql = "INSERT INTO productos (Id_Productos, Nombre_Producto, Precio_Compra, Precio_Venta, Stock, Stock_Minimo, ID_Proveedores) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, Id_Productos);
            ps.setString(2, Nombre_Producto);
            ps.setDouble(3, Precio_Compra);
            ps.setDouble(4, Precio_Venta);
            ps.setInt(5, Stock);
            ps.setInt(6, Stock_Minimo);
            ps.setInt(7, ID_Proveedores);
            int resultado = ps.executeUpdate();
            if (resultado > 0) {
                return "success";
            }
            return "error";
        }
        catch (SQLException e)
        {
            System.out.println("Error al registrar producto: " + e.getMessage());
            e.printStackTrace();
            return "error";
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
            server.createContext("/api/registrarEmpleado", new RegistrarEmpleadoHandler());
            server.createContext("/api/obtenerEmpleados", new ObtenerEmpleadosHandler());
            server.createContext("/api/registrarProducto", new RegistrarProductoHandler());
            server.createContext("/api/obtenerProveedores", new ObtenerProveedoresHandler());
            server.createContext("/api/obtenerProductosBajoStock", new ObtenerProductosBajoStockHandler());
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

    static class RegistrarEmpleadoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                
                Map<String, String> params = parseParams(body.toString());
                String Nombre_Completo = params.get("nombre");
                String Contraseña = params.get("password");
                
                System.out.println("Registro de empleado - Nombre: " + Nombre_Completo
                + ", Password: [oculto]");
                
                boolean success = registrarEmpleado(Nombre_Completo, Contraseña);

                String response;
                int statusCode;
                if (success) {
                    response = "{\"success\": true, \"mensaje\": \"Empleado registrado correctamente\"}";
                    statusCode = 200;
                } else {
                    response = "{\"success\": false, \"mensaje\": \"Error al registrar empleado\"}";
                    statusCode = 500;
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

    static class ObtenerEmpleadosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Solicitando lista de empleados");
                
                // Obtener empleados de la BD
                String empleadosJson = obtenerEmpleados();
                
                // Enviar respuesta
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, empleadosJson.length());
                OutputStream os = exchange.getResponseBody();
                os.write(empleadosJson.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        }
    }

    static class RegistrarProductoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                
                Map<String, String> params = parseParams(body.toString());
                System.out.println("Datos recibidos: " + body.toString());
                System.out.println("Params parseados: " + params);
                
                String Id_Productos = params.get("Id_Productos");
                String Nombre_Producto = params.get("Nombre_Producto");
                int ID_Proveedores = 0;
                double Precio_Compra = 0;
                double Precio_Venta = 0;
                int Stock = 0;
                int Stock_Minimo = 0;
                
                try {
                    String idProveedorStr = params.get("ID_Proveedores");
                    if (idProveedorStr != null && !idProveedorStr.isEmpty() && !idProveedorStr.equals("undefined")) {
                        ID_Proveedores = Integer.parseInt(idProveedorStr);
                    }
                    if (params.get("Precio_Compra") != null && !params.get("Precio_Compra").isEmpty()) {
                        Precio_Compra = Double.parseDouble(params.get("Precio_Compra"));
                    }
                    if (params.get("Precio_Venta") != null && !params.get("Precio_Venta").isEmpty()) {
                        Precio_Venta = Double.parseDouble(params.get("Precio_Venta"));
                    }
                    if (params.get("Stock") != null && !params.get("Stock").isEmpty()) {
                        Stock = Integer.parseInt(params.get("Stock"));
                    }
                    if (params.get("Stock_Minimo") != null && !params.get("Stock_Minimo").isEmpty()) {
                        Stock_Minimo = Integer.parseInt(params.get("Stock_Minimo"));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error al parsear números: " + e.getMessage());
                }
                
                System.out.println("Registro de producto - Id: " + Id_Productos + ", Nombre: " + Nombre_Producto + ", Proveedor: " + ID_Proveedores);
                
                String result = registrarProducto(Id_Productos, Nombre_Producto, Precio_Compra, Precio_Venta, Stock, Stock_Minimo, ID_Proveedores);

                String response;
                int statusCode;
                if (result.equals("success")) {
                    response = "{\"success\": true, \"mensaje\": \"Producto registrado correctamente\"}";
                    statusCode = 200;
                } else if (result.equals("duplicate")) {
                    response = "{\"success\": false, \"mensaje\": \"Este producto ya existe\"}";
                    statusCode = 400;
                } else {
                    response = "{\"success\": false, \"mensaje\": \"Error al registrar producto\"}";
                    statusCode = 500;
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

    static class ObtenerProveedoresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Solicitando lista de proveedores");
                
                // Obtener proveedores de la BD
                String proveedoresJson = obtenerProveedores();
                
                // Enviar respuesta
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, proveedoresJson.length());
                OutputStream os = exchange.getResponseBody();
                os.write(proveedoresJson.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        }
    }

    static class ObtenerProductosBajoStockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Solicitando productos bajo stock");
                
                // Obtener productos bajo stock de la BD
                String productosJson = obtenerProductosBajoStock();
                
                // Enviar respuesta
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, productosJson.length());
                OutputStream os = exchange.getResponseBody();
                os.write(productosJson.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
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