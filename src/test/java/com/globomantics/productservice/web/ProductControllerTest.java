package com.globomantics.productservice.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globomantics.productservice.model.Product;
import com.globomantics.productservice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {
    @MockBean
    private ProductService productService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /product/1 - Found")
    void testGetProductByIdFound() throws Exception {
        // Setup our mocked service
        Product good_product = new Product(1, "good product", 400, 1);
        when(productService.findById(1)).thenReturn(Optional.of(good_product));

        // Execute the GET request
        mockMvc.perform(get("/product/{id}", 1))

                // Validate response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))

                // Validate headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))

                // Validate json response
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("good product")))
                .andExpect(jsonPath("$.quantity", is(400)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("GET /product/2 - Not Found")
    void testGetProductByIdNotFound() throws Exception {
        // Set up the mocked Service
        when(productService.findById(2)).thenReturn(Optional.empty());

        // Execute the GET request
        mockMvc.perform(get("/product/{id}", 2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /products - Found")
    void testGetAllProducts() throws Exception {
        // Set up the mocked Service
        Product product = new Product(1, "first", 20, 1);
        Product product2 = new Product(2, "second", 20, 1);
        List<Product> products = new ArrayList<>(Arrays.asList(product, product2));
        when(productService.findAll()).thenReturn(products);

        // Execute the GET request
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id", is(1)))
                .andExpect(jsonPath("[1].name", is("second")));
    }

    @Test
    @DisplayName("POST /product - Success")
    void testPostProduct() throws Exception {
        // Set up mocked Service
        Product postedProduct = new Product("prod", 20);
        Product responseProduct = new Product(1, "prod", 20, 1);
        when(productService.save(any())).thenReturn(responseProduct);
//        doReturn(responseProduct).when(productService).save(any());

        // Execute post request
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postedProduct)))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, is("\"1\"")))
                .andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))

                // Validate the response body
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.name", is("prod")))
                .andExpect(jsonPath("$.quantity", is(20)));
    }

    @Test
    @DisplayName("PUT /product/1 - Success")
    void testPutProductSuccess() throws Exception {
        // Set up mocked Service
        Product putProduct = new Product("newProdName", 20);
        Product responseProduct = new Product(1, "prod", 20, 1);
        doReturn(Optional.of(responseProduct)).when(productService).findById(1);
        when(productService.update(any())).thenReturn(true);
//        doReturn(responseProduct).when(productService).save(any());

        // Execute put request
        mockMvc.perform(put("/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, is("\"2\"")))
                .andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))

                // Validate the response body
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.version", is(2)))
                .andExpect(jsonPath("$.name", is("newProdName")))
                .andExpect(jsonPath("$.quantity", is(20)));
    }
    @Test
    @DisplayName("PUT /product/1 - Version Mismatch")
    void testPutProductVersionMissMatch() throws Exception {
        // Set up mocked Service
        Product putProduct = new Product("newProdName", 20);
        Product findByIdProduct = new Product(1, "prod", 20, 2);
        doReturn(Optional.of(findByIdProduct)).when(productService).findById(1);

        // Execute put request
        mockMvc.perform(put("/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /product/1 - Not Found")
    void testProductPutNotFound() throws Exception {
        // Setup mocked Service
        Product putProduct = new Product("Product Name", 10);
        doReturn(Optional.empty()).when(productService).findById(1);

        // Execute put request
        mockMvc.perform(put("/product/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /product/1 Success")
    void testProductDeleteSuccess() throws Exception {
        // Set up the mocked product
        Product mockProduct = new Product(1, "product", 11, 1);

        // Set up the mocked Service
        doReturn(Optional.of(mockProduct)).when(productService).findById(1);
        doReturn(true).when(productService).delete(1);

        // Execute our DELETE request
        mockMvc.perform(delete("/product/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /product/1 - Not Found")
    void testProductDeleteNotFound() throws Exception {
        // Set up mocked Service
        doReturn(Optional.empty()).when(productService).findById(1);

        // Execute the Service call
        mockMvc.perform(delete("/product/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /product/1 - Failure")
    void testProductDeleteFailed() throws Exception {
        // Set up the mocked product
        Product mockProduct = new Product(1, "product", 11, 1);

        // Set up the mocked Service
        doReturn(Optional.of(mockProduct)).when(productService).findById(1);
        doReturn(false).when(productService).delete(1);

        // Execute the Service call
        mockMvc.perform(delete("/product/{id}", 1))
                .andExpect(status().isInternalServerError());
    }




    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
