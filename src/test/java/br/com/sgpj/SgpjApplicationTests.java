package br.com.sgpj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de fumaça (smoke test) que sobe o contexto completo do Spring Boot,
 * validando que todos os beans, repositórios JPA, filtros de segurança e
 * configurações da aplicação estão corretamente conectados após a refatoração.
 *
 * <p><b>Atenção:</b> este projeto não possui um perfil de teste com banco em memória
 * (H2) configurado — o datasource aponta para MySQL (ver application.properties).
 * Para este teste subir com sucesso, é necessário ter uma instância MySQL acessível
 * com as variáveis de ambiente DB_HOST/DB_NAME/DB_USERNAME/DB_PASSWORD (ou os valores
 * padrão de desenvolvimento) configuradas, da mesma forma que para rodar a aplicação
 * localmente. Caso o time deseje rodar esse teste sem dependência externa, será
 * necessário adicionar um driver H2 e um application-test.properties com um perfil
 * dedicado — o que está fora do escopo desta reativação de teste.</p>
 */
@SpringBootTest
class SgpjApplicationTests {

    @Test
    @DisplayName("O contexto da aplicação deve subir corretamente")
    void contextLoads() {
        // Sucesso implícito: se o contexto do Spring não subir (beans mal configurados,
        // dependências faltando, erro de configuração de segurança, etc.), o teste falha
        // automaticamente durante o carregamento do ApplicationContext.
    }
}
