package com.nimbachi.franquicias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nimbachi.franquicias.model.Product;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    /**
     * Endpoint 7: Encuentra el producto con más stock por cada sucursal de una franquicia dada.
     */
    @Query(value = """
        SELECT p.* FROM (
            SELECT *, ROW_NUMBER() OVER (PARTITION BY branch_id ORDER BY stock DESC) as rn
            FROM products
        ) p
        JOIN branches b ON p.branch_id = b.id
        WHERE b.franchise_id = :franchiseId AND p.rn = 1
        """, nativeQuery = true)
    List<Product> findTopStockProductsPerBranchForFranchise(@Param("franchiseId") Long franchiseId);
}