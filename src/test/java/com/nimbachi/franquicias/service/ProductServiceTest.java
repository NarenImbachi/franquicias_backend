package com.nimbachi.franquicias.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.request.ProductCreateDTO;
import com.nimbachi.franquicias.dto.request.StockUpdateDTO;
import com.nimbachi.franquicias.dto.response.ProductResponseDTO;
import com.nimbachi.franquicias.dto.response.TopProductPerBranchDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.mapper.IproductMapper;
import com.nimbachi.franquicias.model.Branch;
import com.nimbachi.franquicias.model.Franchise;
import com.nimbachi.franquicias.model.Product;
import com.nimbachi.franquicias.repository.IBranchRepository;
import com.nimbachi.franquicias.repository.IProductRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private BranchService branchService;

    @Mock
    private FranchiseService franchiseService;

    @Mock
    private IproductMapper productMapper;

    @Mock
    private IBranchRepository branchRepository;

    @InjectMocks
    private ProductService productService;

    private Franchise franchise;
    private Branch branch1;
    private Branch branch2;
    private Product product1_branch1;
    private Product product2_branch1;
    private Product product1_branch2;
    private ProductCreateDTO productCreateDTO;
    private ProductResponseDTO productResponseDTO;
    private StockUpdateDTO stockUpdateDTO;
    private NameUpdateDTO nameUpdateDTO;

    @BeforeEach
    void setUp() {
        franchise = new Franchise();
        franchise.setId(1L);
        franchise.setName("Franquicia Test");

        branch1 = new Branch();
        branch1.setId(10L);
        branch1.setName("Sucursal A");
        branch1.setFranchise(franchise);
        branch1.setProducts(new ArrayList<>());

        branch2 = new Branch();
        branch2.setId(11L);
        branch2.setName("Sucursal B");
        branch2.setFranchise(franchise);
        branch2.setProducts(new ArrayList<>());

        franchise.setBranches(Arrays.asList(branch1, branch2));

        product1_branch1 = new Product();
        product1_branch1.setId(100L);
        product1_branch1.setName("Producto X");
        product1_branch1.setStock(50);
        product1_branch1.setBranch(branch1);
        branch1.addProduct(product1_branch1);

        product2_branch1 = new Product();
        product2_branch1.setId(101L);
        product2_branch1.setName("Producto Y");
        product2_branch1.setStock(75);
        product2_branch1.setBranch(branch1);
        branch1.addProduct(product2_branch1);

        product1_branch2 = new Product();
        product1_branch2.setId(102L);
        product1_branch2.setName("Producto Z");
        product1_branch2.setStock(120);
        product1_branch2.setBranch(branch2);
        branch2.addProduct(product1_branch2);

        productCreateDTO = new ProductCreateDTO("Nuevo Producto", 100);

        productResponseDTO = new ProductResponseDTO(103L, "Nuevo Producto", 100, branch1.getId());

        stockUpdateDTO = new StockUpdateDTO(150);

        nameUpdateDTO = new NameUpdateDTO("Producto Renombrado");
    }

    @Test
    void addProductToBranch_Success() {
        Long branchId = branch1.getId();
        Product newProductEntity = new Product();
        newProductEntity.setName(productCreateDTO.getName());
        newProductEntity.setStock(productCreateDTO.getStock());
        newProductEntity.setBranch(branch1);

        when(branchService.findBranchEntityById(branchId)).thenReturn(branch1);
        when(productMapper.toEntity(productCreateDTO)).thenReturn(newProductEntity);
        when(branchRepository.save(branch1)).thenReturn(branch1);
        when(productMapper.toDto(newProductEntity)).thenReturn(productResponseDTO);

        ProductResponseDTO result = productService.addProductToBranch(branchId, productCreateDTO);

        assertNotNull(result);
        assertEquals(productResponseDTO.getName(), result.getName());
        assertEquals(productResponseDTO.getStock(), result.getStock());
        assertEquals(productResponseDTO.getBranchId(), result.getBranchId());
        verify(branchService, times(1)).findBranchEntityById(branchId);
        verify(productMapper, times(1)).toEntity(productCreateDTO);
        verify(branchRepository, times(1)).save(branch1);
        verify(productMapper, times(1)).toDto(newProductEntity);
        assertTrue(branch1.getProducts().contains(newProductEntity));
        assertEquals(branch1, newProductEntity.getBranch());
    }

    @Test
    void addProductToBranch_ThrowsDuplicateResourceException() {
        Long branchId = branch1.getId();
        ProductCreateDTO duplicateProductDTO = new ProductCreateDTO("Producto X", 200); // Nombre duplicado

        when(branchService.findBranchEntityById(branchId)).thenReturn(branch1);

        assertThrows(DuplicateResourceException.class,
                () -> productService.addProductToBranch(branchId, duplicateProductDTO));

        verify(branchService, times(1)).findBranchEntityById(branchId);
        verify(productMapper, never()).toEntity(any(ProductCreateDTO.class));
        verify(branchRepository, never()).save(any(Branch.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void addProductToBranch_BranchNotFound() {
        Long nonExistentBranchId = 99L;
        when(branchService.findBranchEntityById(nonExistentBranchId))
                .thenThrow(new ResourceNotFoundException("Sucursal", "id", nonExistentBranchId));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.addProductToBranch(nonExistentBranchId, productCreateDTO));

        verify(branchService, times(1)).findBranchEntityById(nonExistentBranchId);
        verify(productMapper, never()).toEntity(any(ProductCreateDTO.class));
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void deleteProduct_Success() {
        Long productId = product1_branch1.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product1_branch1));
        doNothing().when(productRepository).delete(product1_branch1);

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(product1_branch1);
    }

    @Test
    void deleteProduct_ThrowsResourceNotFoundException() {
        Long nonExistentProductId = 999L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(nonExistentProductId));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void updateStock_Success() {
        Long productId = product1_branch1.getId();
        ProductResponseDTO updatedResponse = new ProductResponseDTO(productId, product1_branch1.getName(),
                stockUpdateDTO.getNewStock(), product1_branch1.getBranch().getId());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product1_branch1));
        when(productRepository.save(product1_branch1)).thenReturn(product1_branch1);
        when(productMapper.toDto(product1_branch1)).thenReturn(updatedResponse);

        ProductResponseDTO result = productService.updateStock(productId, stockUpdateDTO);

        assertNotNull(result);
        assertEquals(stockUpdateDTO.getNewStock(), result.getStock());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product1_branch1);
        verify(productMapper, times(1)).toDto(product1_branch1);
        assertEquals(stockUpdateDTO.getNewStock(), product1_branch1.getStock());
    }

    @Test
    void updateStock_ThrowsResourceNotFoundException() {
        Long nonExistentProductId = 999L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateStock(nonExistentProductId, stockUpdateDTO));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void updateStock_ThrowsIllegalArgumentException_NegativeStock() {
        Long productId = product1_branch1.getId();
        StockUpdateDTO invalidStockDTO = new StockUpdateDTO(-5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product1_branch1));

        assertThrows(IllegalArgumentException.class, () -> productService.updateStock(productId, invalidStockDTO));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void getTopStockProductsForFranchise_Success() {
        Long franchiseId = franchise.getId();
        List<Product> topProducts = Arrays.asList(product2_branch1, product1_branch2); // Asumiendo estos son los top

        // Configurar los productos para que sus sucursales y franquicias estén
        // enlazadas
        product2_branch1.setBranch(branch1);
        branch1.setFranchise(franchise);

        product1_branch2.setBranch(branch2);
        branch2.setFranchise(franchise);

        when(franchiseService.findFranchiseEntityById(franchiseId)).thenReturn(franchise);
        when(productRepository.findTopStockProductsPerBranchForFranchise(franchiseId)).thenReturn(topProducts);

        List<TopProductPerBranchDTO> result = productService.getTopStockProductsForFranchise(franchiseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(product2_branch1.getName(), result.get(0).getProductName());
        assertEquals(product1_branch2.getName(), result.get(1).getProductName());
        assertEquals(branch1.getName(), result.get(0).getBranchName());
        assertEquals(branch2.getName(), result.get(1).getBranchName());
        verify(franchiseService, times(1)).findFranchiseEntityById(franchiseId);
        verify(productRepository, times(1)).findTopStockProductsPerBranchForFranchise(franchiseId);
    }

    @Test
    void getTopStockProductsForFranchise_FranchiseNotFound() {
        Long nonExistentFranchiseId = 99L;
        when(franchiseService.findFranchiseEntityById(nonExistentFranchiseId))
                .thenThrow(new ResourceNotFoundException("Franquicia", "id", nonExistentFranchiseId));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getTopStockProductsForFranchise(nonExistentFranchiseId));

        verify(franchiseService, times(1)).findFranchiseEntityById(nonExistentFranchiseId);
        verify(productRepository, never()).findTopStockProductsPerBranchForFranchise(anyLong());
    }

    @Test
    void findProductEntityById_Success() {
        Long productId = product1_branch1.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product1_branch1));

        Product result = productService.findProductEntityById(productId);

        assertNotNull(result);
        assertEquals(product1_branch1.getId(), result.getId());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void findProductEntityById_ThrowsResourceNotFoundException() {
        Long nonExistentProductId = 999L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findProductEntityById(nonExistentProductId));

        verify(productRepository, times(1)).findById(nonExistentProductId);
    }

    @Test
    void updateName_Success() {
        Long productId = product1_branch1.getId();
        NameUpdateDTO newNameDTO = new NameUpdateDTO("Producto X Renombrado");
        ProductResponseDTO updatedResponse = new ProductResponseDTO(productId, newNameDTO.getNewName(),
                product1_branch1.getStock(), branch1.getId());

        Product productToUpdate = new Product();
        productToUpdate.setId(productId);
        productToUpdate.setName("Producto X");
        productToUpdate.setStock(50);
        productToUpdate.setBranch(branch1);
        branch1.setProducts(new ArrayList<>(Arrays.asList(productToUpdate, product2_branch1))); // Ensure products are
                                                                                                // setup for checking

        when(productRepository.findById(productId)).thenReturn(Optional.of(productToUpdate));
        when(productRepository.save(productToUpdate)).thenReturn(productToUpdate);
        when(productMapper.toDto(productToUpdate)).thenReturn(updatedResponse);

        ProductResponseDTO result = productService.updateName(productId, newNameDTO);

        assertNotNull(result);
        assertEquals(newNameDTO.getNewName(), result.getName());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(productToUpdate);
        verify(productMapper, times(1)).toDto(productToUpdate);
        assertEquals(newNameDTO.getNewName(), productToUpdate.getName());
    }

    @Test
    void updateName_ThrowsResourceNotFoundException() {
        Long nonExistentProductId = 999L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateName(nonExistentProductId, nameUpdateDTO));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void updateName_ThrowsDuplicateResourceException() {
        Long productId = product1_branch1.getId();
        NameUpdateDTO duplicateNameDTO = new NameUpdateDTO("Producto Y"); // Nombre de otro producto en la misma
                                                                          // sucursal

        Product productToUpdate = new Product();
        productToUpdate.setId(productId);
        productToUpdate.setName("Producto X");
        productToUpdate.setStock(50);
        productToUpdate.setBranch(branch1);
        branch1.setProducts(new ArrayList<>(Arrays.asList(productToUpdate, product2_branch1))); // Ensure products are
                                                                                                // setup for checking

        when(productRepository.findById(productId)).thenReturn(Optional.of(productToUpdate));

        assertThrows(DuplicateResourceException.class, () -> productService.updateName(productId, duplicateNameDTO));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void updateName_ThrowsIllegalArgumentException_EmptyName() {
        Long productId = product1_branch1.getId();
        NameUpdateDTO invalidNameDTO = new NameUpdateDTO("");

        Product productToUpdate = new Product();
        productToUpdate.setId(productId);
        productToUpdate.setName("Producto X");
        productToUpdate.setStock(50);
        productToUpdate.setBranch(branch1);
        branch1.setProducts(new ArrayList<>(Arrays.asList(productToUpdate, product2_branch1)));

        when(productRepository.findById(productId)).thenReturn(Optional.of(productToUpdate));

        assertThrows(IllegalArgumentException.class, () -> productService.updateName(productId, invalidNameDTO));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toDto(any(Product.class));
    }
}