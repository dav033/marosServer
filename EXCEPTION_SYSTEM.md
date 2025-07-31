# Sistema de Excepciones Personalizadas

## Resumen
Se ha implementado un sistema completo de excepciones personalizadas que elimina la necesidad de bloques `try-catch` repetitivos en los controladores y simplifica el manejo de errores en toda la aplicación.

## Estructura del Sistema

### 1. Excepciones Base

#### `BaseException`
- Clase abstracta que sirve como base para todas las excepciones personalizadas
- Incluye campos para código de error, estado HTTP y argumentos adicionales
- Proporciona múltiples constructores para diferentes casos de uso

#### Tipos de Excepciones Derivadas

**`BusinessException`**
- Para errores de lógica de negocio
- Status HTTP: 400 (BAD_REQUEST)
- Código de error: "BUSINESS_ERROR"

**`ValidationException`**
- Para errores de validación de datos
- Status HTTP: 400 (BAD_REQUEST)
- Código de error: "VALIDATION_ERROR"

**`ResourceNotFoundException`**
- Para recursos no encontrados
- Status HTTP: 404 (NOT_FOUND)
- Código de error: "RESOURCE_NOT_FOUND"

**`ExternalServiceException`**
- Para errores en servicios externos (ClickUp, APIs, etc.)
- Status HTTP: 502 (BAD_GATEWAY)
- Código de error: "EXTERNAL_SERVICE_ERROR"

**`DatabaseException`**
- Para errores de base de datos
- Status HTTP: 500 (INTERNAL_SERVER_ERROR)
- Código de error: "DATABASE_ERROR"

### 2. Excepciones Específicas del Dominio

#### `LeadExceptions`
```java
// Lead no encontrado
throw new LeadExceptions.LeadNotFoundException(leadId);

// Número de lead duplicado
throw new LeadExceptions.DuplicateLeadNumberException(leadNumber);

// Estado de lead inválido
throw new LeadExceptions.InvalidLeadStatusException(status);

// Error al crear lead
throw new LeadExceptions.LeadCreationException(message, cause);
```

#### `ContactExceptions`
```java
// Contacto no encontrado por ID
throw new ContactExceptions.ContactNotFoundException(contactId);

// Contacto no encontrado por nombre
throw new ContactExceptions.ContactNotFoundException(contactName);

// Contacto duplicado
throw new ContactExceptions.DuplicateContactException(contactName);

// Datos de contacto inválidos
throw new ContactExceptions.InvalidContactDataException(field, value);
```

#### `ProjectTypeExceptions`
```java
// Tipo de proyecto no encontrado
throw new ProjectTypeExceptions.ProjectTypeNotFoundException(projectTypeId);

// Tipo de proyecto inválido
throw new ProjectTypeExceptions.InvalidProjectTypeException(projectType);
```

### 3. Global Exception Handler

El `ApiExceptionHandler` maneja automáticamente todas las excepciones:

- **Excepciones personalizadas**: Manejo específico según el tipo
- **Excepciones de Spring**: Validación, binding, argumentos, etc.
- **Excepciones de JPA**: EntityNotFoundException, DataIntegrityViolationException
- **Excepciones genéricas**: Cualquier excepción no manejada específicamente

#### Características del Handler

1. **Logging automático** con TraceId único para rastrear errores
2. **Respuestas estandarizadas** con formato JSON consistente
3. **Mensajes en español** amigables para el usuario
4. **Información detallada** para debugging (en logs)

### 4. Formato de Respuesta de Error

```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Contacto no encontrado con ID: 123",
  "errorCode": "RESOURCE_NOT_FOUND",
  "path": "/contacts/123",
  "traceId": "a1b2c3d4",
  "details": null,
  "fieldErrors": null
}
```

Para errores de validación:
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 400,
  "error": "Validation Failed",
  "message": "Los datos enviados no son válidos",
  "errorCode": "VALIDATION_FAILED",
  "path": "/leads/new-contact",
  "traceId": "e5f6g7h8",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "El email debe tener un formato válido"
    }
  ]
}
```

## Uso en Servicios

### Antes (con try-catch)
```java
@Transactional
public Leads CreateLeadByNewContact(Leads lead, Contacts contact) {
    try {
        // lógica del método
        return result;
    } catch (DataIntegrityViolationException e) {
        System.err.println("Error: " + e.getMessage());
        throw new RuntimeException("Error al crear el lead", e);
    }
}
```

### Después (sin try-catch)
```java
@Transactional
public Leads CreateLeadByNewContact(Leads lead, Contacts contact) {
    // Validaciones con excepciones específicas
    ContactsEntity contactEntity = contactsRepository.findById(contactId)
            .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(contactId));
    
    ProjectTypeEntity projectTypeEntity = projectTypeRepository.findById(projectTypeId)
            .orElseThrow(() -> new ProjectTypeExceptions.ProjectTypeNotFoundException(projectTypeId));
    
    try {
        // Solo operaciones de base de datos que pueden fallar
        LeadsEntity savedLeadEntity = repository.save(leadEntity);
        return leadMapper.toDto(savedLeadEntity);
    } catch (DataIntegrityViolationException e) {
        throw new LeadExceptions.LeadCreationException("Error de integridad de datos", e);
    }
}
```

## Uso en Controladores

### Antes (con try-catch)
```java
@PostMapping("/new-contact")
public ResponseEntity<Leads> createLeadByNewContact(@RequestBody CreateLeadByNewContactRequest request) {
    try {
        Leads lead = leadsService.CreateLeadByNewContact(request.getLead(), request.getContact());
        return ResponseEntity.ok(lead);
    } catch (Exception e) {
        System.out.println(e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}
```

### Después (sin try-catch)
```java
@PostMapping("/new-contact")
public ResponseEntity<Leads> createLeadByNewContact(@RequestBody CreateLeadByNewContactRequest request) {
    Leads lead = leadsService.CreateLeadByNewContact(request.getLead(), request.getContact());
    return ResponseEntity.ok(lead);
}
```

## Beneficios del Sistema

1. **Código más limpio**: Eliminación de try-catch repetitivos
2. **Manejo consistente**: Todas las excepciones se manejan de forma uniforme
3. **Mejor debugging**: TraceId único para cada error
4. **Respuestas estandarizadas**: Formato JSON consistente para el frontend
5. **Separación de responsabilidades**: Los controladores se enfocan en la lógica HTTP
6. **Mantenibilidad**: Fácil agregar nuevos tipos de excepciones
7. **Logging centralizado**: Todos los errores se registran automáticamente

## Migraciones Realizadas

### Servicios Actualizados
- ✅ `ContactsService`: Usa `ContactExceptions`
- ✅ `LeadsService`: Usa `LeadExceptions`, `ContactExceptions`, `ProjectTypeExceptions`
- ✅ `ClickUpService`: Mantiene `ClickUpException` mejorada

### Controladores Simplificados
- ✅ `ContactsController`: Sin try-catch
- ✅ `LeadsController`: Sin try-catch
- ✅ `ProjectTypeController`: Sin try-catch
- ✅ `WebhookController`: Sin try-catch

## Próximos Pasos

1. **Agregar validaciones personalizadas** usando anotaciones de Bean Validation
2. **Implementar logging de métricas** para análisis de errores
3. **Crear excepciones específicas** para otros módulos según sea necesario
4. **Documentar APIs** con los códigos de error en Swagger/OpenAPI

## Ejemplo Completo de Uso

```java
// En el servicio
public Contact updateContact(Long id, Contact contactData) {
    // Validación de existencia
    ContactsEntity entity = repository.findById(id)
            .orElseThrow(() -> new ContactExceptions.ContactNotFoundException(id));
    
    // Validación de negocio
    if (contactData.getEmail() != null && !isValidEmail(contactData.getEmail())) {
        throw new ContactExceptions.InvalidContactDataException("email", contactData.getEmail());
    }
    
    // Operación que puede fallar
    try {
        mapper.updateEntity(contactData, entity);
        ContactsEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    } catch (DataIntegrityViolationException e) {
        if (e.getMessage().contains("email")) {
            throw new ContactExceptions.DuplicateContactException(contactData.getEmail());
        }
        throw new DatabaseException("Error al actualizar contacto", e);
    }
}

// En el controlador
@PutMapping("/{id}")
public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
    Contact updated = contactsService.updateContact(id, contact);
    return ResponseEntity.ok(updated);
}
```

El sistema manejará automáticamente cualquier excepción y devolverá una respuesta JSON apropiada con el código de estado HTTP correcto.
