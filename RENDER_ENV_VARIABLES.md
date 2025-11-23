# Environment Variables for Render.com

## ⚠️ CRITICAL: You MUST set these in Render's Environment Variables

Go to: **Render Dashboard → Your Service → Environment Tab**

Add each of these variables:

## Database Configuration
```
DB_HOST=aws-0-us-west-1.pooler.supabase.com
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres.fiewexyeshsiqgewjxpv
DB_PASS=@Clavesegura03
```

## ClickUp Configuration
```
CLICKUP_CLIENT_ID=L336OIFQF2AJHPTSS3CI31DK48P1VPXZ
CLICKUP_CLIENT_SECRET=830J0N8V02UFDSICOSAT57SV64O2LOBHPQZUTG0XC0ZZXH0NDW6ZUQ377OUC8WBN
CLICKUP_ACCESS_TOKEN=pk_132156678_F1PYSRF2UFTQEVQI0XKEN2W9ETSFLN79
CLICKUP_TEAM_ID=90131026621
CLICKUP_SPACE_ID=90135726072
CLICKUP_LIST_ID=901312360970
```

## Supabase Configuration
```
SUPABASE_DB_WEBHOOK_SECRET=MiSecretoMuyFuerte
```

## ClickUp Lists by Type
```
CLICKUP_SPACE_ID_CONSTRUCTION=90135726072
CLICKUP_LIST_ID_CONSTRUCTION=901312360970
CLICKUP_SPACE_ID_PLUMBING=90135749663
CLICKUP_LIST_ID_PLUMBING=901312719543
```

## ClickUp Field IDs - PLUMBING
```
CLICKUP_CF_PLUMBING_LEADNUMBER=333e78db-249f-4e19-b95d-49a48d28dbb8
CLICKUP_CF_PLUMBING_LOCATION_TEXT=49dd4488-ecf5-417c-84b6-3cd700ff991c
CLICKUP_CF_PLUMBING_CONTACT_NAME=524a8b7c-cfb7-4361-886e-59a019f8c5b5
CLICKUP_CF_PLUMBING_CUSTOMER_NAME=c8dbf709-6ef9-479f-a915-b20518ac30e6
CLICKUP_CF_PLUMBING_EMAIL=f2220992-2039-4a6f-9717-b53ede8f5ec1
CLICKUP_CF_PLUMBING_PHONE=d648567f-d0e4-4ea0-af2d-1a8c5c44c2d8
CLICKUP_CF_PLUMBING_NOTES=448bb7fd-e7a4-4808-9201-e38860426dc2
```

## ClickUp Field IDs - CONSTRUCTION
```
CLICKUP_CF_CONSTRUCTION_LEADNUMBER=53d6e312-0f63-40ba-8f87-1f3092d8b322
CLICKUP_CF_CONSTRUCTION_LOCATION_TEXT=401a9851-6f11-4043-b577-4c7b3f03fb03
CLICKUP_CF_CONSTRUCTION_CONTACT_NAME=524a8b7c-cfb7-4361-886e-59a019f8c5b5
CLICKUP_CF_CONSTRUCTION_CUSTOMER_NAME=c8dbf709-6ef9-479f-a915-b20518ac30e6
CLICKUP_CF_CONSTRUCTION_EMAIL=f2220992-2039-4a6f-9717-b53ede8f5ec1
CLICKUP_CF_CONSTRUCTION_NOTES=4db2b087-6922-4dd7-9017-2e4a62cc2e95
```

---

## How to Add in Render:

1. Log in to https://dashboard.render.com
2. Click on your **marosserver** service
3. Click on **Environment** in the left sidebar
4. Click **Add Environment Variable**
5. Copy each KEY=VALUE pair above
6. For each variable:
   - Paste the KEY in the "Key" field
   - Paste the VALUE in the "Value" field
   - Click "Save"
7. After adding all variables, Render will automatically redeploy

## Verification:

After redeployment, check the logs. You should NO longer see:
- `$%257BCLICKUP_LIST_ID_CONSTRUCTION%257D`
- `$%257BCLICKUP_LIST_ID_PLUMBING%257D`

Instead, the actual list IDs should appear in the URLs.
