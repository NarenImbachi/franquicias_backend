package com.nimbachi.franquicias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductPerBranchDTO {

    private Long productId;
    private String productName;
    private int stock;
    private Long branchId;
    private String branchName;
}