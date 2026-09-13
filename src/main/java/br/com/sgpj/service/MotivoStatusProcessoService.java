package br.com.sgpj.service;

import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MotivoStatusProcessoService {

    private final MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    public MotivoStatusProcessoService(MotivoStatusProcessoRepository motivoStatusProcessoRepository) {
        this.motivoStatusProcessoRepository = motivoStatusProcessoRepository;
    }

    @Transactional(readOnly = true)
    public List<MotivoStatusProcesso> listarTodos() {
        return motivoStatusProcessoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MotivoStatusProcesso> listarPorAplicabilidade(MotivoStatusProcesso.AplicavelA aplicavelA) {
        return motivoStatusProcessoRepository.findByAplicavelA(aplicavelA);
    }
}
