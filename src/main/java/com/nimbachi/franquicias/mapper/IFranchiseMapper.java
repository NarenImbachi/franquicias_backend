package com.nimbachi.franquicias.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nimbachi.franquicias.dto.request.FranchiseCreateDTO;
import com.nimbachi.franquicias.dto.response.FranchiseResponseDTO;
import com.nimbachi.franquicias.model.Franchise;

@Mapper(componentModel = "spring") 
public interface IFranchiseMapper {

    FranchiseResponseDTO toDto(Franchise franchise);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branches", ignore = true)
    Franchise toEntity(FranchiseCreateDTO dto);
}
