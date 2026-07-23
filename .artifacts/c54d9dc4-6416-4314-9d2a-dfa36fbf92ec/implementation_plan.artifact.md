# Refactorización MDCapp - MVP Vendedor

Este plan detalla la transformación de la aplicación MDCapp de un visor de facturas a una herramienta completa de gestión para vendedores, permitiendo el registro de clientes, pedidos, facturas y pagos.

## User Review Required

> [!IMPORTANT]
> Se requiere confirmar si se desea persistencia local (SQLite/Room) adicional a Firestore, o si todo el flujo debe ser directamente contra la nube. Dado que se busca algo "práctico", se propone usar Firestore como fuente de verdad única para facilitar la sincronización entre dispositivos.

> [!WARNING]
> La implementación del "Semáforo de vencimientos" requiere que la fecha de recepción o de factura esté siempre presente para calcular el vencimiento automáticamente.

## Proposed Changes

### 1. Infraestructura y Autenticación

#### [MODIFY] [libs.versions.toml](file:///C:/Users/git/Android/MDCapp/gradle/libs.versions.toml)
* Agregar dependencia de Firebase Auth (GitLive).

#### [MODIFY] [build.gradle.kts](file:///C:/Users/git/Android/MDCapp/composeApp/build.gradle.kts)
* Incluir la librería de Auth en `commonMain`.

#### [NEW] Login Screen & ViewModel
* Implementar pantalla de acceso para vendedores.

### 2. Gestión de Clientes

#### [NEW] AddClientScreen
* Formulario para dar de alta nuevos clientes (`ClientModel`).
* Integración en `ClientService` para guardar en Firestore.

### 3. Gestión de Pedidos (Orders)

#### [NEW] CreateOrderScreen
* Selección de Fábrica/Segmento.
* Ingreso manual de artículos (Nombre, Color, Cantidad de pares, Precio).
* Selección de condiciones de pago (Días, Descuento).
* Notas de venta.

#### [MODIFY] [OrderService.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/data/service/OrderService.kt)
* Agregar funciones para crear pedidos (`saveOrder`).

### 4. Facturación y Pagos

#### [NEW] ManageInvoicesScreen
* Asignar facturas/remitos a un pedido existente.
* Cálculo automático de:
    * `toPay`: Monto con descuento aplicado según la condición de pago.
    * `payDate`: Fecha estimada de pago.
    * `dueDate`: Fecha de vencimiento real.
* Registro de pagos parciales o totales actualizando el saldo pendiente (`rest`).

#### [NEW] InvoiceList (Dashboard)
* Listado ordenado por estado (Pendiente, Pagado, Vencido).
* Indicador visual (Semáforo):
    * Verde: Al día / Pagado.
    * Amarillo: Próximo a vencer (< 7 días).
    * Rojo: Vencido.

### 5. Navegación

#### [MODIFY] [AndroidNavigation.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/androidMain/kotlin/com/mdcapp/ui/navigation/AndroidNavigation.kt)
* Actualizar el `NavHost` para incluir el flujo: Login -> Home (Clients/Dashboard) -> Add Client -> Create Order -> Invoices.

## Verification Plan

### Manual Verification
1.  **Autenticación:** Verificar que solo usuarios logueados accedan a la app.
2.  **Clientes:** Crear un cliente y verificar su aparición en la lista.
3.  **Pedidos:** Generar un pedido con artículos manuales y condiciones de pago.
4.  **Facturas:** Agregar una factura a ese pedido y verificar el cálculo de fechas automáticas.
5.  **Pagos:** Registrar un pago parcial y confirmar que el saldo se actualiza correctamente.
6.  **Semáforo:** Cambiar la fecha de vencimiento de una factura para verificar que el color del indicador cambie a rojo si está vencida.
