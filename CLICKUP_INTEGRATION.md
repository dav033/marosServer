# Integración Supabase → ClickUp

Esta integración permite crear y eliminar automáticamente tareas en ClickUp cuando se insertan o eliminan leads en la base de datos de Supabase.

## 🔧 Configuración

### 1. Variables de Entorno (.env)

Completa las siguientes variables en el archivo `.env`:

```properties
# ClickUp Configuration
CLICKUP_ACCESS_TOKEN=tu_token_de_acceso_personal
CLICKUP_TEAM_ID=tu_team_id
CLICKUP_SPACE_ID=tu_space_id  
CLICKUP_LIST_ID=tu_list_id
```

### 2. Obtener Token de ClickUp

1. Ve a ClickUp → Settings → Apps
2. Genera un "Personal Access Token"
3. Copia el token y ponlo en `CLICKUP_ACCESS_TOKEN`

### 3. Obtener IDs de ClickUp

Para obtener los IDs necesarios, puedes usar la API de ClickUp:

```bash
# Listar teams
curl -H "Authorization: YOUR_ACCESS_TOKEN" \
  https://api.clickup.com/api/v2/team

# Listar spaces
curl -H "Authorization: YOUR_ACCESS_TOKEN" \
  https://api.clickup.com/api/v2/team/TEAM_ID/space

# Listar listas
curl -H "Authorization: YOUR_ACCESS_TOKEN" \
  https://api.clickup.com/api/v2/space/SPACE_ID/list
```

### 4. Configurar Webhook en Supabase

1. Ve a tu proyecto en Supabase
2. Navega a Database → Webhooks
3. Crea un nuevo webhook con:
   - **Table**: `leads`
   - **Events**: `INSERT`, `DELETE`
   - **URL**: `https://tu-dominio.com/api/webhook/supabase`
   - **HTTP Headers**: 
     ```
     Authorization: Bearer MiSecretoMuyFuerte
     ```

## 📡 Endpoints

### Webhook Principal
```
POST /api/webhook/supabase
```
Recibe webhooks de Supabase y crea tareas en ClickUp.

### Health Check
```
GET /api/webhook/health
```
GET /api/webhook/health
```
Verifica que el servicio está funcionando.

### Endpoint de Test para Crear Tarea
```
POST /api/webhook/test-contact/{contactId}
```
Simula la creación de una tarea con un contacto real.

### Endpoint de Test para Eliminar Tarea
```
POST /api/webhook/test-delete/{leadId}
```
Simula la eliminación de una tarea basada en un leadId existente.

## 📝 Operaciones Soportadas

### Creación de Tareas (INSERT)

Cuando se crea un nuevo lead, se genera una tarea con:

- **Nombre**: `Lead: [Nombre del Lead] ([Número de Lead])`
- **Descripción**: Detalles del lead incluyendo ubicación, fecha, tipo
- **Tags**: `lead`, tipo de lead, `automated`
- **Prioridad**: Normal (configurable)

Además, se guarda un mapping en la tabla `lead_clickup_mapping` para relacionar el lead con la tarea de ClickUp.

### Eliminación de Tareas (DELETE)

Cuando se elimina un lead:

1. Se busca el mapping correspondiente en `lead_clickup_mapping`
2. Se elimina la tarea en ClickUp usando la API
3. Se elimina el registro de mapping de la base de datos

## 🗄️ Esquema de Base de Datos

### Tabla: lead_clickup_mapping

```sql
CREATE TABLE lead_clickup_mapping (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL UNIQUE,
    lead_number VARCHAR(255) NOT NULL,
    clickup_task_id VARCHAR(255) NOT NULL,
    clickup_task_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Esta tabla mantiene la relación entre los leads de Supabase y las tareas de ClickUp.

## 🔒 Seguridad

- El webhook valida el secret configurado en `SUPABASE_DB_WEBHOOK_SECRET`
- Solo procesa eventos INSERT y DELETE en la tabla `leads`
- Manejo de errores con logging detallado
- Validación de integridad de datos antes de operaciones en ClickUp

## 🚀 Testing

### Probar el Health Check
```bash
curl http://localhost:8080/api/webhook/health
```

### Simular Webhook de Creación (INSERT)
```bash
curl -X POST http://localhost:8080/api/webhook/supabase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MiSecretoMuyFuerte" \
  -d '{
    "type": "INSERT",
    "table": "leads",
    "schema": "public",
    "record": {
      "id": 1,
      "name": "Test Lead",
      "lead_number": "001-0725",
      "location": "Test Location",
      "start_date": "2025-07-30",
      "status": "TO_DO",
      "lead_type": "CONSTRUCTION"
    }
  }'
```

### Simular Webhook de Eliminación (DELETE)
```bash
curl -X POST http://localhost:8080/api/webhook/supabase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MiSecretoMuyFuerte" \
  -d '{
    "type": "DELETE",
    "table": "leads",
    "schema": "public",
    "record": {
      "id": 1,
      "lead_number": "001-0725"
    }
  }'
```

### Test con Contacto Real (CREATE)
```bash
curl -X POST http://localhost:8080/api/webhook/test-contact/123
```

### Test de Eliminación (DELETE)
```bash
curl -X POST http://localhost:8080/api/webhook/test-delete/1
```

## 📋 Logs

El sistema registra:
- Webhooks recibidos (INSERT/DELETE)
- Tareas creadas y eliminadas en ClickUp
- Mappings guardados y eliminados
- Errores y excepciones
- Configuración faltante

### Ejemplos de Logs:

**Creación exitosa:**
```
INFO - Webhook recibido de Supabase: tabla=leads, tipo=INSERT
INFO - Procesando INSERT de lead
INFO - Creando tarea en ClickUp: Lead: Test Lead (001-0725)
INFO - Tarea creada con éxito en ClickUp → id=abc123, url=https://...
INFO - Mapping guardado: leadId=1 → clickUpTaskId=abc123
```

**Eliminación exitosa:**
```
INFO - Webhook recibido de Supabase: tabla=leads, tipo=DELETE
INFO - Procesando DELETE de lead
INFO - Eliminando tarea en ClickUp: abc123
INFO - Tarea eliminada con éxito en ClickUp → id=abc123
INFO - Lead y tarea eliminados: leadId=1 → clickUpTaskId=abc123
```

Verifica los logs en caso de problemas.
