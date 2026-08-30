package com.gestionstock.controller;

import com.gestionstock.model.Produit;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardController {

    @FXML private Label labelTotalProduits;
    @FXML private Label labelStockBas;
    @FXML private Label labelValeurStock;
    @FXML private Label labelMouvementsJour;
    @FXML private ListView<String> listeAlertesStock;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final MouvementService mouvementService = new MouvementServiceImpl();

    @FXML
    public void initialize() {
        chargerStatistiques();
    }

    // Toutes les statistiques sont recalculées à chaque affichage du dashboard,
    // jamais de valeurs figées (exigence du sujet)
    private void chargerStatistiques() {
        List<Produit> tousLesProduits = produitService.findAllProduits();
        List<Produit> produitsStockBas = produitService.findByStockBas();

        labelTotalProduits.setText(String.valueOf(tousLesProduits.size()));
        labelStockBas.setText(String.valueOf(produitsStockBas.size()));

        // Valeur totale du stock = somme(quantite * prix) sur tous les produits
        double valeurTotale = tousLesProduits.stream()
                .mapToDouble(p -> p.getQuantiteStock() * p.getPrix())
                .sum();
        labelValeurStock.setText(String.format("%,.0f FCFA", valeurTotale));

        // Mouvements du jour : entrées + sorties confondues, sur la journée en cours
        LocalDateTime debutJour = LocalDate.now().atStartOfDay();
        LocalDateTime finJour = LocalDate.now().atTime(23, 59, 59);
        long mouvementsAujourdhui = mouvementService.findByPeriode(debutJour, finJour).size();
        labelMouvementsJour.setText(String.valueOf(mouvementsAujourdhui));

        List<String> alertes = produitsStockBas.stream()
                .map(p -> p.getNom() + " — stock : " + p.getQuantiteStock() + " (min. requis : " + p.getQuantiteMin() + ")")
                .toList();

        if (alertes.isEmpty()) {
            listeAlertesStock.setItems(FXCollections.observableArrayList("Aucune alerte de stock actuellement."));
        } else {
            listeAlertesStock.setItems(FXCollections.observableArrayList(alertes));
        }
    }
}