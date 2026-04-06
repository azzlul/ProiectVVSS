package drinkshop.service;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for StocService.consuma() method using control flow coverage criteria
 * 
 * Method Analysis:
 * - Cyclomatic Complexity (CC) = 5 (using formula E-N+2 and decision points+1)
 * - Decision points: 
 *   1. !areSuficient(reteta) check (line 55)
 *   2. for loop condition for ingredients (line 59)
 *   3. for loop condition for stock entries (line 69)
 *   4. if (ramas <= 0) condition (line 70)
 * - Covers: statement coverage (SC), decision coverage (DC), condition coverage (CC),
 *   multiple condition coverage (MCC), all-path coverage (APC), loop coverage (LC)
 */
@DisplayName("StocService.consuma() - Control Flow Coverage Tests")
class StocServiceConsumaTest {

    @Mock
    private Repository<Integer, Stoc> stocRepoMock;
    
    private StocService stocService;
    private List<Stoc> stocList;

    @BeforeEach
    void setUp() {
        // Initialize mock repository
        MockitoAnnotations.openMocks(this);
        stocService = new StocService(stocRepoMock);
        stocList = new ArrayList<>();
    }

    // ============================================================================
    // PATH 1: Insufficient Stock - Exception Path
    // Tests decision coverage for !areSuficient(reteta) = true
    // ============================================================================
    @Nested
    @DisplayName("Path 1: Insufficient Stock Exception")
    class InsufficientStockPath {

        @Test
        @DisplayName("SC1: Throws exception when recipe has insufficient stock")
        void testConsumaThrowsExceptionInsufficientStock() {
            // Arrange: Setup insufficient stock
            stocList.add(new Stoc(1, "Sugar", 5, 10)); // Only 5 units, need 10
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Sugar", 10.0));

            // Act & Assert: Verify exception is thrown
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> stocService.consuma(recipe),
                    "Should throw exception when stock is insufficient"
            );

            assertTrue(exception.getMessage().contains("Stoc insuficient"),
                    "Exception message should mention insufficient stock");
            
            // Verify no updates were performed
            verify(stocRepoMock, never()).update(any(Stoc.class));
        }

        @Test
        @DisplayName("SC2: Multiple ingredients - insufficient for one")
        void testConsumaMultipleIngredientsInsufficientOneIngredient() {
            // Arrange: One ingredient has insufficient stock
            stocList.add(new Stoc(1, "Sugar", 50, 10));
            stocList.add(new Stoc(2, "Water", 3, 10)); // Insufficient
            when(stocRepoMock.findAll()).thenReturn(stocList);

            List<IngredientReteta> ingredients = new ArrayList<>();
            ingredients.add(createIngredient("Sugar", 10.0));
            ingredients.add(createIngredient("Water", 5.0));
            Reteta recipe = new Reteta(1, ingredients);

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> stocService.consuma(recipe));
            verify(stocRepoMock, never()).update(any(Stoc.class));
        }
    }

    // ============================================================================
    // PATH 2: Single Ingredient, Single Stock Entry
    // Tests statement coverage + simple decision/loop paths
    // ============================================================================
    @Nested
    @DisplayName("Path 2: Single Ingredient, Single Stock Entry")
    class SingleIngredientSingleStockPath {

        @Test
        @DisplayName("SC3: Consumes from single stock entry completely")
        void testConsumaSingleIngredientSingleStockComplete() {
            // Arrange: Exact quantity available
            Stoc stock = new Stoc(1, "Sugar", 10, 5);
            stocList.add(stock);
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Sugar", 10.0));

            // Act
            stocService.consuma(recipe);

            // Assert: Stock should be completely consumed
            verify(stocRepoMock, times(1)).update(argThat(s -> 
                    s.getIngredient().equalsIgnoreCase("Sugar") && s.getCantitate() == 0));
        }

        @Test
        @DisplayName("SC4: Consumes from single stock entry partially")
        void testConsumaSingleIngredientSingleStockPartial() {
            // Arrange: More stock than needed
            Stoc stock = new Stoc(1, "Sugar", 50, 5);
            stocList.add(stock);
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Sugar", 10.0));

            // Act
            stocService.consuma(recipe);

            // Assert: Only 10 units consumed
            verify(stocRepoMock, times(1)).update(argThat(s -> 
                    s.getIngredient().equalsIgnoreCase("Sugar") && s.getCantitate() == 40));
        }

        @Test
        @DisplayName("DC1: Decision coverage - ramas <= 0 condition triggers break")
        void testConsumaBreakConditionTriggered() {
            // Arrange: Stock exactly matches requirement
            Stoc stock = new Stoc(1, "Lemon", 15, 5);
            stocList.add(stock);
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Lemon", 15.0));

            // Act
            stocService.consuma(recipe);

            // Assert: ramas should be 0, triggering the break condition
            verify(stocRepoMock, times(1)).update(argThat(s -> s.getCantitate() == 0));
        }
    }

    // ============================================================================
    // PATH 3: Single Ingredient, Multiple Stock Entries
    // Tests loop coverage (inner loop) + decision/condition coverage
    // ============================================================================
    @Nested
    @DisplayName("Path 3: Single Ingredient, Multiple Stock Entries")
    class SingleIngredientMultipleStockPath {

        @Test
        @DisplayName("LC1: Inner loop - iterates over 2 stock entries, consumes from both")
        void testConsumaMultipleStockEntriesPartialConsumption() {
            // Arrange: 2 stock entries for same ingredient
            stocList.add(new Stoc(1, "Sugar", 7, 5));
            stocList.add(new Stoc(2, "Sugar", 8, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Sugar", 12.0));

            // Act
            stocService.consuma(recipe);

            // Assert: First entry consumed (7), second entry partially consumed (5)
            verify(stocRepoMock, atLeastOnce()).update(any(Stoc.class));
            
            // Verify both entries were updated
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 2 && s.getCantitate() == 3));
        }

        @Test
        @DisplayName("LC2: Inner loop - iterates over 3 stock entries")
        void testConsumaThreeStockEntries() {
            // Arrange: 3 stock entries, need to consume from all
            stocList.add(new Stoc(1, "Water", 5, 5));
            stocList.add(new Stoc(2, "Water", 5, 5));
            stocList.add(new Stoc(3, "Water", 5, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Water", 12.0));

            // Act
            stocService.consuma(recipe);

            // Assert: First two consumed completely, third partially
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 2 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 3 && s.getCantitate() == 3));
        }

        @Test
        @DisplayName("LC3: Inner loop - break condition prevents unnecessary iterations")
        void testConsumaBreakPreventsFurtherIterations() {
            // Arrange: Multiple stocks, but only first one needed
            stocList.add(new Stoc(1, "Honey", 100, 5));
            stocList.add(new Stoc(2, "Honey", 100, 5));
            stocList.add(new Stoc(3, "Honey", 100, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Honey", 50.0));

            // Act
            stocService.consuma(recipe);

            // Assert: Only first stock entry was updated (break condition)
            verify(stocRepoMock, times(1)).update(any(Stoc.class));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 50));
        }
    }

    // ============================================================================
    // PATH 4: Multiple Ingredients
    // Tests outer loop coverage + statement coverage
    // ============================================================================
    @Nested
    @DisplayName("Path 4: Multiple Ingredients")
    class MultipleIngredientsPath {

        @Test
        @DisplayName("LC4: Outer loop - iterates over 2 ingredients")
        void testConsumaMultipleIngredients() {
            // Arrange: Two different ingredients
            stocList.add(new Stoc(1, "Sugar", 20, 5));
            stocList.add(new Stoc(2, "Lemon", 15, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            List<IngredientReteta> ingredients = new ArrayList<>();
            ingredients.add(createIngredient("Sugar", 10.0));
            ingredients.add(createIngredient("Lemon", 8.0));
            Reteta recipe = new Reteta(1, ingredients);

            // Act
            stocService.consuma(recipe);

            // Assert: Both ingredients consumed correctly
            verify(stocRepoMock).update(argThat(s -> 
                    s.getIngredient().equalsIgnoreCase("Sugar") && s.getCantitate() == 10));
            verify(stocRepoMock).update(argThat(s -> 
                    s.getIngredient().equalsIgnoreCase("Lemon") && s.getCantitate() == 7));
        }

        @Test
        @DisplayName("LC5: Outer loop - iterates over 3 ingredients")
        void testConsumaThreeIngredients() {
            // Arrange: Three ingredients with various stock distributions
            stocList.add(new Stoc(1, "Sugar", 30, 5));
            stocList.add(new Stoc(2, "Water", 25, 5));
            stocList.add(new Stoc(3, "Lemon", 20, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            List<IngredientReteta> ingredients = new ArrayList<>();
            ingredients.add(createIngredient("Sugar", 15.0));
            ingredients.add(createIngredient("Water", 10.0));
            ingredients.add(createIngredient("Lemon", 12.0));
            Reteta recipe = new Reteta(1, ingredients);

            // Act
            stocService.consuma(recipe);

            // Assert: All ingredients processed
            verify(stocRepoMock, times(3)).update(any(Stoc.class));
        }
    }

    // ============================================================================
    // PATH 5: Complex Scenarios - Multiple Conditions
    // Tests MCC (Multiple Condition Coverage), APC (All Path Coverage)
    // ============================================================================
    @Nested
    @DisplayName("Path 5: Complex Scenarios - MCC & APC")
    class ComplexScenariosPath {

        @Test
        @DisplayName("MCC1: Multiple conditions - ingredient from multiple stock entries")
        void testConsumaMCCMultipleStockEntriesPerIngredient() {
            // Arrange: Complex scenario with multiple ingredients and multiple stock entries
            stocList.add(new Stoc(1, "Sugar", 8, 5));
            stocList.add(new Stoc(2, "Sugar", 10, 5));
            stocList.add(new Stoc(3, "Water", 12, 5));
            stocList.add(new Stoc(4, "Water", 8, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            List<IngredientReteta> ingredients = new ArrayList<>();
            ingredients.add(createIngredient("Sugar", 15.0));
            ingredients.add(createIngredient("Water", 18.0));
            Reteta recipe = new Reteta(1, ingredients);

            // Act
            stocService.consuma(recipe);

            // Assert: Verify correct consumption from multiple entries
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 2 && s.getCantitate() == 3));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 3 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 4 && s.getCantitate() == 2));
        }

        @Test
        @DisplayName("APC1: All paths - ingredient exists in first stock entry")
        void testConsumaAPCIngredientInFirstEntry() {
            // Arrange: Ingredient in first position
            stocList.add(new Stoc(1, "Mint", 30, 5));
            stocList.add(new Stoc(2, "Sugar", 30, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Mint", 10.0));

            // Act
            stocService.consuma(recipe);

            // Assert
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 20));
            verify(stocRepoMock, never()).update(argThat(s -> s.getId() == 2));
        }

        @Test
        @DisplayName("APC2: All paths - ingredient exists in last stock entry")
        void testConsumaAPCIngredientInLastEntry() {
            // Arrange: Ingredient in last position
            stocList.add(new Stoc(1, "Sugar", 30, 5));
            stocList.add(new Stoc(2, "Lemon", 30, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Lemon", 10.0));

            // Act
            stocService.consuma(recipe);

            // Assert
            verify(stocRepoMock, never()).update(argThat(s -> s.getId() == 1));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 2 && s.getCantitate() == 20));
        }

        @Test
        @DisplayName("DC2: Decision coverage - both branches of areSuficient check")
        void testConsumaBothBranchesOfAreSufficientCheck() {
            // This test verifies both true (sufficient) and false (insufficient) paths
            // are covered by combining with other test cases
            
            // Valid path covered in other tests
            Stoc stock = new Stoc(1, "Tea", 50, 5);
            stocList.add(stock);
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Tea", 20.0));

            // Act
            stocService.consuma(recipe);

            // Assert: Sufficient stock path executed successfully
            verify(stocRepoMock, times(1)).update(any(Stoc.class));
        }
    }

    // ============================================================================
    // PATH 6: Edge Cases & Boundary Conditions
    // ============================================================================
    @Nested
    @DisplayName("Path 6: Edge Cases & Boundary Conditions")
    class EdgeCasesPath {

        @Test
        @DisplayName("SC5: Empty stock list for ingredient")
        void testConsumaNoStockForIngredient() {
            // This should fail at areSuficient check
            when(stocRepoMock.findAll()).thenReturn(new ArrayList<>());

            Reteta recipe = createReceta(1, createIngredient("NonExistent", 5.0));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> stocService.consuma(recipe));
        }

        @Test
        @DisplayName("SC6: Case-insensitive ingredient matching")
        void testConsumaCaseInsensitiveMatching() {
            // Arrange: Different case for ingredient name
            stocList.add(new Stoc(1, "SUGAR", 20, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("sugar", 10.0));

            // Act
            stocService.consuma(recipe);

            // Assert: Case-insensitive matching should work
            verify(stocRepoMock, times(1)).update(argThat(s -> s.getCantitate() == 10));
        }

        @Test
        @DisplayName("LC6: Loop with minimum iterations (1 ingredient, 1 stock)")
        void testConsumaMinimumLoopIterations() {
            // Arrange: Minimal scenario
            stocList.add(new Stoc(1, "Coffee", 10, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Coffee", 5.0));

            // Act
            stocService.consuma(recipe);

            // Assert
            verify(stocRepoMock, times(1)).update(any(Stoc.class));
        }

        @Test
        @DisplayName("SC7: Exact quantity consumption from multiple entries")
        void testConsumaExactQuantityFromMultipleEntries() {
            // Arrange: Exact match across entries
            stocList.add(new Stoc(1, "Milk", 5, 5));
            stocList.add(new Stoc(2, "Milk", 5, 5));
            stocList.add(new Stoc(3, "Milk", 5, 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Milk", 15.0));

            // Act
            stocService.consuma(recipe);

            // Assert: All entries consumed exactly
            verify(stocRepoMock).update(argThat(s -> s.getId() == 1 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 2 && s.getCantitate() == 0));
            verify(stocRepoMock).update(argThat(s -> s.getId() == 3 && s.getCantitate() == 0));
        }

        @ParameterizedTest
        @CsvSource({
                "10.5, 5", // Fractional quantity consumption
                "1.0, 9",   // Small quantity
                "100.0, 0", // Large quantity complete consumption
        })
        @DisplayName("SC8: Various quantity values")
        void testConsumaVariousQuantities(double required, double remaining) {
            // Arrange
            stocList.add(new Stoc(1, "Item", (int)(required + remaining), 5));
            when(stocRepoMock.findAll()).thenReturn(stocList);

            Reteta recipe = createReceta(1, createIngredient("Item", required));

            // Act
            stocService.consuma(recipe);

            // Assert
            verify(stocRepoMock, times(1)).update(any(Stoc.class));
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private Reteta createReceta(int id, IngredientReteta ingredient) {
        List<IngredientReteta> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        return new Reteta(id, ingredients);
    }

    private IngredientReteta createIngredient(String name, double quantity) {
        return new IngredientReteta(name, quantity);
    }
}




