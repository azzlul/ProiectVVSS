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

@Tag("Service-Update-ECP")
class ProductServiceECPTest {

    private final Repository<Integer, Product> repo = Mockito.mock(Repository.class);
    private final ProductValidator validator = new ProductValidator();
    private final ProductService service = new ProductService(repo, validator);

    @ParameterizedTest
    @ValueSource(strings = {"Pepsi Max", "Ceai Verde"})
    @DisplayName("ECP Update Valid: Nume valid")
    void testUpdateProduct_ECP_Valid(String nume) {
        int id = 1;
        double pret = 12.0;

        assertDoesNotThrow(() -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        Mockito.verify(repo, Mockito.atLeastOnce()).update(ArgumentMatchers.any(Product.class));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.0, -0.5})
    @DisplayName("ECP Update Invalid: Pret negativ")
    void testUpdateProduct_ECP_InvalidPrice(double pret) {
        int id = 1;
        String nume = "Produs Update";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateProduct(id, nume, pret, CategorieBautura.BUBBLE_TEA, TipBautura.BASIC));

        assertTrue(ex.getMessage().contains("Pret invalid"));
        Mockito.verify(repo, Mockito.never()).update(ArgumentMatchers.any(Product.class));
    }
}