package com.globomantics.productservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.globomantics.productservice.model.Product;
import com.globomantics.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests the ProductService.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductServiceTest {

    /**
     * The service that we want to test.
     */
    @Autowired
    private ProductService service;

    /**
     * A mock version of the ProductRepository for use in our tests.
     */
    @MockBean
    private ProductRepository repository;

    @Test
    @DisplayName("Test findById(1)")
    void testFindById() {
        // Setup our mock
        Product originalObject = new Product(1, "name", 11, 1);
        doReturn(Optional.of(originalObject)).when(repository).findById(1);

        // Execute the service call
        Optional<Product> optionalOfFoundObject = service.findById(1);

        // Assert the response
        assertTrue(optionalOfFoundObject.isPresent(), "Product should be existed");
        assertSame(originalObject, optionalOfFoundObject.get(), "Products should be the same");
    }


    @Test
    @DisplayName("Test findAll()")
    void testFindAll() {
        // Setup our mock
        Product originalObject = new Product(1, "name", 11, 1);
        Product originalObject1 = new Product(1, "name", 11, 1);
        doReturn(Arrays.asList(originalObject1, originalObject)).when(repository).findAll();

        // Execute service
        List<Product> productList = service.findAll();

        // Assert the response
        assertEquals(productList.size(), 2, "Size of retrieved objects should be 2");
    }

    @Test
    @DisplayName("Test Delete(1)")
    void testDelete() {
        // Setup our mock
        doReturn(true).when(repository).delete(1);

        // Execute the service
        boolean result = service.delete(1);

        // Assert the response
        assertTrue(result, "Product should be deleted");
    }

    @Test
    @DisplayName("Test save(productObj)")
    void testSave() {
        // Setup our mock
        Product mockProduct = new Product(1, "name", 11);
        doReturn(mockProduct).when(repository).save(any());

        // Execute service call
        Product returnedProduct = service.save(mockProduct);

        // Assert the response
        assertNotNull(returnedProduct, "returned product cannot be null");
        assertEquals(Integer.valueOf(1), returnedProduct.getVersion(), "Version should be 1");
    }
}
