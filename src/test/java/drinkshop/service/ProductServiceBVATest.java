package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@Tag("Service-Update-BVA")
class ProductServiceBVATest {

    private final Repository<Integer, Product> repo = Mockito.mock(Repository.class);
    private final ProductValidator validator = new ProductValidator();
    private final ProductService service = new ProductService(repo, validator);

    @Test
    @DisplayName("BVA Update Valid: Pret la limita minima pozitiva")
    void testUpdateProduct_BVA_ValidPrice() {
        double pret = 0.01;
        int id = 1;
        String nume = "Update BVA";

        assertDoesNotThrow(() -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));
        Mockito.verify(repo, Mockito.times(1)).update(ArgumentMatchers.any(Product.class));
    }

    @Test
    @DisplayName("BVA Update Invalid: Pret zero")
    void testUpdateProduct_BVA_InvalidPriceZero() {
        double pret = 0.0;
        int id = 1;
        String nume = "Update BVA";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        assertTrue(ex.getMessage().contains("Pret invalid"));
        Mockito.verify(repo, Mockito.never()).update(ArgumentMatchers.any(Product.class));
    }

    @Test
    @DisplayName("BVA Update Invalid: Nume cu un singur spatiu")
    void testUpdateProduct_BVA_InvalidNameSpace() {
        String nume = " ";
        int id = 1;
        double pret = 10.0;

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        assertTrue(ex.getMessage().contains("Numele nu poate fi gol"));
    }
}