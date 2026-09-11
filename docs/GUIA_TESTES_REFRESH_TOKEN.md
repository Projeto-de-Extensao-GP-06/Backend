# 🧪 Guia de Testes — Autenticação e Refresh Token

Este guia orienta como testar todos os fluxos da funcionalidade de **Refresh Token** implementada no backend do SGPJ, tanto de forma **automatizada** quanto **manual** (via Postman, Insomnia, cURL ou Navegador).

---

## 📋 Sumário de Cenários de Teste

| # | Cenário | Objetivo | Resultado Esperado |
|---|---|---|---|
| **01** | **Login com Sucesso** | Obter `authToken` e `refreshToken` nos cookies | Status `200 OK`, `Set-Cookie` com ambos os tokens |
| **02** | **Acesso Autenticado** | Consumir endpoint protegido | Status `200 OK` |
| **03** | **Rotação de Refresh Token** | Renovar tokens expirados | Status `200 OK`, novos cookies emitidos |
| **04** | **Detecção de Reuso de Token** | Tentar reutilizar token já rotacionado | Status `401 Unauthorized`, revoga todas as sessões |
| **05** | **Bloqueio de Usuário Inativo** | Tentar renovar token de usuário inativado | Status `401 Unauthorized` |
| **06** | **Logout com Limpeza de Cookies** | Encerrar sessão e invalidar token no banco | Status `204 No Content`, cookies zerados (`Max-Age=0`) |
| **07** | **Invalidação na Troca de Senha** | Alterar senha e revogar sessões ativas | Status `200 OK`, tokens do banco deletados |

---

## ⚙️ 1. Testes Automatizados (Maven / JUnit 5)

Para executar toda a suíte de testes unitários de autenticação e tokens:

```powershell
.\mvnw.cmd test "-Dtest=UsuarioControllerTest,UsuarioServiceTest,RefreshTokenServiceTest,UsuarioRefreshTokenTest"
```

Para rodar **todos** os testes do projeto:
```powershell
.\mvnw.cmd test
```

---

## 🌐 2. Testes Manuais Passo a Passo

### Cenário 1: Login com Sucesso
* **Método:** `POST`
* **URL:** `http://localhost:8080/usuarios/login`
* **Headers:** `Content-Type: application/json`
* **Body:**
  ```json
  {
    "email": "admin@sgpj.com.br",
    "senha": "senhaValida@123"
  }
  ```
* **O que verificar:**
    * Status de resposta: `200 OK`.
    * Headers de resposta:
        * `Set-Cookie: authToken=<JWT>; Path=/; HttpOnly; SameSite=Strict`
        * `Set-Cookie: refreshToken=<UUID>; Path=/usuarios; HttpOnly; SameSite=Strict`
    * No banco de dados (`usuario_refresh_token`):
        * Nova linha criada com `revogado = 0`, `expira_em` futura e `token` armazenando o **hash SHA-256** (64 caracteres hexadecimais).

---

### Cenário 2: Consumir Rota Protegida com o JWT
* **Método:** `GET`
* **URL:** `http://localhost:8080/usuarios`
* **Cookies enviados:** `authToken=<JWT>`
* **O que verificar:**
    * Status: `200 OK` e lista de usuários retornada.

---

### Cenário 3: Rotação Bem-Sucedida (*Refresh Token Rotation*)
* **Método:** `POST`
* **URL:** `http://localhost:8080/usuarios/refresh-token`
* **Cookies enviados:** `refreshToken=<UUID_DO_LOGIN>`
* **O que verificar:**
    * Status de resposta: `200 OK`.
    * Headers de resposta contêm novos cookies com tokens atualizados:
        * Novo `authToken`
        * Novo `refreshToken`
    * No banco de dados:
        * O hash do token anterior passa para `revogado = 1`.
        * Uma nova linha é inserida para o novo token com `revogado = 0`.

---

### Cenário 4: Detecção de Reuso de Token (*Token Theft Detection*)
* **Método:** `POST`
* **URL:** `http://localhost:8080/usuarios/refresh-token`
* **Cookies enviados:** Reenvie o **mesmo** `refreshToken=<UUID_DO_LOGIN>` que já foi rotacionado no Cenário 3.
* **O que verificar:**
    * Status de resposta: `401 Unauthorized` (`"Tentativa de reutilização de refresh token revogado."`).
    * No banco de dados:
        * **Todas as sessões e tokens daquele usuário são imediatamente deletados** da tabela `usuario_refresh_token` por segurança.

---

### Cenário 5: Tentativa de Rotação por Usuário Inativo
1. Inative o usuário no banco de dados (`UPDATE usuario SET ativo = 0 WHERE email = '...';`).
2. Tente chamar `POST /usuarios/refresh-token` com um cookie de refresh token válido.
* **O que verificar:**
    * Status de resposta: `401 Unauthorized` (`"Este usuário está inativo."`).
    * O token é marcado como revogado e nenhum novo token é emitido.

---

### Cenário 6: Logout
* **Método:** `POST`
* **URL:** `http://localhost:8080/usuarios/logout`
* **Cookies enviados:** `refreshToken=<UUID>`
* **O que verificar:**
    * Status de resposta: `204 No Content`.
    * Headers de resposta:
        * `Set-Cookie: authToken=; Max-Age=0; Path=/`
        * `Set-Cookie: refreshToken=; Max-Age=0; Path=/usuarios`
    * No banco de dados: o token é marcado como `revogado = 1`.

---

### Cenário 7: Invalidação de Sessões na Troca de Senha
* **Método:** `POST`
* **URL:** `http://localhost:8080/usuarios/definir-nova-senha`
* **Headers:** `Authorization: Bearer <JWT_SET_PASSWORD>`
* **Body:**
  ```json
  {
    "senha": "novaSenhaForte@2026"
  }
  ```
* **O que verificar:**
    * Status de resposta: `200 OK`.
    * No banco de dados: todos os registros do usuário na tabela `usuario_refresh_token` são deletados.
    * Os cookies de sessão são expirados na resposta HTTP.

---

## 🔒 3. Boas Práticas de Verificação de Segurança (OWASP)

Ao inspecionar o tráfego de rede (DevTools / Network Tab):
1. **Proteção XSS:** Verifique se ambos os cookies possuem a flag **`HttpOnly`** assinalada.
2. **Proteção CSRF:** Verifique se o atributo **`SameSite`** está configurado como **`Strict`**.
3. **Escopo do Path:** Verifique se o `refreshToken` está restrito a **`Path=/usuarios`**.