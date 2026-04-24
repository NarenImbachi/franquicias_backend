package com.nimbachi.franquicias.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private IBranchRepository branchRepository;

    @Mock
    private FranchiseService franchiseService; 
    @Mock
    private IBranchMapper branchMapper;

    @Mock
    private IFranchiseRepository franchiseRepository; 

    @InjectMocks
    private BranchService branchService;

    private Franchise franchise;
    private Branch branch1;
    private Branch branch2;
    private BranchCreateDTO branchCreateDTO;
    private BranchResponseDTO branchResponseDTO;
    private NameUpdateDTO nameUpdateDTO;

    @BeforeEach
    void setUp() {
        franchise = new Franchise();
        franchise.setId(1L);
        franchise.setName("Franquicia Principal");
        franchise.setBranches(new ArrayList<>());

        branch1 = new Branch();
        branch1.setId(10L);
        branch1.setName("Sucursal A");
        branch1.setFranchise(franchise);
        franchise.addBranch(branch1); // Añadir branch1 a la franquicia

        branch2 = new Branch();
        branch2.setId(11L);
        branch2.setName("Sucursal B");
        branch2.setFranchise(franchise);
        franchise.addBranch(branch2); // Añadir branch2 a la franquicia

        branchCreateDTO = new BranchCreateDTO("Nueva Sucursal");

        branchResponseDTO = new BranchResponseDTO();
        branchResponseDTO.setId(12L);
        branchResponseDTO.setName("Nueva Sucursal");
        branchResponseDTO.setFranchiseId(1L);

        nameUpdateDTO = new NameUpdateDTO("Nombre Sucursal Actualizado");
    }

    @Test
    void addBranchToFranchise_Success() {
        // Arrange
        Long franchiseId = 1L;
        Branch newBranchEntity = new Branch();
        newBranchEntity.setName(branchCreateDTO.getName());
        newBranchEntity.setFranchise(franchise);

        when(franchiseService.findFranchiseEntityById(franchiseId)).thenReturn(franchise);
        when(branchMapper.toEntity(branchCreateDTO)).thenReturn(newBranchEntity);

        when(franchiseRepository.save(franchise)).thenReturn(franchise);
        when(branchMapper.toDto(newBranchEntity)).thenReturn(branchResponseDTO);

        // Act
        BranchResponseDTO result = branchService.addBranchToFranchise(franchiseId, branchCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(branchResponseDTO.getName(), result.getName());
        assertEquals(franchiseId, result.getFranchiseId());
        verify(franchiseService, times(1)).findFranchiseEntityById(franchiseId);
        verify(branchMapper, times(1)).toEntity(branchCreateDTO);
        verify(franchiseRepository, times(1)).save(franchise);
        verify(branchMapper, times(1)).toDto(newBranchEntity);
        assertTrue(franchise.getBranches().contains(newBranchEntity));
        assertEquals(franchise, newBranchEntity.getFranchise());
    }

    @Test
    void addBranchToFranchise_ThrowsDuplicateResourceException() {
        // Arrange
        Long franchiseId = 1L;
        BranchCreateDTO duplicateBranchDTO = new BranchCreateDTO("Sucursal A");

        when(franchiseService.findFranchiseEntityById(franchiseId)).thenReturn(franchise);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> branchService.addBranchToFranchise(franchiseId, duplicateBranchDTO));

        verify(franchiseService, times(1)).findFranchiseEntityById(franchiseId);
        verify(branchMapper, never()).toEntity(any(BranchCreateDTO.class));
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(branchMapper, never()).toDto(any(Branch.class));
    }

    @Test
    void addBranchToFranchise_FranchiseNotFound() {
        // Arrange
        Long franchiseId = 99L; // ID que no existe
        when(franchiseService.findFranchiseEntityById(franchiseId))
                .thenThrow(new ResourceNotFoundException("Franquicia", "id", franchiseId));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> branchService.addBranchToFranchise(franchiseId, branchCreateDTO));
        verify(franchiseService, times(1)).findFranchiseEntityById(franchiseId);
        verify(branchMapper, never()).toEntity(any(BranchCreateDTO.class));
        verify(franchiseRepository, never()).save(any(Franchise.class));
    }

    @Test
    void findBranchEntityById_Success() {
        // Arrange
        Long branchId = 10L;
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch1));

        // Act
        Branch result = branchService.findBranchEntityById(branchId);

        // Assert
        assertNotNull(result);
        assertEquals(branch1.getId(), result.getId());
        assertEquals(branch1.getName(), result.getName());
        verify(branchRepository, times(1)).findById(branchId);
    }

    @Test
    void findBranchEntityById_ThrowsResourceNotFoundException() {
        // Arrange
        Long branchId = 99L;
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> branchService.findBranchEntityById(branchId));
        verify(branchRepository, times(1)).findById(branchId);
    }

    @Test
    void updateName_Success() {
        // Arrange
        Long branchId = 10L;
        NameUpdateDTO updateDTO = new NameUpdateDTO("Sucursal A - Renombrada");

        Franchise franchiseWithBranches = new Franchise(1L, "Franquicia Principal",
                new ArrayList<>(Arrays.asList(branch1, branch2)));
        branch1.setFranchise(franchiseWithBranches);
        branch2.setFranchise(franchiseWithBranches);

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch1));
        when(branchRepository.save(branch1)).thenReturn(branch1);

        BranchResponseDTO expectedResponse = new BranchResponseDTO(branchId, updateDTO.getNewName(), franchise.getId());
        when(branchMapper.toDto(branch1)).thenReturn(expectedResponse);

        // Act
        BranchResponseDTO result = branchService.updateName(branchId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.getNewName(), result.getName());
        verify(branchRepository, times(1)).findById(branchId);
        verify(branchRepository, times(1)).save(branch1);
        verify(branchMapper, times(1)).toDto(branch1);
        assertEquals(updateDTO.getNewName(), branch1.getName()); // Verify name was updated on entity
    }

    @Test
    void updateName_ThrowsResourceNotFoundException() {
        // Arrange
        Long branchId = 99L;
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> branchService.updateName(branchId, nameUpdateDTO));
        verify(branchRepository, times(1)).findById(branchId);
        verify(branchRepository, never()).save(any(Branch.class));
        verify(branchMapper, never()).toDto(any(Branch.class));
    }

    @Test
    void updateName_ThrowsDuplicateResourceException() {
        // Arrange
        Long branchId = 10L;
        NameUpdateDTO duplicateNameDTO = new NameUpdateDTO("Sucursal B");

        Franchise franchiseWithBranches = new Franchise(1L, "Franquicia Principal",
                new ArrayList<>(Arrays.asList(branch1, branch2)));
        branch1.setFranchise(franchiseWithBranches);
        branch2.setFranchise(franchiseWithBranches);

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch1));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> branchService.updateName(branchId, duplicateNameDTO));
        verify(branchRepository, times(1)).findById(branchId);
        verify(branchRepository, never()).save(any(Branch.class));
        verify(branchMapper, never()).toDto(any(Branch.class));
    }

    @Test
    void updateName_NameNotChanged_NoDuplicateCheckNeeded() {
        // Arrange
        Long branchId = 10L;
        NameUpdateDTO sameNameDTO = new NameUpdateDTO("Sucursal A"); // El mismo nombre que ya tiene

        Franchise franchiseWithBranches = new Franchise(1L, "Franquicia Principal",
                new ArrayList<>(Arrays.asList(branch1, branch2)));
        branch1.setFranchise(franchiseWithBranches);
        branch2.setFranchise(franchiseWithBranches);

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch1));
        when(branchRepository.save(branch1)).thenReturn(branch1);

        BranchResponseDTO expectedResponse = new BranchResponseDTO(branchId, sameNameDTO.getNewName(),
                franchise.getId());
        when(branchMapper.toDto(branch1)).thenReturn(expectedResponse);

        // Act
        BranchResponseDTO result = branchService.updateName(branchId, sameNameDTO);

        // Assert
        assertNotNull(result);
        assertEquals(sameNameDTO.getNewName(), result.getName());
        verify(branchRepository, times(1)).findById(branchId);
        verify(branchRepository, times(1)).save(branch1); // save is called because updateName is always called, even if
                                                          // value is same.
        verify(branchMapper, times(1)).toDto(branch1);
    }
}