# Integración Supabase → ClickUp

Esta integración permite crear automáticamente tareas en ClickUp cuando se insertan nuevos leads en la base de datos de Supabase.

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
   - **Events**: `INSERT`
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
Verifica que el servicio está funcionando.

## 📝 Formato de Tarea en ClickUp

Cuando se crea un nuevo lead, se genera una tarea con:

- **Nombre**: `Lead: [Nombre del Lead] ([Número de Lead])`
- **Descripción**: Detalles del lead incluyendo ubicación, fecha, tipo
- **Tags**: `lead`, tipo de lead, `automated`
- **Estado**: Mapeado desde el status del lead
- **Prioridad**: Normal (configurable)

## 🔒 Seguridad

- El webhook valida el secret configurado en `SUPABASE_DB_WEBHOOK_SECRET`
- Solo procesa eventos INSERT en la tabla `leads`
- Manejo de errores con logging detallado

## 🚀 Testing

### Probar el Health Check
```bash
curl http://localhost:8080/api/webhook/health
```

### Simular Webhook de Supabase
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

## 📋 Logs

El sistema registra:
- Webhooks recibidos
- Tareas creadas en ClickUp
- Errores y excepciones
- Configuración faltante

Verifica los logs en caso de problemas.
