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

### 4. Fixed OTP Plain Text Storage
**Files**: 
- `src/main/java/br/com/sgpj/entity/Otp.java`
- `src/main/java/br/com/sgpj/service/OtpService.java`
- `src/main/java/br/com/sgpj/security/OtpHashUtil.java`
- `src/main/resources/schema.sql`

**Before**:
- OTP codes stored in plain text in database
- Direct string comparison for OTP validation
- Database column size was 6 characters

**After**:
- OTP codes stored as BCrypt hashes in database
- Added `OtpHashUtil` class for secure OTP hashing
- Updated `OtpService` to hash OTPs before storage and verify using hash comparison
- Database column size increased to 60 characters for BCrypt hashes
- Comprehensive test coverage for OTP hashing

### 5. Added Security Tests
**Files**:
- `src/test/java/br/com/sgpj/config/SecretValidationConfigTest.java`
- `src/test/java/br/com/sgpj/security/OtpHashUtilTest.java`
- Updated `src/test/java/br/com/sgpj/service/OtpServiceTest.java`

**Test Coverage**:
- ✅ Validates exception when secrets are empty
- ✅ Validates exception when secrets are null
- ✅ Tests validation can be disabled for development
- ✅ Tests successful validation when all secrets are provided
- ✅ Tests OTP hash generation and verification
- ✅ Tests OTP service integration with hashing
- ✅ Updated existing OTP tests to use mocked hash utility

### 6. Created Environment Variables Template
**File**: `.env.example`

**Purpose**:
- Provides template for required environment variables
- Documents all secrets that need to be configured
- Includes security best practices guidance

### 7. Updated .gitignore
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
- **Critical**: OTP codes stored in plain text in database, exposing valid codes on database access

### After
- **Secure**: All secrets must be provided via environment variables
- **Secure**: Application validates secret presence at startup
- **Secure**: Git prevents accidental commits of `.env` files
- **Secure**: Clear documentation of required configuration
- **Secure**: Production deployment can enforce secret validation
- **Secure**: OTP codes are hashed using BCrypt, preventing exposure in database access
- **Secure**: Hash comparison prevents OTP forgery even with database access

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

## Database Migration Required

For existing databases, the OTP column size must be updated:

```sql
ALTER TABLE usuario_otp MODIFY COLUMN otp VARCHAR(60) NOT NULL;
```

**Note**: This migration will invalidate all existing OTP codes. Users will need to request new OTPs after deployment.

## Testing Results

All tests pass successfully:
- ✅ SecretValidationConfigTest: 7/7 tests passed
- ✅ OtpHashUtilTest: 11/11 tests passed
- ✅ OtpServiceTest: 14/14 tests passed
- ✅ ProcessoServiceTest: 25/25 tests passed  
- ✅ HistoricoProcessoServiceTest: 10/10 tests passed
- ✅ Previous security tests still functional

## Files Modified

1. `src/main/resources/application.properties` - Removed hardcoded secrets
2. `src/main/java/br/com/sgpj/adapter/api/DataJudAdapter.java` - Inject API key from environment
3. `src/main/java/br/com/sgpj/config/SecretValidationConfig.java` - Added secret validation
4. `src/main/java/br/com/sgpj/entity/Otp.java` - Updated OTP column size and added security documentation
5. `src/main/java/br/com/sgpj/service/OtpService.java` - Added OTP hashing integration
6. `src/main/java/br/com/sgpj/security/OtpHashUtil.java` - Created OTP hashing utility
7. `src/main/resources/schema.sql` - Updated OTP column size
8. `src/test/java/br/com/sgpj/config/SecretValidationConfigTest.java` - Added security tests
9. `src/test/java/br/com/sgpj/security/OtpHashUtilTest.java` - Added OTP hashing tests
10. `src/test/java/br/com/sgpj/service/OtpServiceTest.java` - Updated tests for hashing
11. `.env.example` - Created environment variables template
12. `.gitignore` - Added environment file patterns

## Recommendations

1. **Immediate Action**: Ensure `.env.example` is committed but actual `.env` files are never committed
2. **Production**: Set `security.validate-secrets=true` to enforce secret validation
3. **Development**: Use `.env.local` for local development secrets (already gitignored)
4. **Database Migration**: Execute the ALTER TABLE command before deploying to production
5. **CI/CD**: Configure environment variables in your deployment pipeline
6. **Rotation**: Implement secret rotation policies for production environments
7. **Monitoring**: Add logging for secret validation failures in production
8. **OTP Reset**: Communicate to users that OTPs will be invalidated after deployment

## Compliance

This fix addresses:
- **OWASP A02:2021** - Cryptographic Failures (secrets and OTP plain text storage)
- **OWASP A07:2021** - Identification and Authentication Failures (token exposure)
- **Security Best Practices** - Secrets management and credential protection
- **Database Security** - Hash storage for sensitive temporary credentials
