package br.com.sgpj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class SgpjApplicationTests {

    @Test
    @DisplayName("O contexto da aplicação deve subir corretamente")
    void contextLoads() {
    }
}
