package com.nimbachi.franquicias.controller;

import com.nimbachi.franquicias.dto.request.FranchiseCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.ApiResponse;
import com.nimbachi.franquicias.dto.response.FranchiseResponseDTO;
import com.nimbachi.franquicias.service.FranchiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/franchises")
public class FranchiseController {

    private final FranchiseService franchiseService;

    public FranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    // Endpoint 2: Agregar una nueva franquicia
    @PostMapping
    public ResponseEntity<ApiResponse<FranchiseResponseDTO>> createFranchise(
            @Valid @RequestBody FranchiseCreateDTO createDTO) {
        
        FranchiseResponseDTO newFranchise = franchiseService.create(createDTO);
        ApiResponse<FranchiseResponseDTO> response = ApiResponse.success(newFranchise, "Franquicia creada exitosamente.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // Plus: Actualizar el nombre de la franquicia
    @PutMapping("/{franchiseId}/name")
    public ResponseEntity<ApiResponse<FranchiseResponseDTO>> updateFranchiseName(
            @PathVariable Long franchiseId,
            @Valid @RequestBody NameUpdateDTO updateDTO) {
            
        FranchiseResponseDTO updatedFranchise = franchiseService.updateName(franchiseId, updateDTO);
        ApiResponse<FranchiseResponseDTO> response = ApiResponse.success(updatedFranchise, "Nombre de la franquicia actualizado.");
        return ResponseEntity.ok(response);
    }
}