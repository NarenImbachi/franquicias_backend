package com.nimbachi.franquicias.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchCreateDTO {

    @NotBlank(message = "El nombre de la sucursal no puede estar vacío.")
    private String name;
}
