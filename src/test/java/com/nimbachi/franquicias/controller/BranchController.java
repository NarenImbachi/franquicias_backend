package com.nimbachi.franquicias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbachi.franquicias.dto.request.BranchCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.BranchResponseDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.service.BranchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BranchController.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @Autowired
    private ObjectMapper objectMapper;

    private BranchCreateDTO branchCreateDTO;
    private BranchResponseDTO branchResponseDTO;
    private NameUpdateDTO nameUpdateDTO;
    private Long franchiseId;
    private Long branchId;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        branchId = 10L;

        branchCreateDTO = new BranchCreateDTO("Sucursal A");
        branchResponseDTO = new BranchResponseDTO(branchId, "Sucursal A", franchiseId);
        nameUpdateDTO = new NameUpdateDTO("Sucursal A - Renombrada");
    }

    @Test
    void addBranchToFranchise_Success() throws Exception {
        // Arrange
        when(branchService.addBranchToFranchise(eq(franchiseId), any(BranchCreateDTO.class)))
                .thenReturn(branchResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/franchises/{franchiseId}/branches", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branchCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Sucursal agregada exitosamente.")))
                .andExpect(jsonPath("$.data.id", is(branchResponseDTO.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(branchResponseDTO.getName())))
                .andExpect(jsonPath("$.data.franchiseId", is(branchResponseDTO.getFranchiseId().intValue())));
    }

    @Test
    void addBranchToFranchise_InvalidInput() throws Exception {
        // Arrange
        BranchCreateDTO invalidCreateDTO = new BranchCreateDTO(""); // Nombre vacío

        // Act & Assert
        mockMvc.perform(post("/api/franchises/{franchiseId}/branches", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("El nombre de la sucursal no puede estar vacío.")))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.path", is("/api/franchises/" + franchiseId + "/branches")));
    }

    @Test
    void addBranchToFranchise_ResourceNotFoundException_Franchise() throws Exception {
        // Arrange
        Long nonExistentFranchiseId = 99L;
        when(branchService.addBranchToFranchise(eq(nonExistentFranchiseId), any(BranchCreateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Franquicia", "id", nonExistentFranchiseId));

        // Act & Assert
        mockMvc.perform(post("/api/franchises/{franchiseId}/branches", nonExistentFranchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branchCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Franquicia no encontrada con id : '%s'", nonExistentFranchiseId))))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.path", is("/api/franchises/" + nonExistentFranchiseId + "/branches")));
    }

    @Test
    void addBranchToFranchise_DuplicateResourceException() throws Exception {
        // Arrange
        String duplicateBranchName = "Sucursal Existente";
        BranchCreateDTO duplicateCreateDTO = new BranchCreateDTO(duplicateBranchName);
        when(branchService.addBranchToFranchise(eq(franchiseId), any(BranchCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Sucursal", "nombre", duplicateBranchName));

        // Act & Assert
        mockMvc.perform(post("/api/franchises/{franchiseId}/branches", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Ya existe un recurso Sucursal con nombre : '%s'", duplicateBranchName))))
                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                .andExpect(jsonPath("$.path", is("/api/franchises/" + franchiseId + "/branches")));
    }

    @Test
    void updateBranchName_Success() throws Exception {
        // Arrange
        BranchResponseDTO updatedBranchResponse = new BranchResponseDTO(branchId, nameUpdateDTO.getNewName(), franchiseId);
        when(branchService.updateName(eq(branchId), any(NameUpdateDTO.class))).thenReturn(updatedBranchResponse);

        // Act & Assert
        mockMvc.perform(put("/api/branches/{branchId}/name", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Nombre de la sucursal actualizado.")))
                .andExpect(jsonPath("$.data.id", is(updatedBranchResponse.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(updatedBranchResponse.getName())))
                .andExpect(jsonPath("$.data.franchiseId", is(updatedBranchResponse.getFranchiseId().intValue())));
    }

    @Test
    void updateBranchName_InvalidInput() throws Exception {
        // Arrange
        NameUpdateDTO invalidNameUpdateDTO = new NameUpdateDTO(""); // Nombre vacío

        // Act & Assert
        mockMvc.perform(put("/api/branches/{branchId}/name", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidNameUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("El nuevo nombre no puede estar vacío.")))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.path", is("/api/branches/" + branchId + "/name")));
    }

    @Test
    void updateBranchName_ResourceNotFoundException_Branch() throws Exception {
        // Arrange
        Long nonExistentBranchId = 99L;
        when(branchService.updateName(eq(nonExistentBranchId), any(NameUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Sucursal", "id", nonExistentBranchId));

        // Act & Assert
        mockMvc.perform(put("/api/branches/{branchId}/name", nonExistentBranchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Sucursal no encontrada con id : '%s'", nonExistentBranchId))))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.path", is("/api/branches/" + nonExistentBranchId + "/name")));
    }

    @Test
    void updateBranchName_DuplicateResourceException() throws Exception {
        // Arrange
        String duplicateBranchName = "Otra Sucursal Existente";
        NameUpdateDTO duplicateNameUpdateDTO = new NameUpdateDTO(duplicateBranchName);
        when(branchService.updateName(eq(branchId), any(NameUpdateDTO.class)))
                .thenThrow(new DuplicateResourceException("Sucursal", "nombre", duplicateBranchName));

        // Act & Assert
        mockMvc.perform(put("/api/branches/{branchId}/name", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateNameUpdateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Ya existe un recurso Sucursal con nombre : '%s'", duplicateBranchName))))
                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                .andExpect(jsonPath("$.path", is("/api/branches/" + branchId + "/name")));
    }
}
