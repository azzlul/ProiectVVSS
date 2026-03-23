package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@Tag("Service-Update-BVA")
class ProductServiceBVATest {

    private final Repository<Integer, Product> repo = Mockito.mock(Repository.class);
    private final ProductValidator validator = new ProductValidator();
    private final ProductService service = new ProductService(repo, validator);

    @ParameterizedTest
    @ValueSource(doubles = {0.01})
    @DisplayName("BVA Update Valid: Pret la limita minima pozitiva")
    void testUpdateProduct_BVA_ValidPrice(double pret) {
        int id = 1;
        String nume = "Update BVA";

        assertDoesNotThrow(() -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));
        Mockito.verify(repo, Mockito.times(1)).update(ArgumentMatchers.any(Product.class));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0})
    @DisplayName("BVA Update Invalid: Pret zero")
    void testUpdateProduct_BVA_InvalidPriceZero(double pret) {
        int id = 1;
        String nume = "Update BVA";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        assertTrue(ex.getMessage().contains("Pret invalid"));
        Mockito.verify(repo, Mockito.never()).update(ArgumentMatchers.any(Product.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {" "})
    @DisplayName("BVA Update Invalid: Nume cu un singur spatiu")
    void testUpdateProduct_BVA_InvalidNameSpace(String nume) {
        int id = 1;
        double pret = 10.0;

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        assertTrue(ex.getMessage().contains("Numele nu poate fi gol"));
    }
}