package br.com.sgpj.service.useCases.cargo;

public final class ConstantesValidation {

    public ConstantesValidation() {}

    public static final Long CARGO_ADMIN_ID = 1L;
    public static final Long SEM_CARGO_ID = 2L;
    public static final String CARGO_ADMIN_NOME = "ADMIN";
    public static final String SEM_CARGO_NOME = "Sem Cargo";
    public static final Long TODAS_PERMISSOES_ID = 18L;
    public static final Long SEM_PERMISSOES_ID = 19L;
    public static final int TOTAL_PERMISSOES_NORMAIS = 17;

    public static boolean isNomeCargoReservado(String nome) {
        return nome != null && (nome.trim().equalsIgnoreCase(CARGO_ADMIN_NOME)
                || nome.trim().equalsIgnoreCase(SEM_CARGO_NOME));
    }

}
