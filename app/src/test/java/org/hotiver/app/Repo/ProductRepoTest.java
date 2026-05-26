package org.hotiver.app.Repo;

import org.hotiver.app.AbstractRepositoryTest;
import org.hotiver.app.factory.TestEntityFactory;
import org.hotiver.domain.Entity.Product;
import org.hotiver.repo.CategoryRepo;
import org.hotiver.repo.ProductRepo;
import org.hotiver.repo.SellerRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestEntityFactory.class)
public class ProductRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TestEntityFactory entityFactory;
    @Autowired
    private SellerRepo sellerRepo;

    @Test
    void shouldReturnProduct() {
        //Long productId = 1L;
        Product product = createProduct();

        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> result = productRepo.findById(1L);

        assertThat(result).isPresent();
        assertEquals("test", result.get().getName());
    }

    private Product createProduct() {
        Product product = new Product(
                null,
                "test",
                BigDecimal.valueOf(100),
                "test",
                categoryRepo.findById(1L).get(),
                null,
                entityFactory.createSeller(),
                5,
                5,
                Date.valueOf(LocalDate.now()),
                BigDecimal.valueOf(5),
                true,
                null
        );

        return product;
    }
}
