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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

//E: src/main/java/drinkshop/domain/Product.java
//V: src/main/java/drinkshop/service/validator/ProductValidator.java
//R: src/main/java/drinkshop/repository/Repository.java (instantiat mock Repository<Integer, Product>)
//S: src/main/java/drinkshop/service/ProductService.java

@Tag("Service-Isolation-Mock")
class ProductServiceIsolationMockTest {

    private Repository<Integer, Product> productRepo;
    private ProductValidator validator;
    private ProductService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        productRepo = (Repository<Integer, Product>) Mockito.mock(Repository.class);
        validator = Mockito.mock(ProductValidator.class);
        service = new ProductService(productRepo, validator);
    }

    @Test
    @DisplayName("S isolated: addProduct valid -> validator and repository are both called")
    void addProduct_validProduct_callsValidatorAndSave() {
        Product product = new Product(1, "Latte", 18.5, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        assertDoesNotThrow(() -> service.addProduct(product));

        Mockito.verify(validator).validate(product);
        Mockito.verify(productRepo).save(product);
    }

    @Test
    @DisplayName("S isolated: addProduct invalid -> repository is not called when validator fails")
    void addProduct_invalidProduct_validatorThrows_repositoryNotCalled() {
        Product product = new Product(1, "Latte", 18.5, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        Mockito.doThrow(new ValidationException("Pret invalid!"))
                .when(validator)
                .validate(product);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(product));

        assertEquals("Pret invalid!", ex.getMessage());
        Mockito.verify(validator).validate(product);
        Mockito.verify(productRepo, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("S isolated: updateProduct forwards the new Product object to repository")
    void updateProduct_validData_passesBuiltEntityToDependencies() {
        int id = 11;
        String name = "Bubble Berry";
        double price = 23.0;

        assertDoesNotThrow(() -> service.updateProduct(id, name, price, CategorieBautura.BUBBLE_TEA, TipBautura.WATER_BASED));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(validator).validate(captor.capture());
        Product validated = captor.getValue();

        assertEquals(id, validated.getId());
        assertEquals(name, validated.getNume());
        assertEquals(price, validated.getPret());

        Mockito.verify(productRepo).update(validated);
    }
}
