package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("Integration-Step3")
class Step3_IntegrationR_Test {

    private Repository<Integer, Product> realProductRepo;
    private ProductValidator realValidator;
    private ProductService service;

    @BeforeEach
    void setUp() {
        realProductRepo = new Repository<Integer, Product>() {
            private final Map<Integer, Product> data = new HashMap<>();

            @Override public Product findOne(Integer id) { return data.get(id); }
            @Override public List<Product> findAll() { return new ArrayList<>(data.values()); }
            @Override public Product save(Product entity) {
                data.put(entity.getId(), entity);
                return entity;
            }
            @Override public Product delete(Integer id) { return data.remove(id); }
            @Override public Product update(Product entity) {
                data.put(entity.getId(), entity);
                return entity;
            }
        };

        realValidator = new ProductValidator();
        service = new ProductService(realProductRepo, realValidator);
    }

    @Test
    @DisplayName("Integrare S+V+R: Produs valid -> adăugat cu succes în repository-ul real")
    void addProduct_IntegrationSVR_ValidProduct() {
        Product product = new Product(1, "Cappuccino", 15.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        assertDoesNotThrow(() -> service.addProduct(product));

        assertEquals(1, service.getAllProducts().size());
        assertEquals("Cappuccino", service.findById(1).getNume());
    }

    @Test
    @DisplayName("Integrare S+V+R: Produs cu preț invalid -> respins, repository gol")
    void addProduct_IntegrationSVR_InvalidPrice() {
        Product product = new Product(1, "Latte", 0.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(product));

        assertEquals("Pret invalid!\n", ex.getMessage());

        assertEquals(0, service.getAllProducts().size());
    }
}