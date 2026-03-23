package drinkshop.domain;

public class Product {

    private int id;
    private String nume;
    private double pret;
    private CategorieBautura categorie;
    private TipBautura tip;

    public Product(int id, String nume, double pret,
                  CategorieBautura categorie,
                  TipBautura tip) {

        if (id < 0) {
            throw new IllegalArgumentException("ID-ul trebuie sa fie pozitiv");
        }
        if (categorie == null) {
            throw new IllegalArgumentException("Categoria nu poate fi nula");
        }
        if (tip == null) {
            throw new IllegalArgumentException("Tipul nu poate fi nul");
        }

        this.id = id;
        this.nume = nume;
        this.pret = pret;
        this.categorie = categorie;
        this.tip = tip;
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public double getPret() { return pret; }
    public CategorieBautura getCategorie() { return categorie; }

    public void setCategorie(CategorieBautura categorie) {
        this.categorie = categorie;
    }

    public TipBautura getTip() { return tip; }

    public void setTip(TipBautura tip) {
        this.tip = tip;
    }
    public void setNume(String nume) { this.nume = nume; }
    public void setPret(double pret) { this.pret = pret; }

    @Override
    public String toString() {
        return nume + " (" + categorie + ", " + tip + ") - " + pret + " lei";
    }
}