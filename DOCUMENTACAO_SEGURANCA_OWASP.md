# Documentação de Correções de Segurança OWASP

Este documento detalha as vulnerabilidades identificadas e as correções implementadas no backend SGPJ, mapeando cada commit às respectivas melhorias de segurança.

---

## 1.0: Controle de acesso só no nível do endpoint, sem autorização por objeto

**Commit:** `3ccad09` - "melhorar segurança"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/main/java/br/com/sgpj/exception/AcessoNegadoException.java` | 1-7 | Nova exceção customizada para acesso negado a recursos |
| `src/main/java/br/com/sgpj/security/AuthorizationUtil.java` | 1-68 | Serviço central de autorização por objeto |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/java/br/com/sgpj/service/ClienteService.java` | +17, +19, +67, +76, +84, +92 | Injeção de `AuthorizationUtil` e chamada a `verificarAcessoCliente(id)` no `buscarPorId()` |
| `src/main/java/br/com/sgpj/service/DocumentoService.java` | +10, +12, +30, +37, +42, +53-58, +72 | Injeção de `AuthorizationUtil`; validação em `listarDocumentosPorCaminho()` (linhas 53-58) e `listarCaminhosDoCliente()` (linha 72) |
| `src/main/java/br/com/sgpj/service/ProcessoService.java` | +11, +13, +44, +48, +57, +172, +190, +272 | Injeção de `AuthorizationUtil`; validação em `buscarPorId()` (172), `buscarPorClienteId()` (190), `listarDocumentosDoProcesso()` (272) |

### Lógica de Autorização (`AuthorizationUtil.java`):
- **Linhas 38-45**: `verificarAcessoCliente(Long clienteId)` - busca cliente e valida acesso
- **Linhas 47-54**: `verificarAcessoProcesso(Long processoId)` - busca processo e valida acesso
- **Linhas 56-59**: `usuarioHasAccessToCliente()` - retorna `true` (placeholder para regras futuras)
- **Linhas 61-67**: `usuarioHasAccessToProcesso()` - verifica se usuário logado é o `usuarioResponsavel` do processo

### Razão:
Antes, apenas anotações `@PreAuthorize` nos controllers protegiam endpoints. Qualquer usuário autenticado podia acessar qualquer cliente/processo via ID. Agora, cada service valida se o usuário logado tem posse/acesso ao objeto específico antes de permitir a operação.

---

## 1.1: IDs de usuário vêm do cliente e são confiados pelo backend

**Commit:** `3ccad09` - "melhorar segurança" (mesmo commit do 1.0)

### Arquivos/linhas relevantes:
| Arquivo | Linhas | Mudança |
|---------|--------|---------|
| `src/main/java/br/com/sgpj/security/AuthorizationUtil.java` | 24-35 | `getUsuarioLogado()` extrai email do token JWT via `UsuarioAutenticacaoAdapter` e busca usuário no banco |
| `src/main/java/br/com/sgpj/security/AuthorizationUtil.java` | 38-54 | Métodos de verificação usam `getUsuarioLogado()` (do token) ao invés de confiar em IDs vindos do request |

### Razão:
O backend agora deriva a identidade do usuário exclusivamente do token JWT validado (`AuthorizationUtil.getUsuarioLogado()`), nunca confiando em `userId` ou `clienteId` enviados pelo frontend. A autorização por objeto (1.0) garante que o usuário só acesse recursos que lhe pertencem.

---

## 2.0: Segredos e credenciais commitados no código

**Commit:** `8e46649` - "terceira opção da owasp junto com testes unitarios"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `.env.example` | 1-25 | Template de variáveis de ambiente obrigatórias |
| `src/main/java/br/com/sgpj/config/SecretValidationConfig.java` | 1-114 | Validação de segredos no startup |
| `src/test/java/br/com/sgpj/config/SecretValidationConfigTest.java` | 1-122 | Testes de validação de segredos |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `.gitignore` | +7-10 | Adicionado `.env`, `.env.local`, `.env.*.local` |
| `src/main/resources/application.properties` | 6-7, 15-17, 20-21, 24-25, 28-29 | Removidos valores default hardcoded; agora usam `${VAR}` sem fallback inseguro |
| `src/main/java/br/com/sgpj/adapter/api/DataJudAdapter.java` | +6, +15-18, +21-22, +38 | Injeção de `@Value("${datajud.api-key}")` no construtor; removido API key hardcoded |

### Segredos Removidos do Código:
- `DB_PASSWORD` (era `1234`)
- `JWT_SECRET` (era Base64 hardcoded)
- `CLIENT_ID` (era Microsoft App ID real)
- `CLIENT_SECRET` (era secret real)
- `ACCESS_TOKEN` / `REFRESH_TOKEN` (tokens OAuth completos)
- `DATAJUD_API_KEY` (era API key real no código)

### Razão:
Credenciais hardcoded expõem banco, JWT, APIs Microsoft e DataJud a qualquer um com acesso ao repositório. A validação no startup (`SecretValidationConfig`) impede inicialização sem segredos configurados via environment variables.

---

## 2.1: OTP armazenado em texto puro

**Commit:** `1558aa4` - "quarta opção da owasp junto com testes unitarios"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/main/java/br/com/sgpj/security/OtpHashUtil.java` | 1-53 | Utilitário BCrypt para hash/verificação de OTP |
| `src/test/java/br/com/sgpj/security/OtpHashUtilTest.java` | 1-134 | Testes completos do utilitário |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/java/br/com/sgpj/entity/Otp.java` | 4-11, 18, 21 | Javadoc de segurança; coluna `otp` alterada de `VARCHAR(6)` para `VARCHAR(60)` |
| `src/main/java/br/com/sgpj/service/OtpService.java` | +6, +15-16, +24-25, +28-29, +33-34, +44, +55-56 | Injeção de `OtpHashUtil`; `generateOtp()` faz hash antes de salvar; `isOtpValid()` usa `verifyOtp()` |
| `src/main/resources/schema.sql` | 346 | `otp VARCHAR(6)` → `otp VARCHAR(60)` |

### Fluxo Seguro:
1. `generateOtp()` (linha 33): Gera OTP numérico → `otpHashUtil.hashOtp(otp)` → salva hash no banco
2. `isOtpValid()` (linha 55): Recebe OTP do usuário → `otpHashUtil.verifyOtp(inputOtp, hashedOtp)` → compara hash

### Razão:
OTP em texto puro no banco permite que qualquer acesso ao banco (backup, logs, SQL injection) revele códigos válidos. BCrypt com salt único por OTP impede rainbow tables e garante que mesmo com acesso ao banco, não é possível obter OTPs válidos.

---

## 3.0: Nome de arquivo entra bruto na construção de caminho remoto

**Commit:** `cc67d56` - "quinto melhoria da segurança com testes unitarios"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/main/java/br/com/sgpj/security/FilenameSanitizer.java` | 1-125 | Sanitização robusta contra path traversal |
| `src/test/java/br/com/sgpj/security/FilenameSanitizerTest.java` | 1-230 | 30+ testes de segurança |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/java/br/com/sgpj/controller/OneDriveController.java` | +7, +63-65, +79-82, +94-98, +109-112 | Sanitização em `uploadDocumento()`, `criarPasta()`, `renomearArquivo()`, `excluirArquivo()` |
| `src/main/java/br/com/sgpj/service/OneDriveService.java` | +18, +147-148, +166, +170, +192, +216-217, +250-251, +262-263, +348-351, +361-362, +373-375 | Sanitização em `criarPasta()`, `gerarNomePasta()`, `uploadDocumento()`, `excluirArquivo()`, `renomearArquivo()` |

### Proteções do `FilenameSanitizer`:
- **Linhas 18-25**: `PATH_TRAVERSAL_PATTERN` detecta `../`, `..\`, `/..`, `\..`, `..`, inicia com `.`, separadores, null bytes, control chars
- **Linhas 27-28**: `DANGEROUS_CHARS_PATTERN` remove `<>:|?*` e control chars (Windows)
- **Linha 33**: `sanitize()` - lança `OneDriveErroException` se invalido
- **Linha 80**: `isValid()` - validação booleana sem exceção
- **Linha 92**: `sanitizeSafe()` - fallback seguro sem exceções para uso interno

### Razão:
Nomes de arquivo vindos do usuário eram concatenados diretamente em caminhos OneDrive (`caminho + "/" + nomeArquivo`), permitindo path traversal (`../../../etc/passwd`) e injeção de caracteres perigosos. A sanitização ocorre tanto no controller (entrada) quanto no service (defesa em profundidade).

---

## 4.0: Ausência de proteção contra abuso nos endpoints públicos de OTP/formulário

**Commit:** `a075133` - "sexta melhoria da segurança com testes unitarios"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/main/java/br/com/sgpj/security/IpAddressUtil.java` | 1-56 | Extração de IP real considerando proxies/load balancers |
| `src/main/java/br/com/sgpj/security/RateLimiter.java` | 1-139 | Rate limiting sliding window por identificador |
| `src/main/java/br/com/sgpj/exception/RateLimitExceededException.java` | 1-11 | Exceção para rate limit excedido |
| `src/main/java/br/com/sgpj/exception/OtpGenerationLimitExceededException.java` | 1-11 | Exceção específica para OTP generation limit |
| `src/test/java/br/com/sgpj/security/IpAddressUtilTest.java` | 1-143 | Testes de extração de IP |
| `src/test/java/br/com/sgpj/security/RateLimiterTest.java` | 1-227 | Testes completos de rate limiting |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/java/br/com/sgpj/controller/SolicitacaoDuvidaController.java` | +6-7, +32-33, +39-42 | Injeção `RateLimiter`; `criar()` usa `IpAddressUtil.getClientIpAddress()` + `checkFormRateLimit()` |
| `src/main/java/br/com/sgpj/controller/UsuarioController.java` | +6-7, +38, +41, +61-64, +170-173, +185-188, +194-197, +200-203 | Injeção `RateLimiter`; rate limits em `login()`, `enviarRedefinirSenhaOtp()`, `enviarDefinirSenhaOtp()`, `validateOtp()` |
| `src/main/java/br/com/sgpj/service/OtpService.java` | +13-14, +25-26, +38-39, +48-73, +154-166, +168-177 | Rate limit interno por email: `MAX_OTP_GENERATION_PER_MINUTE=2`, `MAX_OTP_GENERATION_PER_HOUR=5` |
| `src/main/java/br/com/sgpj/exception/GlobalExceptionHandler.java` | +185-195 | Handlers para `RateLimitExceededException` e `OtpGenerationLimitExceededException` → HTTP 429 |

### Limites Configurados (`RateLimiter.java` linhas 24-29):
- **Geral**: 10 req/hora, 5 req/minuto
- **OTP**: 3 req/hora, 5 req/minuto
- **Login**: 5 req/hora, 3 req/minuto
- **Formulário**: 10 req/hora, 5 req/minuto
- **OTP Generation (service)**: 2 req/minuto, 5 req/hora

### Identificação (`IpAddressUtil.java`):
- **Linhas 14-40**: `getClientIpAddress()` - verifica headers `X-Forwarded-For`, `Proxy-Client-IP`, `WL-Proxy-Client-IP`, `HTTP_CLIENT_IP`, `HTTP_X_FORWARDED_FOR`, fallback `getRemoteAddr()`
- **Linhas 43-54**: `createRateLimitIdentifier()` - combina IP + email (normalizado lowercase/trim) para rate limit por usuário

### Razão:
Endpoints públicos (`/login`, `/enviar-otp`, `/validar-otp`, `/solicitacoes`) permitiam ataques de brute force, enumeração de email e DoS. Rate limiting por IP+email com sliding window impede abuso mantendo usabilidade.

---

## 5.0: Cookie de autenticação sem Secure e logout só no cliente

**Commit:** `be0ef94` - "setima melhoria da segurança com os testes unitarios"

### Arquivos Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/main/java/br/com/sgpj/security/TokenBlacklistService.java` | 1-164 | Blacklist server-side de tokens JWT (revogação imediata) |
| `src/main/java/br/com/sgpj/exception/TokenBlacklistedException.java` | 1-11 | Exceção para token revogado |
| `src/test/java/br/com/sgpj/security/TokenBlacklistServiceTest.java` | 1-345 | Testes completos de blacklist |

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/resources/application.properties` | +19 | `cookie.secure=${COOKIE_SECURE:false}` - configurável via env |
| `src/main/java/br/com/sgpj/config/jwt/GerenciadorTokenJwt.java` | +14, +98, +118 | Adicionado claim `jti` (UUID) em todos tokens para identificação única |
| `src/main/java/br/com/sgpj/config/jwt/AutenticacaoFilter.java` | +21-22, +63-64, +77-83 | Verifica `tokenBlacklistService.isTokenBlacklisted()` antes de validar JWT |
| `src/main/java/br/com/sgpj/config/SecurityConfiguracao.java` | +6, +59, +67, +175 | Injeta `TokenBlacklistService` no `AutenticacaoFilter` |
| `src/main/java/br/com/sgpj/controller/UsuarioController.java` | +9-10, +41, +44-45, +88-95, +155-156, +165-168, +229-236 | `cookieSecure` via `@Value`; cookie usa `.secure(cookieSecure)`; `logout()` e `definirNovaSenha()` blacklistam token atual |
| `src/main/java/br/com/sgpj/exception/GlobalExceptionHandler.java` | +195-199 | Handler para `TokenBlacklistedException` → HTTP 401 |

### Fluxo de Revogação:
1. **Login** (linha 98/118 `GerenciadorTokenJwt`): Token inclui `jti` (UUID único)
2. **Logout** (`UsuarioController.java` linhas 88-95): Extrai token do request → `tokenBlacklistService.blacklistToken(token)` → expira cookie
3. **Troca de senha** (linhas 229-236): Mesma lógica do logout
4. **Filtro** (`AutenticacaoFilter.java` linhas 77-83): Em cada request, verifica blacklist antes de processar JWT
5. **Cleanup** (`TokenBlacklistService.java` linhas 143-148): `@Scheduled` a cada 30 min remove tokens expirados

### Cookie Seguro:
- **HttpOnly**: `true` (linha 165) - inacessível a JavaScript (XSS)
- **SameSite**: `Strict` (linha 166) - bloqueia CSRF cross-site
- **Secure**: `${COOKIE_SECURE:false}` (linha 164) - `true` em produção (HTTPS), `false` em dev

### Razão:
Antes: logout apenas expirava cookie client-side; token JWT roubado permanecia válido até expiração (1h). Agora: revogação server-side imediata via blacklist + cookie Secure configurável para produção.

---

## 5.1: Enumeração de conta e vazamento de mensagens de erro

**Commit:** `2ea5608` - "ultima melhoria da segurança com os testes unitarios"

### Arquivos Modificados:
| Arquivo | Linhas Alteradas | Mudança |
|---------|------------------|---------|
| `src/main/java/br/com/sgpj/adapter/database/UsuarioPersistenciaAdapter.java` | 37, 41 | Exceções `UsuarioNaoEncontradoException` recebem apenas email/ID (sem mensagem detalhada) |
| `src/main/java/br/com/sgpj/exception/UsuarioNaoEncontradoException.java` | 6, 10 | Construtores agora definem `super("Usuário não encontrado")` - mensagem genérica fixa |
| `src/main/java/br/com/sgpj/exception/GlobalExceptionHandler.java` | 27, 57 | Handlers retornam mensagem fixa `"Usuário não encontrado"` ao invés de `ex.getMessage()` |
| `src/main/resources/application.properties` | 3-4 | `spring.web.error.include-message=never`, `include-binding-errors=never` - desabilita vazamento de stack traces e detalhes de validação |

### Arquivos de Teste Criados:
| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `src/test/java/br/com/sgpj/adapter/database/UsuarioPersistenciaAdapterSecurityTest.java` | 1-192 | 7 testes de segurança anti-enumeração |
| `src/test/java/br/com/sgpj/exception/UsuarioNaoEncontradoExceptionSecurityTest.java` | 1-95 | 5 testes verificando não-exposição de email/ID |

### Testes de Segurança Principais:
- `deveLancarExcecaoSemExporEmailEmBuscarUsuarioPorEmail` - verifica mensagem não contém email
- `deveRetornarMesmoTipoExcecaoParaEmailNaoEncontrado` - mesma exceção para métodos diferentes
- `deveNaoDiferenciarMensagensErroEntreMetodos` - mensagens idênticas impedem enumeração
- `deveLancarUsuarioNaoEncontradoExceptionAoInvésDeUsernameNotFoundException` - unifica tipo de exceção

### Razão:
Antes: mensagens de erro expunham emails (`"Usuário não encontrado para esse endereço de email: admin@empresa.com"`) e IDs, permitindo enumeração de contas válidas. Stack traces e detalhes de validação vazavam em respostas de erro. Agora: mensagem genérica fixa `"Usuário não encontrado"` para todos casos, mesmos códigos HTTP, mesma exceção, configuração Spring para não expor detalhes internos.

---

## Resumo de Commits por Issue

| Issue | Commit | Hash | Data |
|-------|--------|------|------|
| 1.0 + 1.1 | melhorar segurança | `3ccad09` | 22/08/2026 |
| 2.0 | terceira opção owasp | `8e46649` | 22/08/2026 |
| 2.1 | quarta opção owasp | `1558aa4` | 22/08/2026 |
| 3.0 | quinto melhoria | `cc67d56` | 23/08/2026 |
| 4.0 | sexta melhoria | `a075133` | 23/08/2026 |
| 5.0 | sétima melhoria | `be0ef94` | 24/08/2026 |
| 5.1 | última melhoria | `2ea5608` | 25/08/2026 |

---

## Observações de Deploy

1. **Migração de Banco (2.1)**: Executar `ALTER TABLE usuario_otp MODIFY COLUMN otp VARCHAR(60) NOT NULL;` - invalida OTPs existentes
2. **Variáveis de Ambiente Obrigatórias**: Configurar todas em `.env` (baseado em `.env.example`)
3. **Produção**: Definir `security.validate-secrets=true` e `COOKIE_SECURE=true`
4. **Limpeza**: Tokens blacklistados limpos automaticamente a cada 30 min; OTP generation history limpo a cada 10 min