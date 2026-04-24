package com.nimbachi.franquicias.service;

import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.request.FranchiseCreateDTO;
import com.nimbachi.franquicias.dto.response.FranchiseResponseDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.mapper.IFranchiseMapper;
import com.nimbachi.franquicias.model.Franchise;
import com.nimbachi.franquicias.repository.IFranchiseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseService {

    private final IFranchiseRepository franchiseRepository;
    private final IFranchiseMapper franchiseMapper;

    /**
     * Endpoint 2: Agregar una nueva franquicia.
     */
    @Transactional
    public FranchiseResponseDTO create(FranchiseCreateDTO createDTO) {
        // Lógica de aplicación: verificar duplicados antes de crear.
        franchiseRepository.findByName(createDTO.getName()).ifPresent(f -> {
            throw new DuplicateResourceException("Franquicia", "nombre", createDTO.getName());
        });

        Franchise franchise = franchiseMapper.toEntity(createDTO);
        Franchise savedFranchise = franchiseRepository.save(franchise);
        return franchiseMapper.toDto(savedFranchise);
    }

    public Franchise findFranchiseEntityById(Long id) {
        return franchiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franquicia", "id", id));
    }

    /**
     * Plus: Actualizar el nombre de la franquicia.
     */
    @Transactional
    public FranchiseResponseDTO updateName(Long id, NameUpdateDTO updateDTO) {
        Franchise franchise = findFranchiseEntityById(id);

        if (!franchise.getName().equalsIgnoreCase(updateDTO.getNewName())) {
            franchiseRepository.findByName(updateDTO.getNewName()).ifPresent(f -> {
                throw new DuplicateResourceException("Franquicia", "nombre", updateDTO.getNewName());
            });
        }

        franchise.updateName(updateDTO.getNewName());

        Franchise updatedFranchise = franchiseRepository.save(franchise);
        return franchiseMapper.toDto(updatedFranchise);
    }
}
