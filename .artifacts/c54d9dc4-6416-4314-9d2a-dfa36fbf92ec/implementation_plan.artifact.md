# Plan de Mejora de Estados y Comentarios de Facturación - Fase 11

Este plan aborda la visibilidad de las nuevas facturas mediante la asignación correcta de estados y timestamps, redefine los estados de cobranza y moderniza el sistema de comentarios.

## User Review Required

> [!IMPORTANT]
> **Nuevos Estados de Cobranza:** Se unificarán los estados a: `Pendiente`, `En proceso`, `Cobrado`, `Devuelta`, `Cerrada`, `Vencido` y `Por vencer`.
> Las facturas recién creadas se guardarán con estado `Pendiente` por defecto.

## Proposed Changes

### 1. Actualización de Modelos y Mapeadores

#### [MODIFY] [BillingModel.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/data/model/BillingModel.kt)
*   Asegurar que `BillingComments` soporte fecha y texto.
*   Actualizar `recalculate()` para que cambie automáticamente el estado a `En proceso` si hay un pago parcial, o `Cobrado` si el saldo es cero.

### 2. Corrección de Visibilidad en Creación

#### [MODIFY] [AddInvoiceViewModel.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/ui/viewmodels/invoices/AddInvoiceViewModel.kt)
*   **Timestamp:** Asignar `System.currentTimeMillis()` al crear la factura.
*   **Estado Inicial:** Asignar `Pendiente` por defecto.
*   **Fecha de Carga:** Asignar la fecha actual del sistema automáticamente.
*   **Comentarios Estructurados:** Guardar la nota inicial con la fecha actual.

### 3. Redefinición de Filtros en el Dashboard

#### [MODIFY] [InvoicesPagedViewModel.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/ui/viewmodels/invoices/InvoicesPagedViewModel.kt)
*   Actualizar `availableStates` con la nueva lista: `Pendiente`, `En proceso`, `Cobrado`, `Vencido`, `Por vencer`, `Devuelta`, `Cerrada`.
*   Cambiar el estado inicial seleccionado a `Pendiente` para que las facturas nuevas sean visibles de inmediato.

### 4. Gestión de Comentarios y Estados en el Detalle

#### [MODIFY] [DetailInvoiceViewModel.kt](file:///C:/Users/git/Android/MDCapp/composeApp/src/commonMain/kotlin/com/mdcapp/ui/viewmodels/invoices/DetailInvoiceViewModel.kt)
*   Implementar `addComment(text: String)`: Agrega un nuevo comentario con timestamp a la lista.
*   Implementar `updateState(newState: String)`: Permite cambiar manualmente el estado (ej: a `Devuelta` o `Cerrada`).

## Verification Plan

### Manual Verification
1.  **Visibilidad:** Crear una factura y verificar que aparezca inmediatamente en el Dashboard bajo la pestaña "Pendiente".
2.  **Timestamp:** Verificar en Firebase que el campo `Timestamp` ya no sea 0.
3.  **Comentarios:** Agregar un comentario desde el detalle de la factura y verificar que se apile en una lista con su respectiva fecha.
4.  **Auto-Estado:** Registrar un pago parcial y confirmar que el estado cambia automáticamente a `En proceso`. Registrar el pago total y confirmar que cambia a `Cobrado`.
