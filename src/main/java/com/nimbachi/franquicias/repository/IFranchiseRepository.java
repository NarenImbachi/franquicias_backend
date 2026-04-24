package com.nimbachi.franquicias.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nimbachi.franquicias.model.Franchise;

@Repository
public interface IFranchiseRepository extends JpaRepository<Franchise, Long>{

    Optional<Franchise> findByName(String name);
    
}
