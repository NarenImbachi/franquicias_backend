package com.nimbachi.franquicias.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.request.ProductCreateDTO;
import com.nimbachi.franquicias.dto.request.StockUpdateDTO;
import com.nimbachi.franquicias.dto.response.ProductResponseDTO;
import com.nimbachi.franquicias.dto.response.TopProductPerBranchDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.mapper.IproductMapper;
import com.nimbachi.franquicias.model.Branch;
import com.nimbachi.franquicias.model.Product;
import com.nimbachi.franquicias.repository.IBranchRepository;
import com.nimbachi.franquicias.repository.IProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final IProductRepository productRepository;
    private final BranchService branchService;
    private final FranchiseService franchiseService;
    private final IproductMapper productMapper;
    private final IBranchRepository branchRepository;

    /**
     * Endpoint 4: Agregar un nuevo producto a la sucursal.
     */
    @Transactional
    public ProductResponseDTO addProductToBranch(Long branchId, ProductCreateDTO createDTO) {
        Branch branch = branchService.findBranchEntityById(branchId);

        branch.getProducts().stream()
                .filter(p -> p.getName().equalsIgnoreCase(createDTO.getName()))
                .findAny().ifPresent(p -> {
                    throw new DuplicateResourceException("Producto", "nombre", createDTO.getName());
                });

        Product newProduct = productMapper.toEntity(createDTO);

        branch.addProduct(newProduct);

        branchRepository.save(branch);
        return productMapper.toDto(newProduct);
    }

    /**
     * Endpoint 5: Eliminar un producto de una sucursal.
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductEntityById(productId);
        productRepository.delete(product);
    }

    /**
     * Endpoint 6: Modificar un Stock de un producto.
     */
    @Transactional
    public ProductResponseDTO updateStock(Long productId, StockUpdateDTO updateDTO) {
        Product product = findProductEntityById(productId);

        product.updateStock(updateDTO.getNewStock());

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    /**
     * Endpoint 7: Mostrar cual es el producto que más stock tiene por sucursal para
     * una franquicia.
     */
    @Transactional(readOnly = true)
    public List<TopProductPerBranchDTO> getTopStockProductsForFranchise(Long franchiseId) {
        franchiseService.findFranchiseEntityById(franchiseId);

        List<Product> topProducts = productRepository.findTopStockProductsPerBranchForFranchise(franchiseId);

        // TODO: ver si puedo hacer el mapeo con mapstruct mas adelante
        return topProducts.stream()
                .map(product -> new TopProductPerBranchDTO(
                        product.getId(),
                        product.getName(),
                        product.getStock(),
                        product.getBranch().getId(),
                        product.getBranch().getName()))
                .collect(Collectors.toList());
    }

    public Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }

    @Transactional
    public ProductResponseDTO updateName(Long productId, NameUpdateDTO updateDTO) {
        Product product = findProductEntityById(productId);

        // Lógica de aplicación: verificar que el nuevo nombre no exista ya en la misma
        // sucursal
        product.getBranch().getProducts().stream()
                .filter(p -> !p.getId().equals(productId)) // Excluir el producto actual
                .filter(p -> p.getName().equalsIgnoreCase(updateDTO.getNewName()))
                .findAny().ifPresent(p -> {
                    throw new DuplicateResourceException("Producto", "nombre", updateDTO.getNewName());
                });

        product.updateName(updateDTO.getNewName());

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }
}
