# Security Fixes Summary - Hardcoded Secrets and Credentials

## Security Issue Fixed
**OWASP A02:2021 - Cryptographic Failures**: Hardcoded secrets and credentials in source code and configuration files.

## Changes Made

### 1. Removed Hardcoded Secrets from application.properties
**File**: `src/main/resources/application.properties`

**Before**:
- `DB_PASSWORD` had default value `1234`
- `JWT_SECRET` had hardcoded Base64-encoded secret
- `CLIENT_ID` had hardcoded Microsoft App ID
- `CLIENT_SECRET` had hardcoded Microsoft App Secret
- `access-token` and `refresh-token` had hardcoded OAuth tokens

**After**:
- All secrets now require environment variables: `${DB_PASSWORD}`, `${JWT_SECRET}`, `${CLIENT_ID}`, `${CLIENT_SECRET}`, `${DATAJUD_API_KEY}`
- Removed hardcoded access-token and refresh-token entries
- Added `security.validate-secrets=false` for development (can be enabled in production)

### 2. Removed Hardcoded API Key from DataJudAdapter.java
**File**: `src/main/java/br/com/sgpj/adapter/api/DataJudAdapter.java`

**Before**:
```java
.header("Authorization", "APIKey cDZHYzlZa0JadVREZDJCendQbXY6SkJlTzNjLV9TRENyQk1RdnFKZGRQdw==")
```

**After**:
```java
@Value("${datajud.api-key}") String dataJudApiKey
.header("Authorization", "APIKey " + dataJudApiKey)
```

### 3. Added Secret Validation Configuration
**File**: `src/main/java/br/com/sgpj/config/SecretValidationConfig.java`

**Features**:
- Validates that all required secrets are set at application startup
- Can be enabled/disabled via `security.validate-secrets` property
- Prevents application from starting if critical secrets are missing
- Provides clear error messages for missing configuration

### 4. Added Security Tests
**File**: `src/test/java/br/com/sgpj/config/SecretValidationConfigTest.java`

**Test Coverage**:
- ✅ Validates exception when secrets are empty
- ✅ Validates exception when secrets are null
- ✅ Tests validation can be disabled for development
- ✅ Tests successful validation when all secrets are provided

### 5. Created Environment Variables Template
**File**: `.env.example`

**Purpose**:
- Provides template for required environment variables
- Documents all secrets that need to be configured
- Includes security best practices guidance

### 6. Updated .gitignore
**File**: `.gitignore`

**Added**:
- `.env`
- `.env.local`
- `.env.*.local`

**Purpose**: Prevent accidental commits of actual secret values

## Security Impact

### Before
- **Critical**: Database password, JWT secrets, API keys, and OAuth tokens were hardcoded in source code
- **Critical**: Anyone with repository access could extract production credentials
- **Critical**: Secrets could be exposed in version control history
- **Critical**: No validation that secrets were properly configured

### After
- **Secure**: All secrets must be provided via environment variables
- **Secure**: Application validates secret presence at startup
- **Secure**: Git prevents accidental commits of `.env` files
- **Secure**: Clear documentation of required configuration
- **Secure**: Production deployment can enforce secret validation

## Deployment Requirements

For production deployment, set the following environment variables:

```bash
# Database
export DB_PASSWORD=your_secure_database_password

# JWT
export JWT_SECRET=your_secure_jwt_secret_key_at_least_256_bits

# Microsoft Graph API
export CLIENT_ID=your_microsoft_app_client_id
export CLIENT_SECRET=your_microsoft_app_client_secret

# DataJud API
export DATAJUD_API_KEY=your_datajud_api_key

# Enable secret validation in production
export security.validate-secrets=true
```

## Testing Results

All tests pass successfully:
- ✅ SecretValidationConfigTest: 4/4 tests passed
- ✅ ProcessoServiceTest: 25/25 tests passed  
- ✅ HistoricoProcessoServiceTest: 10/10 tests passed
- ✅ Previous security tests still functional

## Files Modified

1. `src/main/resources/application.properties` - Removed hardcoded secrets
2. `src/main/java/br/com/sgpj/adapter/api/DataJudAdapter.java` - Inject API key from environment
3. `src/main/java/br/com/sgpj/config/SecretValidationConfig.java` - Added secret validation
4. `src/test/java/br/com/sgpj/config/SecretValidationConfigTest.java` - Added security tests
5. `.env.example` - Created environment variables template
6. `.gitignore` - Added environment file patterns

## Recommendations

1. **Immediate Action**: Ensure `.env.example` is committed but actual `.env` files are never committed
2. **Production**: Set `security.validate-secrets=true` to enforce secret validation
3. **Development**: Use `.env.local` for local development secrets (already gitignored)
4. **CI/CD**: Configure environment variables in your deployment pipeline
5. **Rotation**: Implement secret rotation policies for production environments
6. **Monitoring**: Add logging for secret validation failures in production

## Compliance

This fix addresses:
- **OWASP A02:2021** - Cryptographic Failures
- **OWASP A07:2021** - Identification and Authentication Failures (token exposure)
- **Security Best Practices** - Secrets management and credential protection
