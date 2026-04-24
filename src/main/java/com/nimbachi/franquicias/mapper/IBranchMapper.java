package com.nimbachi.franquicias.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nimbachi.franquicias.dto.request.BranchCreateDTO;
import com.nimbachi.franquicias.dto.response.BranchResponseDTO;
import com.nimbachi.franquicias.model.Branch;

@Mapper(componentModel = "spring") 
public interface IBranchMapper {
    @Mapping(source = "franchise.id", target = "franchiseId")
    BranchResponseDTO toDto(Branch branch);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "franchise", ignore = true)
    @Mapping(target = "products", ignore = true)
    Branch toEntity(BranchCreateDTO dto);
}
