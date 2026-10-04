package com.ecommerce;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ProductSearchTest {
    @Autowired ProductService productService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private Product product(String name, String price, Category c) {
        Product p = new Product();
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(new BigDecimal(price));
        p.setStock(10);
        p.setCategory(c);
        return productRepository.save(p);
    }

    @Test
    void filtersByKeywordCategoryAndPrice() {
        Category pets = categoryRepository.save(new Category("PetsTest", "x"));
        Category toys = categoryRepository.save(new Category("ToysTest", "x"));
        product("Zebra Collar", "300.00", pets);
        product("Zebra Leash", "900.00", pets);
        product("Zebra Plush", "500.00", toys);

        assertEquals(3, productService.search("zebra", null, null, null, "newest", 0).getTotalElements());
        assertEquals(2, productService.search("zebra", pets.getId(), null, null, "newest", 0).getTotalElements());

        Page<Product> cheap = productService.search("zebra", null, null, new BigDecimal("600"), "price_asc", 0);
        assertEquals(2, cheap.getTotalElements());
        assertEquals("Zebra Collar", cheap.getContent().get(0).getName());
    }
}
