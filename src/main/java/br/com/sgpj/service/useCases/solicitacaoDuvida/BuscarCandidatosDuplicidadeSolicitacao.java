package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.MotivoDuplicidadeEnum;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BuscarCandidatosDuplicidadeSolicitacao {

    private static final int LIMITE_MAXIMO_CANDIDATOS = 10;

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;

    public BuscarCandidatosDuplicidadeSolicitacao(ProcessoAppRepository processoAppRepository,
                                                ClienteRepository clienteRepository,
                                                ContatoRepository contatoRepository) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
    }

    public List<SolicitacaoCandidatoDuplicidadeResponse> buscarPorSolicitacaoId(Long id) {
        ProcessoApp processoApp = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));
        return buscarCandidatos(processoApp);
    }

    public List<SolicitacaoCandidatoDuplicidadeResponse> buscarCandidatos(ProcessoApp processoApp) {
        if (processoApp == null || processoApp.getCliente() == null) {
            return Collections.emptyList();
        }

        Cliente clienteSolicitacao = processoApp.getCliente();
        Long clienteIdSolicitacao = clienteSolicitacao.getId();
        boolean isPF = Boolean.TRUE.equals(clienteSolicitacao.getPF());

        Map<Long, CandidatoAccumulator> candidatosMap = new LinkedHashMap<>();

        if (!isPF) {
            buscarDuplicidadePj(processoApp, clienteIdSolicitacao, candidatosMap);
        } else {
            buscarDuplicidadePf(clienteSolicitacao, clienteIdSolicitacao, candidatosMap);
        }

        return candidatosMap.values().stream()
                .sorted((c1, c2) -> Integer.compare(c2.motivos.size(), c1.motivos.size()))
                .limit(LIMITE_MAXIMO_CANDIDATOS)
                .map(this::toResponse)
                .toList();
    }

    private void buscarDuplicidadePj(ProcessoApp processoApp,
                                     Long clienteIdSolicitacao,
                                     Map<Long, CandidatoAccumulator> candidatosMap) {
        String cnpj = processoApp.getCnpjInformadoFormulario();
        if (cnpj == null || cnpj.isBlank()) {
            return;
        }

        String cnpjNormalizado = cnpj.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
        List<Cliente> clientesPorCnpj = clienteRepository.findCandidatosPorCpfCnpj(cnpjNormalizado, clienteIdSolicitacao);

        for (Cliente cliente : clientesPorCnpj) {
            CandidatoAccumulator acc = candidatosMap.computeIfAbsent(cliente.getId(), id -> new CandidatoAccumulator(cliente));
            acc.motivos.add(MotivoDuplicidadeEnum.CNPJ);
        }
    }

    private void buscarDuplicidadePf(Cliente clienteSolicitacao,
                                     Long clienteIdSolicitacao,
                                     Map<Long, CandidatoAccumulator> candidatosMap) {
        Contato contatoPrincipal = extrairContatoPrincipal(clienteSolicitacao);

        // 1. E-mail
        if (contatoPrincipal != null && contatoPrincipal.getEmail() != null && !contatoPrincipal.getEmail().isBlank()) {
            String emailNormalizado = contatoPrincipal.getEmail().trim().toLowerCase();
            List<Contato> contatosEmail = contatoRepository.findContatosPorEmail(emailNormalizado, clienteIdSolicitacao);
            for (Contato c : contatosEmail) {
                if (c.getCliente() != null) {
                    CandidatoAccumulator acc = candidatosMap.computeIfAbsent(c.getCliente().getId(), id -> new CandidatoAccumulator(c.getCliente()));
                    acc.motivos.add(MotivoDuplicidadeEnum.EMAIL);
                    if (c.getEmail() != null && !c.getEmail().isBlank()) {
                        acc.contatosCorrespondentes.add(c.getEmail().trim());
                    }
                }
            }
        }

        // 2. Telefone
        if (contatoPrincipal != null && contatoPrincipal.getTelefone() != null && !contatoPrincipal.getTelefone().isBlank()) {
            String telefoneDigitos = contatoPrincipal.getTelefone().replaceAll("\\D", "");
            if (!telefoneDigitos.isBlank()) {
                List<Contato> contatosTelefone = contatoRepository.findContatosPorTelefoneNormalizado(telefoneDigitos, clienteIdSolicitacao);
                for (Contato c : contatosTelefone) {
                    if (c.getCliente() != null) {
                        CandidatoAccumulator acc = candidatosMap.computeIfAbsent(c.getCliente().getId(), id -> new CandidatoAccumulator(c.getCliente()));
                        acc.motivos.add(MotivoDuplicidadeEnum.TELEFONE);
                        if (c.getTelefone() != null && !c.getTelefone().isBlank()) {
                            acc.contatosCorrespondentes.add(c.getTelefone().trim());
                        }
                    }
                }
            }
        }

        // 3. Nome
        if (clienteSolicitacao.getNomeRazaoSocial() != null && !clienteSolicitacao.getNomeRazaoSocial().isBlank()) {
            String nome = clienteSolicitacao.getNomeRazaoSocial().trim();
            List<Cliente> clientesNome = clienteRepository.findCandidatosPorNome(nome, clienteIdSolicitacao);
            for (Cliente c : clientesNome) {
                CandidatoAccumulator acc = candidatosMap.computeIfAbsent(c.getId(), id -> new CandidatoAccumulator(c));
                acc.motivos.add(MotivoDuplicidadeEnum.NOME);
            }
        }
    }

    private Contato extrairContatoPrincipal(Cliente cliente) {
        if (cliente.getContatos() == null || cliente.getContatos().isEmpty()) {
            return null;
        }

        return cliente.getContatos().stream()
                .filter(contato -> Boolean.TRUE.equals(contato.getPrincipal()))
                .findFirst()
                .orElse(cliente.getContatos().getFirst());
    }

    private SolicitacaoCandidatoDuplicidadeResponse toResponse(CandidatoAccumulator acc) {
        Cliente c = acc.cliente;
        String statusNome = c.getStatusCliente() != null ? c.getStatusCliente().getNome() : null;

        return new SolicitacaoCandidatoDuplicidadeResponse(
                c.getId(),
                c.getNomeRazaoSocial(),
                c.getPF(),
                statusNome,
                c.getDataHoraEnvioFormulario(),
                new ArrayList<>(acc.motivos),
                new ArrayList<>(acc.contatosCorrespondentes)
        );
    }

    private static class CandidatoAccumulator {
        private final Cliente cliente;
        private final Set<MotivoDuplicidadeEnum> motivos = new LinkedHashSet<>();
        private final Set<String> contatosCorrespondentes = new LinkedHashSet<>();

        public CandidatoAccumulator(Cliente cliente) {
            this.cliente = cliente;
        }
    }
}
