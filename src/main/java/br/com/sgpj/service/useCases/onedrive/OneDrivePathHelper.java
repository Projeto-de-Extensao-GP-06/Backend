package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.security.FilenameSanitizer;

public final class OneDrivePathHelper {

    public static final long LIMITE_UPLOAD_SIMPLES = 4L * 1024 * 1024;
    public static final long TAMANHO_FATIA = 5L * 1024 * 1024;
    public static final String CAMINHO_PADRAO = "root:/sgpj/usuarios";

    private OneDrivePathHelper() {
    }

    public static String sanitizar(String valor) {
        return FilenameSanitizer.sanitizeSafe(valor);
    }

    public static String gerarNomePasta(Cliente cliente) {
        String nome = cliente.getNomeRazaoSocial()
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");

        return sanitizar(nome);
    }

    public static String caminhoRaiz() {
        return CAMINHO_PADRAO;
    }

    public static String caminhoDaPasta(String caminhoBase) {
        return CAMINHO_PADRAO + "/" + caminhoBase + ":";
    }

    public static String caminhoDaPastaFilha(String caminhoBase, String nomePasta) {
        return CAMINHO_PADRAO + "/" + caminhoBase + "/" + nomePasta + ":";
    }

    public static String caminhoDoArquivo(String caminhoBase, String nomeArquivo) {
        return CAMINHO_PADRAO + "/" + caminhoBase + "/" + nomeArquivo + ":";
    }

    public static String extrairPastaPai(String caminhoCompleto) {
        if (caminhoCompleto == null || !caminhoCompleto.contains("/")) {
            return null;
        }
        return caminhoCompleto.substring(0, caminhoCompleto.indexOf('/'));
    }

    public static String caminhoCompletoDaPasta(String nomeBase, String numeroCnj) {
        return nomeBase + "/" + numeroCnj;
    }
}
