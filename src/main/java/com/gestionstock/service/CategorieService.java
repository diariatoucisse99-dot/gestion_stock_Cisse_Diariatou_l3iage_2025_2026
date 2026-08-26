package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import java.util.List;
import java.util.Optional;

public interface CategorieService {

    List<Categorie> findAllCategories();

    Optional<Categorie> findById(int id);

    void addCategorie(Categorie categorie);

    void updateCategorie(Categorie categorie);

    void deleteCategorie(int id);

    int compterProduitsRattaches(int categorieId);
}