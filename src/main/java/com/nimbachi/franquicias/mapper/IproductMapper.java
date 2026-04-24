package com.nimbachi.franquicias.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nimbachi.franquicias.dto.request.ProductCreateDTO;
import com.nimbachi.franquicias.dto.response.ProductResponseDTO;
import com.nimbachi.franquicias.model.Product;

@Mapper(componentModel = "spring") 
public interface IproductMapper {
    @Mapping(source = "branch.id", target = "branchId")
    ProductResponseDTO toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branch", ignore = true)
    Product toEntity(ProductCreateDTO dto);
}
