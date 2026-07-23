# Tareas de Refactorización MDCapp - MVP Vendedor (Fase 11)

- [ ] **Fase 35: Reconfiguración de Estados y Comentarios**
    - [ ] Actualizar `BillingModel.kt` para auto-gestión de estados (`Pendiente`, `En proceso`, `Cobrado`).
    - [ ] Asegurar que `BillingComments` incluya fecha y texto.
- [ ] **Fase 36: Mejoras en Creación de Facturas**
    - [ ] Asignar `System.currentTimeMillis()` al campo `timeStamp` en `AddInvoiceViewModel.kt`.
    - [ ] Setear estado inicial como `Pendiente`.
    - [ ] Formatear el comentario inicial con la fecha actual.
- [ ] **Fase 37: Actualización del Dashboard**
    - [ ] Actualizar `availableStates` en `InvoicesPagedViewModel.kt` con la nueva lista.
    - [ ] Cambiar filtro por defecto a `Pendiente`.
- [ ] **Fase 38: Gestión en Detalle de Factura**
    - [ ] Implementar función para agregar comentarios con fecha en `DetailInvoiceViewModel.kt`.
    - [ ] Habilitar cambio manual de estado a `Devuelta` o `Cerrada`.
