package com.nimbachi.franquicias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nimbachi.franquicias.model.Branch;

@Repository
public interface IBranchRepository extends JpaRepository<Branch, Long>{
    
}
