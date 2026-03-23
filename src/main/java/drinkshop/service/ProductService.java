package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator; // Importăm validatorul

import java.util.List;

public class ProductService {

    private final Repository<Integer, Product> productRepo;
    private final ProductValidator validator; // Câmp nou pentru validator

    // Actualizăm constructorul pentru a primi și validatorul
    public ProductService(Repository<Integer, Product> productRepo, ProductValidator validator) {
        this.productRepo = productRepo;
        this.validator = validator;
    }

    public void addProduct(Product p) {
        // Pasul de validare conform cerințelor de testare black-box [cite: 8, 16]
        validator.validate(p);
        productRepo.save(p);
    }

    public void updateProduct(int id, String name, double price, CategorieBautura categorie, TipBautura tip) {
        Product updated = new Product(id, name, price, categorie, tip);
        // Validăm obiectul nou creat înainte de actualizare
        validator.validate(updated);
        productRepo.update(updated);
    }

    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product findById(int id) {
        return productRepo.findOne(id);
    }

    public List<Product> filterByCategorie(CategorieBautura categorie) {
        if (categorie == CategorieBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getCategorie() == categorie)
                .toList();
    }

    public List<Product> filterByTip(TipBautura tip) {
        if (tip == TipBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getTip() == tip)
                .toList();
    }
}