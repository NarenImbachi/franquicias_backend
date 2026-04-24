package com.nimbachi.franquicias.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class FranchiseCreateDTO {

    @NotBlank(message = "El nombre de la franquicia no puede estar vacío.")
    private String name;
}