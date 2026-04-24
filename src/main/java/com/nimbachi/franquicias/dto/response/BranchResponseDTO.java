package com.nimbachi.franquicias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponseDTO {

    private Long id;
    private String name;
    private Long franchiseId;
}