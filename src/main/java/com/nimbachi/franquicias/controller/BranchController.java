package com.nimbachi.franquicias.controller;

import com.nimbachi.franquicias.dto.request.BranchCreateDTO;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.response.ApiResponse;
import com.nimbachi.franquicias.dto.response.BranchResponseDTO;
import com.nimbachi.franquicias.service.BranchService;
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
@RequestMapping("/api") 
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    /**
     * Endpoint 3: Agregar una nueva sucursal a la franquicia
     */
    @PostMapping("/franchises/{franchiseId}/branches")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> addBranchToFranchise(
            @PathVariable Long franchiseId,
            @Valid @RequestBody BranchCreateDTO createDTO) {

        BranchResponseDTO newBranch = branchService.addBranchToFranchise(franchiseId, createDTO);
        ApiResponse<BranchResponseDTO> response = ApiResponse.success(newBranch, "Sucursal agregada exitosamente.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Plus: Actualizar el nombre de la sucursal
     */
    @PutMapping("/branches/{branchId}/name")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> updateBranchName(
            @PathVariable Long branchId,
            @Valid @RequestBody NameUpdateDTO updateDTO) {

        BranchResponseDTO updatedBranch = branchService.updateName(branchId, updateDTO);
        ApiResponse<BranchResponseDTO> response = ApiResponse.success(updatedBranch,
                "Nombre de la sucursal actualizado.");
        return ResponseEntity.ok(response);
    }
}