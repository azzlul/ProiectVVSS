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

@Tag("Integration-Step4")
class Step4_IntegrationE_Test {

    private Repository<Integer, Product> realProductRepo;
    private ProductValidator realValidator;
    private ProductService service;

    @BeforeEach
    void setUp() {
        realProductRepo = new Repository<Integer, Product>() {
            private final Map<Integer, Product> data = new HashMap<>();
            @Override public Product findOne(Integer id) { return data.get(id); }
            @Override public List<Product> findAll() { return new ArrayList<>(data.values()); }
            @Override public Product save(Product entity) { data.put(entity.getId(), entity); return entity; }
            @Override public Product delete(Integer id) { return data.remove(id); }
            @Override public Product update(Product entity) { data.put(entity.getId(), entity); return entity; }
        };
        realValidator = new ProductValidator();
        service = new ProductService(realProductRepo, realValidator);

        Product initialProduct = new Product(1, "Ceai Negru", 8.0, CategorieBautura.TEA, TipBautura.WATER_BASED);
        service.addProduct(initialProduct);
    }

    @Test
    @DisplayName("Integrare S+V+R+E: Update entitate reală cu date valide -> starea se modifică")
    void updateProduct_IntegrationFull_ValidData() {
        assertDoesNotThrow(() ->
                service.updateProduct(1, "Ceai Verde", 9.5, CategorieBautura.TEA, TipBautura.WATER_BASED)
        );

        Product updatedProduct = service.findById(1);
        assertEquals("Ceai Verde", updatedProduct.getNume());
        assertEquals(9.5, updatedProduct.getPret());
    }

    @Test
    @DisplayName("Integrare S+V+R+E: Update entitate reală cu nume gol -> aruncă eroare, datele vechi rămân intacte")
    void updateProduct_IntegrationFull_InvalidData() {

        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.updateProduct(1, "", 9.5, CategorieBautura.TEA, TipBautura.WATER_BASED)
        );
        assertEquals("Numele nu poate fi gol!\n", ex.getMessage());

        Product unmodifiedProduct = service.findById(1);
        assertEquals("Ceai Negru", unmodifiedProduct.getNume());
        assertEquals(8.0, unmodifiedProduct.getPret());
    }
}