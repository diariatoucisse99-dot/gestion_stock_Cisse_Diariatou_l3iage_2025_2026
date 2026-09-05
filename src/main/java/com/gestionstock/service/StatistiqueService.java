package com.gestionstock.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatistiqueService {

    double valeurTotaleStock();

    String produitLePlusMouvemente(LocalDate debut, LocalDate fin);

    String categoriePlusForteValeur();

    String fournisseurAvecPlusDeProduits();

    long nombreRupturesEvitees(LocalDate debut, LocalDate fin);

    Map<String, Long> quantitesParMoisEtType(LocalDate debut, LocalDate fin);

    Map<String, Double> valeurStockParCategorie();
}