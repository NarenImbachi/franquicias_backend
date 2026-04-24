package com.nimbachi.franquicias.service;

import com.nimbachi.franquicias.dto.request.BranchCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.BranchResponseDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.mapper.IBranchMapper;
import com.nimbachi.franquicias.model.Branch;
import com.nimbachi.franquicias.model.Franchise;
import com.nimbachi.franquicias.repository.IBranchRepository;
import com.nimbachi.franquicias.repository.IFranchiseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final IBranchRepository branchRepository;
    private final FranchiseService franchiseService;
    private final IBranchMapper branchMapper;
    private final IFranchiseRepository franchiseRepository;

    /**
     * Endpoint 3: Agregar una nueva sucursal a la franquicia.
     */
    @Transactional
    public BranchResponseDTO addBranchToFranchise(Long franchiseId, BranchCreateDTO createDTO) {
        Franchise franchise = franchiseService.findFranchiseEntityById(franchiseId);

        boolean nameExistsInFranchise = franchise.getBranches().stream()
                .anyMatch(branch -> branch.getName().equalsIgnoreCase(createDTO.getName()));

        if (nameExistsInFranchise)
            throw new DuplicateResourceException("Sucursal", "nombre", createDTO.getName());

        Branch newBranch = branchMapper.toEntity(createDTO);
        franchise.addBranch(newBranch);

        franchiseRepository.save(franchise);

        return branchMapper.toDto(newBranch);
    }

    public Branch findBranchEntityById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));
    }

    /**
     * Plus: Actualizar el nombre de la sucursal.
     */
    @Transactional
    public BranchResponseDTO updateName(Long branchId, NameUpdateDTO updateDTO) {
        Branch branch = findBranchEntityById(branchId);

        // Lógica de aplicación: verificar duplicados dentro de la misma franquicia.
        branch.getFranchise().getBranches().stream()
                .filter(b -> !b.getId().equals(branchId)) // Excluimos la sucursal actual de la verificación
                .filter(b -> b.getName().equalsIgnoreCase(updateDTO.getNewName()))
                .findAny().ifPresent(b -> {
                    throw new DuplicateResourceException("Sucursal", "nombre", updateDTO.getNewName());
                });

        branch.updateName(updateDTO.getNewName());

        Branch updatedBranch = branchRepository.save(branch);
        return branchMapper.toDto(updatedBranch);
    }
}