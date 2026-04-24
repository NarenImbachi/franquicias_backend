package com.nimbachi.franquicias.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbachi.franquicias.dto.request.FranchiseCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.FranchiseResponseDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.mapper.IFranchiseMapper;
import com.nimbachi.franquicias.model.Franchise;
import com.nimbachi.franquicias.repository.IFranchiseRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private IFranchiseRepository franchiseRepository;

    @Mock
    private IFranchiseMapper franchiseMapper;

    @InjectMocks
    private FranchiseService franchiseService;

    private Franchise franchise;
    private FranchiseCreateDTO franchiseCreateDTO;
    private FranchiseResponseDTO franchiseResponseDTO;
    private NameUpdateDTO nameUpdateDTO;

    @BeforeEach
    void setUp() {
        franchise = new Franchise();
        franchise.setId(1L);
        franchise.setName("Franquicia Test");

        franchiseCreateDTO = new FranchiseCreateDTO();
        franchiseCreateDTO.setName("Franquicia Test");

        franchiseResponseDTO = new FranchiseResponseDTO();
        franchiseResponseDTO.setId(1L);
        franchiseResponseDTO.setName("Franquicia Test");

        nameUpdateDTO = new NameUpdateDTO("Nuevo Nombre Franquicia");
    }

    @Test
    void createFranchise_Success() {
        // Arrange
        when(franchiseRepository.findByName(franchiseCreateDTO.getName())).thenReturn(Optional.empty());
        when(franchiseMapper.toEntity(franchiseCreateDTO)).thenReturn(franchise);
        when(franchiseRepository.save(franchise)).thenReturn(franchise);
        when(franchiseMapper.toDto(franchise)).thenReturn(franchiseResponseDTO);

        // Act
        FranchiseResponseDTO result = franchiseService.create(franchiseCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(franchiseResponseDTO.getName(), result.getName());
        verify(franchiseRepository, times(1)).findByName(franchiseCreateDTO.getName());
        verify(franchiseMapper, times(1)).toEntity(franchiseCreateDTO);
        verify(franchiseRepository, times(1)).save(franchise);
        verify(franchiseMapper, times(1)).toDto(franchise);
    }

    @Test
    void createFranchise_ThrowsDuplicateResourceException() {
        // Arrange
        when(franchiseRepository.findByName(franchiseCreateDTO.getName())).thenReturn(Optional.of(franchise));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> franchiseService.create(franchiseCreateDTO));
        verify(franchiseRepository, times(1)).findByName(franchiseCreateDTO.getName());
        verify(franchiseMapper, never()).toEntity(any(FranchiseCreateDTO.class));
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(franchiseMapper, never()).toDto(any(Franchise.class));
    }

    @Test
    void findFranchiseEntityById_Success() {
        // Arrange
        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));

        // Act
        Franchise result = franchiseService.findFranchiseEntityById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(franchise.getId(), result.getId());
        assertEquals(franchise.getName(), result.getName());
        verify(franchiseRepository, times(1)).findById(1L);
    }

    @Test
    void findFranchiseEntityById_ThrowsResourceNotFoundException() {
        // Arrange
        when(franchiseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> franchiseService.findFranchiseEntityById(1L));
        verify(franchiseRepository, times(1)).findById(1L);
    }

    @Test
    void updateName_Success() {
        // Arrange
        Franchise existingFranchise = new Franchise(1L, "Nombre Antiguo", null); // Usar constructor de Franchise
        Franchise updatedFranchise = new Franchise(1L, nameUpdateDTO.getNewName(), null);
        FranchiseResponseDTO expectedResponse = new FranchiseResponseDTO(1L, nameUpdateDTO.getNewName());

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(existingFranchise));
        when(franchiseRepository.findByName(nameUpdateDTO.getNewName())).thenReturn(Optional.empty());
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(updatedFranchise);
        when(franchiseMapper.toDto(updatedFranchise)).thenReturn(expectedResponse);

        // Act
        FranchiseResponseDTO result = franchiseService.updateName(1L, nameUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(franchiseRepository, times(1)).findById(1L);
        verify(franchiseRepository, times(1)).findByName(nameUpdateDTO.getNewName());
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(franchiseMapper, times(1)).toDto(updatedFranchise);
        assertEquals(nameUpdateDTO.getNewName(), existingFranchise.getName()); // Verify name was updated on entity
    }

    @Test
    void updateName_ThrowsResourceNotFoundException() {
        // Arrange
        when(franchiseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> franchiseService.updateName(1L, nameUpdateDTO));
        verify(franchiseRepository, times(1)).findById(1L);
        verify(franchiseRepository, never()).findByName(anyString());
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(franchiseMapper, never()).toDto(any(Franchise.class));
    }

    @Test
    void updateName_ThrowsDuplicateResourceException() {
        // Arrange
        Franchise existingFranchiseInDB = new Franchise(2L, "Nuevo Nombre Franquicia", null); // Otra franquicia con el mismo nombre
        Franchise franchiseToUpdate = new Franchise(1L, "Nombre Antiguo", null);

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchiseToUpdate));
        when(franchiseRepository.findByName(nameUpdateDTO.getNewName())).thenReturn(Optional.of(existingFranchiseInDB));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> franchiseService.updateName(1L, nameUpdateDTO));
        verify(franchiseRepository, times(1)).findById(1L);
        verify(franchiseRepository, times(1)).findByName(nameUpdateDTO.getNewName());
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(franchiseMapper, never()).toDto(any(Franchise.class));
    }

    @Test
    void updateName_NameNotChanged_NoDuplicateCheck() {
        // Arrange
        NameUpdateDTO sameNameUpdateDTO = new NameUpdateDTO("Franquicia Test");
        Franchise franchiseWithSameName = new Franchise(1L, "Franquicia Test", null);
        FranchiseResponseDTO expectedResponse = new FranchiseResponseDTO(1L, "Franquicia Test");

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchiseWithSameName));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(franchiseWithSameName);
        when(franchiseMapper.toDto(franchiseWithSameName)).thenReturn(expectedResponse);

        // Act
        FranchiseResponseDTO result = franchiseService.updateName(1L, sameNameUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(franchiseRepository, times(1)).findById(1L);
        verify(franchiseRepository, never()).findByName(anyString()); // Should not call findByName
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(franchiseMapper, times(1)).toDto(franchiseWithSameName);
    }
}