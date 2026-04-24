package com.nimbachi.franquicias.controller;

import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.request.ProductCreateDTO;
import com.nimbachi.franquicias.dto.request.StockUpdateDTO;
import com.nimbachi.franquicias.dto.response.ApiResponse;
import com.nimbachi.franquicias.dto.response.ProductResponseDTO;
import com.nimbachi.franquicias.dto.response.TopProductPerBranchDTO;
import com.nimbachi.franquicias.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Endpoint 4: Agregar un nuevo producto a la sucursal
     */
    @PostMapping("/branches/{branchId}/products")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProductToBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody ProductCreateDTO createDTO) {

        ProductResponseDTO newProduct = productService.addProductToBranch(branchId, createDTO);
        ApiResponse<ProductResponseDTO> response = ApiResponse.success(newProduct,
                "Producto agregado a la sucursal exitosamente.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint 5: Eliminar un producto de una sucursal
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        // Usamos success(null) para un cuerpo vacío pero con mensaje de éxito.
        ApiResponse<Object> response = ApiResponse.success(null, "Producto eliminado exitosamente.");
        return ResponseEntity.ok(response); // Podría ser también HttpStatus.NO_CONTENT con cuerpo vacío
    }

    /**
     * Endpoint 6: Modificar un Stock de un producto
     * Usamos PATCH porque es una actualización parcial del recurso Product.
     */
    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProductStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateDTO updateDTO) {

        ProductResponseDTO updatedProduct = productService.updateStock(productId, updateDTO);
        ApiResponse<ProductResponseDTO> response = ApiResponse.success(updatedProduct,
                "Stock del producto actualizado.");
        return ResponseEntity.ok(response);
    }

    /**
     * Plus: Actualizar el nombre del producto
     */
    @PutMapping("/products/{productId}/name")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProductName(
            @PathVariable Long productId,
            @Valid @RequestBody NameUpdateDTO updateDTO) {

        ProductResponseDTO updatedProduct = productService.updateName(productId, updateDTO);
        ApiResponse<ProductResponseDTO> response = ApiResponse.success(updatedProduct,
                "Nombre del producto actualizado.");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 7: Mostrar el producto con más stock por sucursal para una
     * franquicia puntual.
     */
    @GetMapping("/franchises/{franchiseId}/products/top-stock")
    public ResponseEntity<ApiResponse<List<TopProductPerBranchDTO>>> getTopStockProducts(
            @PathVariable Long franchiseId) {

        List<TopProductPerBranchDTO> topProducts = productService.getTopStockProductsForFranchise(franchiseId);
        ApiResponse<List<TopProductPerBranchDTO>> response = ApiResponse.success(topProducts);
        return ResponseEntity.ok(response);
    }
}