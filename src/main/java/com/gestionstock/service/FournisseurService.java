package com.gestionstock.service;

import com.gestionstock.model.Fournisseur;
import java.util.List;
import java.util.Optional;

public interface FournisseurService {

    List<Fournisseur> findAllFournisseurs();

    Optional<Fournisseur> findById(int id);

    void addFournisseur(Fournisseur fournisseur);

    void updateFournisseur(Fournisseur fournisseur);

    void deleteFournisseur(int id);

    int compterProduitsRattaches(int fournisseurId);
}