package drinkshop.domain;

public class IngredientReteta {

    private String denumire;
    private double cantitate;

    public IngredientReteta(String denumire, double cantitate) {
        if(denumire == null || denumire.isEmpty()) {
            throw new IllegalArgumentException("Denumirea ingredientului nu poate fi nula sau goala.");
        }
        if(cantitate <= 0) {
            throw new IllegalArgumentException("Cantitatea trebuie sa fie pozitiva.");
        }
        this.denumire = denumire;
        this.cantitate = cantitate;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public double getCantitate() {
        return cantitate;
    }

    public void setCantitate(double cantitate) {
        this.cantitate = cantitate;
    }

    @Override
    public String toString() {
        return denumire + "," + cantitate;
    }
}