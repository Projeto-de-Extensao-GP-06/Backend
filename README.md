# SGPJ Backend

# Padrão de Commits e Fluxo de Desenvolvimento

Este documento define o padrão de commits e o fluxo de trabalho do projeto.
O objetivo é manter o código organizado, padronizado e fácil de entender por qualquer pessoa do time.

---

# Padrão de Commits (Conventional Commits)

Os commits seguem um padrão semântico, ou seja, cada mensagem indica claramente o que foi feito.

## Tipos de commit

* `feat` → Adiciona uma nova funcionalidade
* `fix` → Corrige um bug
* `docs` → Alterações na documentação
* `test` → Alterações/criação de testes
* `build` → Mudanças em build ou dependências
* `perf` → Melhorias de performance
* `style` → Formatação de código (lint, espaçamento, etc)
* `refactor` → Refatoração sem alterar comportamento
* `chore` → Configurações e tarefas gerais
* `ci` → Integração contínua
* `raw` → Arquivos de configuração/dados
* `cleanup` → Limpeza de código desnecessário
* `remove` → Remoção de código ou arquivos

## Exemplos

```bash
feat: adiciona logo na tela inicial
fix: corrige erro ao salvar usuário
docs: atualiza README
```

---

# Fluxo de Desenvolvimento com Branches

## Conceito

Neste projeto, nunca trabalhamos diretamente na branch `development`.
Toda alteração deve ser feita em uma branch separada.

Isso permite:

* Trabalhar em paralelo com outras pessoas
* Evitar conflitos
* Garantir revisão de código antes de integrar

---

## Passo a passo

### 1. Criar uma nova branch

Sempre comece garantindo que sua `development` está atualizada:

```bash
git checkout development
git pull origin development
git checkout -b feat/nome-da-feature
```

Exemplos de nome:

```bash
feat/adiciona-logo
fix/corrige-login
```

---

### 2. Desenvolver

Faça suas alterações normalmente e commit seguindo o padrão:

```bash
git add .
git commit -m "feat: adiciona logo na tela inicial"
```

---

### 3. Atualizar sua branch

Antes de subir sua branch, sincronize com a `development`:

```bash
git add .
git stash
git pull origin development
git stash apply
```

---

### 4. Subir a branch

```bash
git add .
git commit -m "feat: ajustes finais"
git push origin feat/nome-da-feature
```

---

### 5. Abrir Pull Request (PR)

No GitHub:

* Acesse o repositório
* Clique em **"Compare & pull request"**
* Configure:

    * Base: `development`
    * Compare: sua branch
* Crie o PR

---

### 6. Code Review

* Adicione 2 revisores
* Aguarde aprovação
* Faça ajustes se necessário

---

### 7. Merge

Após aprovação:

* O PR será mergeado na `development`
* Sua branch pode ser removida

---

## ⚠️ Boas práticas

* Nunca commitar direto na `development`
* Usar nomes claros nas branches
* Fazer commits pequenos e objetivos
* Sempre seguir o padrão de commit
* Atualizar sua branch antes de abrir PR

---

## Resumo

* Cada tarefa → uma branch
* Desenvolve → commit → push
* Abre PR → revisão → merge

---

## Exemplo de como criar um arquivo application.properties.example
```text
spring.application.name=sgpj

spring.web.error.include-message=never
spring.web.error.include-binding-errors=never

# Security configuration
# Set to true in production to enforce secret validation
security.validate-secrets=false

spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/${DB_NAME:sgpj}
spring.datasource.username=${DB_USERNAME:app_sgpj}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.show-sql=true

jwt.validity=${JWT_VALIDITY:3600}
jwt.refresh.validity=${JWT_REFRESH_VALIDITY:86400}
jwt.secret=${JWT_SECRET}
cookie.secure=${COOKIE_SECURE:false}


# Configurações para autenticação com Microsoft (Graph API)
microsoft.client-id=${CLIENT_ID}
microsoft.client-secret=${CLIENT_SECRET}
microsoft.tenant-id=${TENANT_ID:common}
microsoft.redirect-uri=${REDIRECT_URI:http://localhost:8080/login/oauth2/code/azure}

spring.servlet.multipart.max-file-size=250MB
spring.servlet.multipart.max-request-size=260MB

# Configurações para API DataJud
datajud.api-key=${DATAJUD_API_KEY}

# CONFIGURAÇÃO DE DESENVOLVIMENTO (Usando mailpit)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.username.from=nao-responda@sgpj.com.br

# CONFIGURAÇÃO DE PRODUÇÃO
#spring.mail.host=smtp.gmail.com
#spring.mail.port=587
#spring.mail.username=sistema.sgpj@gmail.com
#spring.mail.password=zkkx yxzy njgw pwpc
#spring.mail.properties.mail.smtp.auth=true
#spring.mail.properties.mail.smtp.starttls.enable=true
#spring.mail.properties.mail.smtp.starttls.required=true
#spring.mail.properties.mail.smtp.connectiontimeout=5000
#spring.mail.properties.mail.smtp.timeout=5000
#spring.mail.properties.mail.smtp.writetimeout=5000
#spring.mail.username.from=sistema.sgpj@gmail.com

#Configuracao OTLP
otel.service.name=sgpj-backend
otel.exporter.otlp.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:http://observability:4318}
otel.exporter.otlp.protocol=http/protobuf
otel.traces.exporter=otlp
otel.metrics.exporter=none
otel.logs.exporter=none

```

---

## Fluxo de requisicao

### Login

```
POST /usuarios/login
  Body: { "email": "...", "senha": "..." }

  1. AutenticacaoFilter → sem token, passa adiante
  2. UsuarioController   → chama UsuarioService.autenticar()
  3. AutenticacaoProvider → valida email e senha via BCrypt no banco
  4. GerenciadorTokenJwt  → gera token JWT assinado
  5. Resposta:
     - Header Set-Cookie: authToken=<jwt>; HttpOnly; SameSite=Strict
     - Body: { "userId": 1, "nome": "...", "email": "..." }
```

### Requisicao autenticada

```
GET /usuarios
  Cookie: authToken=<jwt>   (ou Authorization: Bearer <jwt>)

  1. AutenticacaoFilter   → extrai token do cookie ou header
  2. GerenciadorTokenJwt  → valida assinatura e expiração
  3. AutenticacaoService  → carrega usuario do banco pelo email
  4. SecurityContext      → usuario autenticado registrado
  5. UsuarioController    → executa e retorna lista de usuarios
```

## Endpoints úteis

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

---
