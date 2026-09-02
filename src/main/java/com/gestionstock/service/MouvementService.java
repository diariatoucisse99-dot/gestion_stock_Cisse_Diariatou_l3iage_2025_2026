package com.gestionstock.service;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementService {

    List<Mouvement> findAll();

    List<Mouvement> findByProduit(int produitId);

    List<Mouvement> findByType(TypeMouvement type);

    List<Mouvement> findByPeriode(LocalDateTime debut, LocalDateTime fin);

    void enregistrerMouvement(int produitId, TypeMouvement type, int quantite, String motif);

    void enregistrerMouvement(int produitId, TypeMouvement type, int quantite, String motif, Long utilisateurId);}