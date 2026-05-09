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

    // Método para obtener todos los productos
    public static String obtenerProductos()
    {
        String sql = "SELECT Id_Productos, Nombre_Producto, Precio_Venta, Stock FROM productos ORDER BY Nombre_Producto";
        StringBuilder json = new StringBuilder("[");
        try (Connection con = conectarDB();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{\"Id_Productos\":\"");
                json.append(rs.getString("Id_Productos"));
                json.append("\",\"Nombre_Producto\":\"");
                json.append(rs.getString("Nombre_Producto"));
                json.append("\",\"Precio_Venta\":");
                json.append(rs.getDouble("Precio_Venta"));
                json.append(",\"Stock\":");
                json.append(rs.getInt("Stock"));
                json.append("}");
                first = false;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error al obtener productos: " + e.getMessage());
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

    public static String registrarVenta(int ID_Clientes, String Metodo_pago,
    double Subtotal, double Total, double Monto_Recibido, double Cambio,
    boolean esCredito, String productosJson)
{
    String sqlVenta = "INSERT INTO ventas (ID_Clientes, Fecha_venta, Hora_venta, Metodo_pago, Subtotal, Total, Monto_Recibido, Cambio) " +
                      "VALUES (?, CURDATE(), CURTIME(), ?, ?, ?, ?, ?)";

    String sqlDetalle = "INSERT INTO Detalle_Ventas (Id_Ventas, ID_Productos, Cantidad, Precio_unitario) " +
                        "VALUES (?, ?, ?, ?)";
    
    String sqlAdeudo = "INSERT INTO adeudos (Id_Ven, Id_Cli, Monto_Ad, Fecha_Limite, Dias_Atraso) " +
                   "VALUES (?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 0)";

    Connection con = null;
    try {
        con = conectarDB();
        con.setAutoCommit(false); // Iniciar transacción

        // 1. Insertar la venta principal
        PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
        psVenta.setInt(1, ID_Clientes);
        psVenta.setString(2, Metodo_pago);
        psVenta.setDouble(3, Subtotal);
        psVenta.setDouble(4, Total);
        psVenta.setDouble(5, Monto_Recibido);
        psVenta.setDouble(6, Cambio);
        psVenta.executeUpdate();

        // Obtener el ID de la venta recién insertada
        ResultSet generatedKeys = psVenta.getGeneratedKeys();
        if (!generatedKeys.next()) {
            con.rollback();
            return "error";
        }
        int idVenta = generatedKeys.getInt(1);

        // 2. Parsear productos y insertar detalle
        // Formato esperado: [{"id":"1","cantidad":2,"precio":18.00}, ...]
        String[] items = productosJson.replace("[","").replace("]","").split("\\},\\{");
        PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);

        for (String item : items) {
            item = item.replace("{","").replace("}","");
            String[] fields = item.split(",");
            String idProducto = "";
            int cantidad = 0;
            double precio = 0.0;

            for (String field : fields) {
                String[] kv = field.split(":");
                if (kv.length == 2) {
                    String key = kv[0].replace("\"","").trim();
                    String val = kv[1].replace("\"","").trim();
                    if (key.equals("id"))       idProducto = val;
                    if (key.equals("cantidad")) cantidad   = Integer.parseInt(val);
                    if (key.equals("precio"))   precio     = Double.parseDouble(val);
                }
            }

            psDetalle.setInt(1, idVenta);
            psDetalle.setString(2, idProducto);
            psDetalle.setInt(3, cantidad);
            psDetalle.setDouble(4, precio);
            psDetalle.addBatch();
        }

        psDetalle.executeBatch();

        // 3. Descontar stock de cada producto
        String sqlStock = "UPDATE productos SET Stock = Stock - ? WHERE Id_Productos = ?";
        PreparedStatement psStock = con.prepareStatement(sqlStock);

        for (String item : items) {
            item = item.replace("{","").replace("}","");
            String[] fields = item.split(",");
            String idProducto = "";
            int cantidad = 0;

            for (String field : fields) {
                String[] kv = field.split(":");
                if (kv.length == 2) {
                    String key = kv[0].replace("\"","").trim();
                    String val = kv[1].replace("\"","").trim();
                    if (key.equals("id"))       idProducto = val;
                    if (key.equals("cantidad")) cantidad   = Integer.parseInt(val);
                }
            }

            psStock.setInt(1, cantidad);
            psStock.setString(2, idProducto);
            psStock.addBatch();
        }

        psStock.executeBatch();
        if (esCredito) {
            double montoPendiente = Total - Monto_Recibido;
            if (montoPendiente > 0) {
                PreparedStatement psAdeudo = con.prepareStatement(sqlAdeudo);
                psAdeudo.setInt(1, idVenta);
                psAdeudo.setInt(2, ID_Clientes);
                psAdeudo.setDouble(3, montoPendiente);
                psAdeudo.executeUpdate();
            }
        }
        con.commit(); // Todo bien, confirmar transacción
        return "success:" + idVenta;

    } catch (SQLException e) {
        System.out.println("Error al registrar venta: " + e.getMessage());
        e.printStackTrace();
        try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        return "error";
    } finally {
        try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}

public static String obtenerAdeudos()
{
    String sql = "SELECT a.Id_Adeudo, c.Nombre_cliente, a.Monto_Ad, " +
                 "a.Fecha_Limite, a.Dias_Atraso " +
                 "FROM adeudos a " +
                 "JOIN clientes c ON a.Id_Cli = c.Id_clientes " +
                 "ORDER BY a.Dias_Atraso DESC, a.Fecha_Limite ASC";
    StringBuilder json = new StringBuilder("[");
    try (Connection con = conectarDB();
         PreparedStatement ps = con.prepareStatement(sql))
    {
        ResultSet rs = ps.executeQuery();
        boolean first = true;
        while (rs.next()) {
            if (!first) json.append(",");
            json.append("{\"Id_Adeudo\":");
            json.append(rs.getInt("Id_Adeudo"));
            json.append(",\"Nombre_cliente\":\"");
            json.append(rs.getString("Nombre_cliente"));
            json.append("\",\"Monto_Ad\":");
            json.append(rs.getDouble("Monto_Ad"));
            json.append(",\"Fecha_Limite\":\"");
            json.append(rs.getString("Fecha_Limite"));
            json.append("\",\"Dias_Atraso\":");
            json.append(rs.getInt("Dias_Atraso"));
            json.append("}");
            first = false;
        }
    }
    catch (SQLException e)
    {
        System.out.println("Error al obtener adeudos: " + e.getMessage());
        e.printStackTrace();
    }
    json.append("]");
    return json.toString();
}


public static String obtenerClientes()
{
    String sql = "SELECT Id_clientes, Nombre_cliente, Telefono_cliente FROM clientes ORDER BY Nombre_cliente";
    StringBuilder json = new StringBuilder("[");
    try (Connection con = conectarDB();
         PreparedStatement ps = con.prepareStatement(sql))
    {
        ResultSet rs = ps.executeQuery();
        boolean first = true;
        while (rs.next()) {
            if (!first) json.append(",");
            json.append("{\"Id_clientes\":");
            json.append(rs.getInt("Id_clientes"));
            json.append(",\"Nombre_cliente\":\"");
            json.append(rs.getString("Nombre_cliente"));
            json.append("\",\"Telefono_cliente\":\"");
            json.append(rs.getString("Telefono_cliente"));
            json.append("\"}");
            first = false;
        }
    }
    catch (SQLException e)
    {
        System.out.println("Error al obtener clientes: " + e.getMessage());
        e.printStackTrace();
    }
    json.append("]");
    return json.toString();
}

public static String registrarCliente(String Nombre_cliente, String Telefono_cliente)
{
    String sql = "INSERT INTO clientes (Nombre_cliente, Telefono_cliente) VALUES (?, ?)";
    try (Connection con = conectarDB();
         PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
    {
        ps.setString(1, Nombre_cliente);
        ps.setString(2, Telefono_cliente);
        int resultado = ps.executeUpdate();
        if (resultado > 0) {
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int nuevoId = keys.getInt(1);
                return "success:" + nuevoId;
            }
        }
        return "error";
    }
    catch (SQLException e)
    {
        System.out.println("Error al registrar cliente: " + e.getMessage());
        e.printStackTrace();
        return "error";
    }
}

public static String obtenerVentas()
{
    String sql = "SELECT v.Id_ventas, v.Fecha_venta, c.Nombre_cliente, " +
                 "v.Metodo_pago, v.Total, " +
                 "COALESCE(a.Monto_Ad, 0) as Adeudo " +
                 "FROM ventas v " +
                 "JOIN clientes c ON v.ID_Clientes = c.Id_clientes " +
                 "LEFT JOIN adeudos a ON v.Id_ventas = a.Id_Ven " +
                 "ORDER BY v.Fecha_venta DESC, v.Id_ventas DESC";
    StringBuilder json = new StringBuilder("[");
    try (Connection con = conectarDB();
         PreparedStatement ps = con.prepareStatement(sql))
    {
        ResultSet rs = ps.executeQuery();
        boolean first = true;
        while (rs.next()) {
            if (!first) json.append(",");
            json.append("{\"Id_ventas\":");
            json.append(rs.getInt("Id_ventas"));
            json.append(",\"Fecha_venta\":\"");
            json.append(rs.getString("Fecha_venta"));
            json.append("\",\"Nombre_cliente\":\"");
            json.append(rs.getString("Nombre_cliente"));
            json.append("\",\"Metodo_pago\":\"");
            json.append(rs.getString("Metodo_pago"));
            json.append("\",\"Total\":");
            json.append(rs.getDouble("Total"));
            json.append(",\"Adeudo\":");
            json.append(rs.getDouble("Adeudo"));
            json.append("}");
            first = false;
        }
    }
    catch (SQLException e)
    {
        System.out.println("Error al obtener ventas: " + e.getMessage());
        e.printStackTrace();
    }
    json.append("]");
    return json.toString();
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
            server.createContext("/api/obtenerProductos", new ObtenerProductosHandler());
            server.createContext("/api/registrarVenta", new RegistrarVentaHandler());
            server.createContext("/api/obtenerClientes",  new ObtenerClientesHandler());
            server.createContext("/api/registrarCliente", new RegistrarClienteHandler());
            server.createContext("/api/obtenerAdeudos", new ObtenerAdeudosHandler());
            server.createContext("/api/obtenerVentas", new ObtenerVentasHandler());
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
    static class ObtenerProductosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Solicitando lista de productos");
                
                // Obtener productos de la BD
                String productosJson = obtenerProductos();
                
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
    static class RegistrarVentaHandler implements HttpHandler {
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

            int ID_Clientes = 1;
            if (params.get("id_cliente") != null && !params.get("id_cliente").isEmpty()) 
            {
               ID_Clientes = Integer.parseInt(params.get("id_cliente"));
            }
            String Metodo_pago    = params.get("metodo_pago");
            double Subtotal       = 0, Total = 0, Monto_Recibido = 0;
            String productosJson  = "";
            boolean esCredito = false;
            if ("true".equals(params.get("es_credito"))) esCredito = true;
            double Cambio = 0;
            try {
                if (params.get("subtotal")      != null) Subtotal       = Double.parseDouble(params.get("subtotal"));
                if (params.get("total")         != null) Total          = Double.parseDouble(params.get("total"));
                if (params.get("monto_recibido")!= null) Monto_Recibido = Double.parseDouble(params.get("monto_recibido"));
                if (params.get("cambio")        != null) Cambio         = Double.parseDouble(params.get("cambio"));
                if (params.get("productos")     != null) productosJson  = java.net.URLDecoder.decode(params.get("productos"), "UTF-8");
                if (params.get("id_cliente")    != null && !params.get("id_cliente").isEmpty())
                    ID_Clientes = Integer.parseInt(params.get("id_cliente"));
            } catch (NumberFormatException e) {
                System.out.println("Error al parsear parámetros: " + e.getMessage());
            }

            try {
                if (params.get("subtotal")        != null) Subtotal       = Double.parseDouble(params.get("subtotal"));
                if (params.get("total")            != null) Total          = Double.parseDouble(params.get("total"));
                if (params.get("monto_recibido")   != null) Monto_Recibido = Double.parseDouble(params.get("monto_recibido"));
                if (params.get("cambio")           != null) Cambio         = Double.parseDouble(params.get("cambio"));
                if (params.get("productos")        != null) productosJson  = java.net.URLDecoder.decode(params.get("productos"), "UTF-8");
            } catch (NumberFormatException e) {
                System.out.println("Error al parsear parámetros: " + e.getMessage());
            }

            System.out.println("Registrando venta - Método: " + Metodo_pago + ", Total: " + Total);

            String result = registrarVenta(ID_Clientes, Metodo_pago, Subtotal, Total, 
                               Monto_Recibido, Cambio, esCredito, productosJson);

            String response;
            int statusCode;
            if (result.startsWith("success")) {
                String[] parts    = result.split(":");
                String idVenta    = parts[1];
                String montoPend  = parts.length > 2 ? parts[2] : "0.0";
                response  = "{\"success\": true, \"id_venta\": " + idVenta + 
                ", \"monto_pendiente\": " + montoPend + 
                ", \"mensaje\": \"Venta registrada correctamente\"}";
                statusCode = 200;
            } 
            else {
                response  = "{\"success\": false, \"mensaje\": \"Error al registrar la venta\"}";
                statusCode = 500;
            }

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
static class ObtenerClientesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Solicitando lista de clientes");
            String clientesJson = obtenerClientes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, clientesJson.length());
            OutputStream os = exchange.getResponseBody();
            os.write(clientesJson.getBytes());
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

static class RegistrarClienteHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);

            Map<String, String> params = parseParams(body.toString());
            String Nombre_cliente   = params.get("nombre");
            String Telefono_cliente = params.get("telefono");

            System.out.println("Registrando cliente: " + Nombre_cliente);

            String result = registrarCliente(Nombre_cliente, Telefono_cliente);

            String response;
            int statusCode;
            if (result.startsWith("success")) {
                String idCliente = result.split(":")[1];
                response   = "{\"success\": true, \"id_cliente\": " + idCliente + ", \"mensaje\": \"Cliente registrado correctamente\"}";
                statusCode = 200;
            } else {
                response   = "{\"success\": false, \"mensaje\": \"Error al registrar cliente\"}";
                statusCode = 500;
            }

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

    static class ObtenerAdeudosHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Solicitando lista de adeudos");
            String adeudosJson = obtenerAdeudos();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, adeudosJson.length());
            OutputStream os = exchange.getResponseBody();
            os.write(adeudosJson.getBytes());
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

static class ObtenerVentasHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Solicitando reporte de ventas");
            String ventasJson = obtenerVentas();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, ventasJson.length());
            OutputStream os = exchange.getResponseBody();
            os.write(ventasJson.getBytes());
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

}