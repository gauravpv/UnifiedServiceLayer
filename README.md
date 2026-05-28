# Bureau / Dedupe Caching Service

Spring Boot service that caches Bureau / Dedupe lookups with end-to-end AES encryption.

## Flow

```
POST /api/v1/process
  -> AES-CBC decrypt -> parse ProcessRequest
  -> route by config.serviceName (Bureau | dedupe)
  -> hash = SHA-256(canonicalized request.data)
  -> look up latest row by hash:
        fresh (< 30 days)  -> return cached
        stale or missing   -> call downstream, UPDATE or INSERT
  -> AES-CBC encrypt ProcessResponse -> return envelope
```

## Envelopes

Request:
```json
{
  "request":       "<Base64(AES-CBC(ProcessRequest))>",
  "reference_id":  "REF-123",
  "source_system": "SFDC"
}
```

Response (success):
```json
{
  "response":    "<Base64(AES-CBC(ProcessResponse))>",
  "status_code": "200",
  "message":     "Success",
  "bre_tat":     "123ms"
}
```

Response (error) — `response` is omitted:
```json
{ "status_code": "400", "message": "..." }
```

Decrypted `ProcessRequest`:
```json
{
  "config": { "serviceName": "Bureau", "requestId": "REQ-1", "...": "..." },
  "data":   { ...any JSON... }
}
```

Decrypted `ProcessResponse`:
```json
{
  "config": { ...echoed from request... },
  "data":   { ...downstream response... }
}
```

## Crypto

| Parameter | Value |
|---|---|
| Algorithm | `AES/CBC/PKCS5Padding` |
| Key       | `)H@McQfTjWnZr4u7x!A&C*F-JaNdRgUk` (32 bytes) |
| IV        | `w9z$C&F)J@NcRfUj` (16 bytes) |
| Charset   | UTF-8 |
| Pre-encrypt | replace `+` -> `~` in plaintext |
| Pre-decrypt | replace `~` -> `+` in ciphertext |

Code: `com.bajaj.security.EncryptionAspect`.

## Run

```powershell
$env:DB_USER="root"; $env:DB_PASS="<your-mysql-pwd>"
$env:BUREAU_API_URL="https://httpbin.org/anything"
$env:DEDUPE_API_URL="https://httpbin.org/anything"
mvn spring-boot:run
```

MySQL setup (run once):
```sql
CREATE DATABASE coe_cache;
USE coe_cache;
SOURCE src/main/resources/sql/01_create_bureau_bre_details.sql;
SOURCE src/main/resources/sql/02_create_dedupe_bre_details.sql;
SOURCE src/main/resources/sql/03_indexes.sql;
```

Swagger UI: http://localhost:8080/swagger-ui.html

## Postman

Import `postman/Bureau-Dedupe-Caching-Service.postman_collection.json`. Collection-level scripts encrypt the request and decrypt the response automatically; you only edit the `plaintextRequest` JSON in each request's pre-request tab.
