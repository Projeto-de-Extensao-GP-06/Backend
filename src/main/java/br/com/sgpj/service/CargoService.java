package br.com.sgpj.service;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.request.RenomearCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.cargo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CargoService {


    private final CreateCargo createCargo;
    private final ReadCargo readCargo;
    private final UpdateCargo updateCargo;
    private final DeleteCargo deleteCargo;


    public CargoService(CreateCargo createCargo, ReadCargo readCargo, UpdateCargo updateCargo, DeleteCargo deleteCargo) {
        this.createCargo = createCargo;
        this.readCargo = readCargo;
        this.updateCargo = updateCargo;
        this.deleteCargo = deleteCargo;
    }


    // CREATE OPERATIONS
    @Transactional
    public CargoResponse criar(CriarCargoRequest request, String emailUsuario) {
        return createCargo.criarCargo(request, emailUsuario);
    }

    //READ OPERATIONS
    @Transactional(readOnly = true)
    public Page<CargoResponse> listarPaginado(Pageable pageable) {
        return readCargo.listarPaginado(pageable);
    }

    //UPDATE OPERATIONS
    @Transactional
    public CargoResponse atualizarPermissoes(Long cargoId, AtribuirPermissoesRequest request, String emailUsuario) {
        return updateCargo.atualizarPermissoesCargo(cargoId, request, emailUsuario);
    }

    @Transactional
    public CargoResponse renomear(Long cargoId, RenomearCargoRequest request) {
        return updateCargo.renomearCargo(cargoId, request);
    }

    //DELETE OPERATIONS
    @Transactional
    public void deletar(Long cargoId) {
        deleteCargo.deletarCargo(cargoId);
    }
}
