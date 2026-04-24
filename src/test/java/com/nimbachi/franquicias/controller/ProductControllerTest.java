package com.nimbachi.franquicias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbachi.franquicias.dto.request.NameUpdateDTO;
import com.nimbachi.franquicias.dto.request.ProductCreateDTO;
import com.nimbachi.franquicias.dto.request.StockUpdateDTO;
import com.nimbachi.franquicias.dto.response.ProductResponseDTO;
import com.nimbachi.franquicias.dto.response.TopProductPerBranchDTO;
import com.nimbachi.franquicias.exception.DuplicateResourceException;
import com.nimbachi.franquicias.exception.ResourceNotFoundException;
import com.nimbachi.franquicias.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ProductService productService;

        @Autowired
        private ObjectMapper objectMapper;

        private Long branchId;
        private Long productId;
        private Long franchiseId;
        private ProductCreateDTO productCreateDTO;
        private ProductResponseDTO productResponseDTO;
        private StockUpdateDTO stockUpdateDTO;
        private NameUpdateDTO nameUpdateDTO;
        private TopProductPerBranchDTO topProduct1;
        private TopProductPerBranchDTO topProduct2;

        @BeforeEach
        void setUp() {
                branchId = 10L;
                productId = 100L;
                franchiseId = 1L;

                productCreateDTO = new ProductCreateDTO("Laptop", 10);
                productResponseDTO = new ProductResponseDTO(productId, "Laptop", 10, branchId);
                stockUpdateDTO = new StockUpdateDTO(25);
                nameUpdateDTO = new NameUpdateDTO("Laptop Pro");

                topProduct1 = new TopProductPerBranchDTO(100L, "Producto A", 50, 10L, "Sucursal X");
                topProduct2 = new TopProductPerBranchDTO(101L, "Producto B", 70, 11L, "Sucursal Y");
        }

        @Test
        void addProductToBranch_Success() throws Exception {
                when(productService.addProductToBranch(eq(branchId), any(ProductCreateDTO.class)))
                                .thenReturn(productResponseDTO);

                mockMvc.perform(post("/api/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productCreateDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Producto agregado a la sucursal exitosamente.")))
                                .andExpect(jsonPath("$.data.id", is(productResponseDTO.getId().intValue())))
                                .andExpect(jsonPath("$.data.name", is(productResponseDTO.getName())))
                                .andExpect(jsonPath("$.data.stock", is(productResponseDTO.getStock())))
                                .andExpect(jsonPath("$.data.branchId",
                                                is(productResponseDTO.getBranchId().intValue())));
        }

        @Test
        void addProductToBranch_InvalidInput() throws Exception {
                ProductCreateDTO invalidCreateDTO = new ProductCreateDTO("", -5);

                mockMvc.perform(post("/api/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidCreateDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message").isNotEmpty())
                                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                                .andExpect(jsonPath("$.path", is("/api/branches/" + branchId + "/products")));
        }

        @Test
        void addProductToBranch_ResourceNotFoundException_Branch() throws Exception {
                Long nonExistentBranchId = 99L;
                when(productService.addProductToBranch(eq(nonExistentBranchId), any(ProductCreateDTO.class)))
                                .thenThrow(new ResourceNotFoundException("Sucursal", "id", nonExistentBranchId));

                mockMvc.perform(post("/api/branches/{branchId}/products", nonExistentBranchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productCreateDTO)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Sucursal no encontrada con id : '%s'",
                                                                nonExistentBranchId))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                                .andExpect(jsonPath("$.path",
                                                is("/api/branches/" + nonExistentBranchId + "/products")));
        }

        @Test
        void addProductToBranch_DuplicateResourceException() throws Exception {
                String duplicateProductName = "Existing Product";
                ProductCreateDTO duplicateCreateDTO = new ProductCreateDTO(duplicateProductName, 10);
                when(productService.addProductToBranch(eq(branchId), any(ProductCreateDTO.class)))
                                .thenThrow(new DuplicateResourceException("Producto", "nombre", duplicateProductName));

                mockMvc.perform(post("/api/branches/{branchId}/products", branchId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateCreateDTO)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Ya existe un recurso Producto con nombre : '%s'",
                                                                duplicateProductName))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                                .andExpect(jsonPath("$.path", is("/api/branches/" + branchId + "/products")));
        }

        @Test
        void deleteProduct_Success() throws Exception {
                doNothing().when(productService).deleteProduct(productId);

                mockMvc.perform(delete("/api/products/{productId}", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Producto eliminado exitosamente.")))
                                .andExpect(jsonPath("$.data").doesNotExist()); // <--- CAMBIO AQUÍ: espera que $.data NO
                                                                               // exista
        }

        @Test
        void deleteProduct_ResourceNotFoundException() throws Exception {
                Long nonExistentProductId = 999L;
                doThrow(new ResourceNotFoundException("Producto", "id", nonExistentProductId))
                                .when(productService).deleteProduct(nonExistentProductId);

                mockMvc.perform(delete("/api/products/{productId}", nonExistentProductId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Producto no encontrada con id : '%s'",
                                                                nonExistentProductId))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + nonExistentProductId)));
        }

        @Test
        void updateProductStock_Success() throws Exception {
                when(productService.updateStock(eq(productId), any(StockUpdateDTO.class)))
                                .thenReturn(new ProductResponseDTO(productId, productResponseDTO.getName(),
                                                stockUpdateDTO.getNewStock(), branchId));

                mockMvc.perform(patch("/api/products/{productId}/stock", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stockUpdateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Stock del producto actualizado.")))
                                .andExpect(jsonPath("$.data.id", is(productId.intValue())))
                                .andExpect(jsonPath("$.data.stock", is(stockUpdateDTO.getNewStock())));
        }

        @Test
        void updateProductStock_InvalidInput() throws Exception {
                StockUpdateDTO invalidStockDTO = new StockUpdateDTO(-1);

                mockMvc.perform(patch("/api/products/{productId}/stock", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidStockDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", is("El stock no puede ser negativo.")))
                                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + productId + "/stock")));
        }

        @Test
        void updateProductStock_ResourceNotFoundException() throws Exception {
                Long nonExistentProductId = 999L;
                when(productService.updateStock(eq(nonExistentProductId), any(StockUpdateDTO.class)))
                                .thenThrow(new ResourceNotFoundException("Producto", "id", nonExistentProductId));

                mockMvc.perform(patch("/api/products/{productId}/stock", nonExistentProductId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stockUpdateDTO)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Producto no encontrada con id : '%s'",
                                                                nonExistentProductId))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + nonExistentProductId + "/stock")));
        }

        @Test
        void updateProductName_Success() throws Exception {
                when(productService.updateName(eq(productId), any(NameUpdateDTO.class)))
                                .thenReturn(new ProductResponseDTO(productId, nameUpdateDTO.getNewName(),
                                                productResponseDTO.getStock(), branchId));

                mockMvc.perform(put("/api/products/{productId}/name", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Nombre del producto actualizado.")))
                                .andExpect(jsonPath("$.data.id", is(productId.intValue())))
                                .andExpect(jsonPath("$.data.name", is(nameUpdateDTO.getNewName())));
        }

        @Test
        void updateProductName_InvalidInput() throws Exception {
                NameUpdateDTO invalidNameDTO = new NameUpdateDTO("");

                mockMvc.perform(put("/api/products/{productId}/name", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidNameDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", is("El nuevo nombre no puede estar vacío.")))
                                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + productId + "/name")));
        }

        @Test
        void updateProductName_ResourceNotFoundException() throws Exception {
                Long nonExistentProductId = 999L;
                when(productService.updateName(eq(nonExistentProductId), any(NameUpdateDTO.class)))
                                .thenThrow(new ResourceNotFoundException("Producto", "id", nonExistentProductId));

                mockMvc.perform(put("/api/products/{productId}/name", nonExistentProductId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(nameUpdateDTO)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Producto no encontrada con id : '%s'",
                                                                nonExistentProductId))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + nonExistentProductId + "/name")));
        }

        @Test
        void updateProductName_DuplicateResourceException() throws Exception {
                String duplicateProductName = "Otro Producto";
                NameUpdateDTO duplicateNameDTO = new NameUpdateDTO(duplicateProductName);
                when(productService.updateName(eq(productId), any(NameUpdateDTO.class)))
                                .thenThrow(new DuplicateResourceException("Producto", "nombre", duplicateProductName));

                mockMvc.perform(put("/api/products/{productId}/name", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateNameDTO)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Ya existe un recurso Producto con nombre : '%s'",
                                                                duplicateProductName))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_DUPLICATE")))
                                .andExpect(jsonPath("$.path", is("/api/products/" + productId + "/name")));
        }

        @Test
        void getTopStockProducts_Success() throws Exception {
                List<TopProductPerBranchDTO> topProducts = Arrays.asList(topProduct1, topProduct2);
                when(productService.getTopStockProductsForFranchise(eq(franchiseId))).thenReturn(topProducts);

                mockMvc.perform(get("/api/franchises/{franchiseId}/products/top-stock", franchiseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].productId", is(topProduct1.getProductId().intValue())))
                                .andExpect(jsonPath("$.data[0].productName", is(topProduct1.getProductName())))
                                .andExpect(jsonPath("$.data[0].stock", is(topProduct1.getStock())))
                                .andExpect(jsonPath("$.data[0].branchId", is(topProduct1.getBranchId().intValue())))
                                .andExpect(jsonPath("$.data[0].branchName", is(topProduct1.getBranchName())))
                                .andExpect(jsonPath("$.data[1].productId", is(topProduct2.getProductId().intValue())))
                                .andExpect(jsonPath("$.data[1].productName", is(topProduct2.getProductName())))
                                .andExpect(jsonPath("$.data[1].stock", is(topProduct2.getStock())))
                                .andExpect(jsonPath("$.data[1].branchId", is(topProduct2.getBranchId().intValue())))
                                .andExpect(jsonPath("$.data[1].branchName", is(topProduct2.getBranchName())));
        }

        @Test
        void getTopStockProducts_ResourceNotFoundException_Franchise() throws Exception {
                Long nonExistentFranchiseId = 99L;
                when(productService.getTopStockProductsForFranchise(eq(nonExistentFranchiseId)))
                                .thenThrow(new ResourceNotFoundException("Franquicia", "id", nonExistentFranchiseId));

                mockMvc.perform(get("/api/franchises/{franchiseId}/products/top-stock", nonExistentFranchiseId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message",
                                                is(String.format("Franquicia no encontrada con id : '%s'",
                                                                nonExistentFranchiseId))))
                                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                                .andExpect(jsonPath("$.path", is(
                                                "/api/franchises/" + nonExistentFranchiseId + "/products/top-stock")));
        }
}