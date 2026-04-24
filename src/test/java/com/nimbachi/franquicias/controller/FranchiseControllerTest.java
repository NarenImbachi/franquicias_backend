package com.nimbachi.franquicias.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbachi.franquicias.dto.request.FranchiseCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.FranchiseResponseDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.service.FranchiseService;

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

@WebMvcTest(FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FranchiseService franchiseService;

    @Autowired
    private ObjectMapper objectMapper;

    private FranchiseCreateDTO franchiseCreateDTO;
    private FranchiseResponseDTO franchiseResponseDTO;
    private NameUpdateDTO nameUpdateDTO;

    @BeforeEach
    void setUp() {
        franchiseCreateDTO = new FranchiseCreateDTO("Nueva Franquicia");
        franchiseResponseDTO = new FranchiseResponseDTO(1L, "Nueva Franquicia");
        nameUpdateDTO = new NameUpdateDTO("Franquicia Actualizada");
    }

    @Test
    void createFranchise_Success() throws Exception {
        // Arrange
        when(franchiseService.create(any(FranchiseCreateDTO.class))).thenReturn(franchiseResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(franchiseCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Franquicia creada exitosamente.")))
                .andExpect(jsonPath("$.data.id", is(franchiseResponseDTO.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(franchiseResponseDTO.getName())));
    }

    @Test
    void createFranchise_InvalidInput() throws Exception {
        // Arrange
        FranchiseCreateDTO invalidCreateDTO = new FranchiseCreateDTO(""); // Nombre vacío
        
        // No necesitamos mockear el servicio porque la validación ocurre antes.

        // Act & Assert
        mockMvc.perform(post("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("El nombre de la franquicia no puede estar vacío."))) // Ajuste aquí
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR"))) // Ajuste aquí
                .andExpect(jsonPath("$.path", is("/api/franchises"))); // Ajuste aquí
    }

    @Test
    void createFranchise_DuplicateResourceException() throws Exception {
        // Arrange
        when(franchiseService.create(any(FranchiseCreateDTO.class)))
                .thenThrow(new DuplicateResourceException("Franquicia", "nombre", franchiseCreateDTO.getName()));

        // Act & Assert
        mockMvc.perform(post("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(franchiseCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Ya existe un recurso Franquicia con nombre : '%s'", franchiseCreateDTO.getName()))))
                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                .andExpect(jsonPath("$.path", is("/api/franchises")));
    }

    @Test
    void updateFranchiseName_Success() throws Exception {
        // Arrange
        Long franchiseId = 1L;
        FranchiseResponseDTO updatedFranchiseResponse = new FranchiseResponseDTO(franchiseId, nameUpdateDTO.getNewName());
        when(franchiseService.updateName(eq(franchiseId), any(NameUpdateDTO.class))).thenReturn(updatedFranchiseResponse);

        // Act & Assert
        mockMvc.perform(put("/api/franchises/{franchiseId}/name", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Nombre de la franquicia actualizado.")))
                .andExpect(jsonPath("$.data.id", is(updatedFranchiseResponse.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(updatedFranchiseResponse.getName())));
    }

    @Test
    void updateFranchiseName_InvalidInput() throws Exception {
        // Arrange
        Long franchiseId = 1L;
        NameUpdateDTO invalidNameUpdateDTO = new NameUpdateDTO(""); // Nombre vacío

        // Act & Assert
        mockMvc.perform(put("/api/franchises/{franchiseId}/name", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidNameUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("El nuevo nombre no puede estar vacío."))) // Ajuste aquí
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR"))) // Ajuste aquí
                .andExpect(jsonPath("$.path", is("/api/franchises/" + franchiseId + "/name"))); // Ajuste aquí
    }

    @Test
    void updateFranchiseName_ResourceNotFoundException() throws Exception {
        // Arrange
        Long franchiseId = 99L; // ID que no existe
        when(franchiseService.updateName(eq(franchiseId), any(NameUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Franquicia", "id", franchiseId));

        // Act & Assert
        mockMvc.perform(put("/api/franchises/{franchiseId}/name", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Franquicia no encontrada con id : '%s'", franchiseId))))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.path", is("/api/franchises/" + franchiseId + "/name")));
    }

    @Test
    void updateFranchiseName_DuplicateResourceException() throws Exception {
        // Arrange
        Long franchiseId = 1L;
        String duplicateName = "Franquicia Existente";
        NameUpdateDTO duplicateNameUpdateDTO = new NameUpdateDTO(duplicateName);
        when(franchiseService.updateName(eq(franchiseId), any(NameUpdateDTO.class)))
                .thenThrow(new DuplicateResourceException("Franquicia", "nombre", duplicateName));

        // Act & Assert
        mockMvc.perform(put("/api/franchises/{franchiseId}/name", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateNameUpdateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(String.format("Ya existe un recurso Franquicia con nombre : '%s'", duplicateName))))
                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                .andExpect(jsonPath("$.path", is("/api/franchises/" + franchiseId + "/name")));
    }
}