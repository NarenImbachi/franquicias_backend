package com.nimbachi.franquicias.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameUpdateDTO {
    
    @NotBlank(message = "El nuevo nombre no puede estar vacío.")
    private String newName;
}