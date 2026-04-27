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
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@Tag("Integration-Step2")
class Step2_IntegrationV_Test {

    private Repository<Integer, Product> mockProductRepo;
    private ProductValidator realValidator;
    private ProductService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockProductRepo = (Repository<Integer, Product>) Mockito.mock(Repository.class);

        realValidator = new ProductValidator();

        service = new ProductService(mockProductRepo, realValidator);
    }

    @Test
    @DisplayName("Integrare S+V: Produs valid -> trece de validator și apelează repo mock")
    void addProduct_IntegrationSV_ValidProduct() {
        Product product = new Product(1, "Americano", 12.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        assertDoesNotThrow(() -> service.addProduct(product));

        Mockito.verify(mockProductRepo, Mockito.times(1)).save(product);
    }

    @Test
    @DisplayName("Integrare S+V: Produs cu preț invalid -> aruncă ValidationException, repo mock nu e apelat")
    void addProduct_IntegrationSV_InvalidPrice() {
        Product product = new Product(1, "Espresso", 0.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(product));

        assertEquals("Pret invalid!\n", ex.getMessage());

        Mockito.verify(mockProductRepo, Mockito.never()).save(any());
    }
}
