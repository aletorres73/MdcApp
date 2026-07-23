# Plan de Refactorización y Nuevas Funcionalidades - Fase 2

Este plan detalla la evolución de MDCapp para soportar multi-usuario, gestión completa de fábricas, registro de usuarios y mejoras de UI/UX.

## User Review Required

> [!IMPORTANT]
> **Aislamiento de Datos:** Se implementará una estructura de colecciones `users/{userId}/[clients|orders|billings|factories]`. Esto significa que al registrarse, el usuario empezará con sus listas vacías.
> ¿Deseas que migremos algún dato de las colecciones globales actuales al primer usuario que se registre, o empezamos de cero para todos?

> [!NOTE]
> **Fábricas y Segmentos:** Se utilizará el término "Segmentos" en la UI para lo que internamente en el código se llama "Marcas" (`branchList`), para mantener la consistencia con tu pedido.

## Proposed Changes

### 1. Autenticación y Multi-tenencia (Aislamiento de Datos)

#### [MODIFY] [AuthService.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/data/service/AuthService.kt)
* Agregar función `signUp(email, password)` para registro de nuevos vendedores.

#### [MODIFY] Servicios de Datos (`ClientService`, `OrderService`, `BillingPaginationService`)
* Refactorizar para que todas las consultas a Firestore incluyan el `userId` del usuario autenticado.
* Nueva ruta base: `users/{userId}/...`

### 2. Navegación y Estructura Principal

#### [NEW] MainScreen (Scaffold con BottomBar)
* Implementar una pantalla contenedora con `NavigationBar`.
* Ítems: **Facturas** e **Clientes**.

#### [MODIFY] [AndroidNavigation.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/androidMain/kotlin/com/mdcapp/ui/navigation/AndroidNavigation.kt)
* Ajustar el `NavHost` para soportar la nueva estructura de `MainScreen`.

### 3. Gestión de Clientes (CRUD Completo)

#### [NEW] ClientsScreen.kt
* Listado de clientes con búsqueda.
* Opciones de Editar y Eliminar para cada cliente.
* Reutilización de `AddClientScreen` para el modo "Edición".

### 4. Gestión de Fábricas y Segmentos

#### [NEW] FactoryManagementScreen
* Listado de fábricas creadas por el usuario.
* Creación/Edición de Fábricas:
    * Nombre de la fábrica.
    * Lista de **Segmentos** (Agregar/Editar/Eliminar).
    * Asignación de **Condiciones de Pago** (usando la lógica de `PaymentCondition`).

### 5. Mejoras de UI/UX y Registro

#### [NEW] SignUpScreen.kt
* Pantalla de registro para nuevos usuarios.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/ui/screens/LoginScreen.kt)
* Agregar icono de "ojo" para alternar visibilidad de contraseña.
* Enlace a la pantalla de Registro.

#### [MODIFY] Restricciones de Entrada (Inputs)
* Configurar `singleLine = true` en todos los campos de texto de entrada.
* Configurar `keyboardType = KeyboardType.Number` para campos numéricos (cantidades, montos).

## Verification Plan

### Manual Verification
1.  **Registro:** Crear un nuevo usuario y verificar que se guarde en Firebase Auth.
2.  **Aislamiento:** Crear un cliente con el "Usuario A", desloguearse, entrar con "Usuario B" y verificar que la lista esté vacía.
3.  **Fábricas:** Crear una fábrica, agregarle 3 segmentos, editar uno y eliminar otro.
4.  **UI:** Verificar que al presionar "Enter" en los inputs no se creen nuevas líneas y que el teclado numérico aparezca en los campos de "pares".
5.  **BottomBar:** Navegar entre Facturas y Clientes asegurando que el estado de la pantalla anterior se mantenga (si es posible).
