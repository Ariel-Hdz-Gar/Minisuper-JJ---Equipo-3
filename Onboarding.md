## Onboarding del Proyecto: MinisuperJJ

## Uso principal del sistema

Este software te ayudará a realizar los cobros de forma rápida, registrar la llegada de mercancía y llevar un control claro de las cuentas del negocio sin necesidad de anotaciones en papel.

El software esta diseñado para realizar un control de inventario de un minisuper, hace uso de una base de datos para hacer un conteo de productos, registros de ventas, entradas y salidas de productos, conteos de cajas y un resumen de las entradas del dia

## Usos de los modulos del sistema

1. Alertas
El módulo de **Alertas** es una herramienta preventiva que detecta automáticamente cuando el stock de un producto de alta rotación cae por debajo de su límite configurado. A través de un cuadro de resumen, una tabla de faltantes y mensajes de urgencia, la interfaz permite evaluar el nivel de escasez y tomar decisiones mediante acciones individuales o globales. El flujo concluye al generar una orden de compra conectada directamente con el *Registro de Entrada*, asegurando el resurtido oportuno para evitar el desabasto.

2. Caja
El módulo de Caja (Gestion.html) es una herramienta enfocada en el control financiero diario y la auditoría. Su flujo permite registrar el balance dinámico entre las entradas por ventas y las salidas por pagos a proveedores o abonos de adeudos. Mediante un indicador de estado, resumen estructurado y notificaciones, la interfaz guía al usuario a ingresar el conteo físico de dinero para compararlo contra el cálculo teórico del sistema, resaltando de inmediato cualquier discrepancia o alerta de conciliación. El proceso concluye al ejecutar el botón de acción crítica para "Cerrar Caja", lo que bloquea los registros del día y genera un reporte de transparencia enviado automáticamente a la administración para su fiscalización.

3. Adeudos
El módulo de Adeudos (Adeuso.html) es una herramienta de alerta y control financiero enfocada en el seguimiento de las ventas realizadas a crédito. Su flujo inicia listando las cuentas pendientes provenientes del *Registro de Venta*, utilizando un semáforo de morosidad y una tabla de seguimiento que muestran los días de atraso de cada cliente para tomar decisiones informadas sobre la concesión de nuevos créditos. La funcionalidad central permite registrar abonos y pagos para recuperar capital, enviando automáticamente dicho ingreso al módulo de *Caja*. Además, incluye accesos para dar de alta a nuevos clientes frecuentes, facilitando un crecimiento comercial controlado mediante una estructura transparente de auditoría.

4. Inventario
El módulo de Inventario (Registro_Entrada.html, Registro_producto.html, Registro_venta.html) es el núcleo operativo que gestiona el ciclo completo de los productos desde su alta hasta su comercialización. Su flujo inicia en el Registro de Producto, donde se definen las especificaciones base y los límites para alertas de stock. Continúa en el Registro de Entrada, que actualiza las existencias físicas al recibir mercancía de proveedores y genera automáticamente el registro de egreso económico correspondiente. Finalmente, la interfaz de Registro de Venta facilita el cobro escaneando artículos en tiempo real, calculando impuestos y procesando pagos en efectivo o mediante "Venta a Crédito", la cual canaliza deudas hacia el módulo de Adeudos. La integración automática entre estos tres submódulos garantiza que las entradas sumen y las ventas descuenten inventario en tiempo real sin requerir ajustes manuales.

5. Reporte
El módulo de Reporte (Reporte.html) es la herramienta de análisis financiero y auditoría orientada a la evaluación del desempeño del negocio. Su flujo permite al usuario definir rangos de fechas específicos y alternar entre distintos tipos de consultas para identificar los productos de mayor rotación y analizar las métricas clave de venta. A través de un cuadro de resumen y una tabla de resultados dinámica, la interfaz desglosa los montos totales, las vías de pago utilizadas y las deudas que quedaron registradas como adeudos pendientes. Finalmente, el proceso concluye facilitando la portabilidad de la información mediante la exportación de los datos consolidados a formatos PDF o Excel para su posterior revisión y control administrativo.

6. Seguridad
El módulo de Seguridad (Seguridad.html) es la herramienta de control administrativo responsable de la gestión de personal y la asignación de roles dentro del sistema. Su flujo inicia con un formulario de captura que permite registrar nuevos perfiles laborales utilizando el RFC como identificador único, lo que garantiza la integridad de los datos de cada trabajador. La funcionalidad principal réside en la definición y asignación de cargos, mecanismo fundamental para restringir y administrar los permisos de acceso a las distintas áreas del sistema del minisuper. El proceso concluye en una tabla de visualización de registros que permite al administrador verificar, auditar y consultar rápidamente la información de la plantilla laboral.

## Realizar cambios en el prototipo

Para realizar cambios en el prototipo del software solo se tienen que editar los archvios siguientes: 
-ISWConexion.java que se encuentra en la carpeta del Back-End (Java)
-Los archivos .HTML de la carpeta de Front-End (JavaScript, HTML, CSS)
-El servidor de base de datos de MariaDB usando el host con la ip 127.0.0.1, y con el puerto 3306