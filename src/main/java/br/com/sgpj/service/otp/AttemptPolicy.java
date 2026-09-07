package br.com.sgpj.service.otp;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
import org.springframework.stereotype.Component;

/**
 * Política que controla as tentativas de validação de OTP.
 * Mantém a regra de negócio separada do modelo de persistência.
 */
@Component
public class AttemptPolicy {

    private final int maxTentativas;

    public AttemptPolicy() {
        this.maxTentativas = 3; // default, pode ser parametrizado se necessário
    }

    public AttemptPolicy(int maxTentativas) {
        this.maxTentativas = maxTentativas;
    }

    /**
     * Registra uma tentativa incorreta e lança exceção se o limite for excedido.
     */
    public void registerFailedAttempt(Otp otp) {
        otp.registrarTentativaIncorreta();
        if (otp.excedeuLimiteDeTentativas(maxTentativas)) {
            throw new LimiteTentativasExcedidoException("Número máximo de tentativas alcançado. Solicite um novo código.");
        }
    }
}
