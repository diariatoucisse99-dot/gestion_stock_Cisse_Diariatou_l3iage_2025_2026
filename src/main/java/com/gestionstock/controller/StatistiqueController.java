package com.gestionstock.controller;

import com.gestionstock.service.StatistiqueService;
import com.gestionstock.service.StatistiqueServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class StatistiqueController {

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Label labelValeurStock;
    @FXML private Label labelProduitTop;
    @FXML private Label labelCategorieTop;
    @FXML private Label labelRuptures;
    @FXML private BarChart<String, Number> graphiqueBarres;
    @FXML private PieChart graphiqueCamembert;

    private final StatistiqueService statistiqueService = new StatistiqueServiceImpl();

    @FXML
    public void initialize() {
        // Période par défaut : les 6 derniers mois
        dateFin.setValue(LocalDate.now());
        dateDebut.setValue(LocalDate.now().minusMonths(6));
        actualiser();
    }

    @FXML
    private void actualiser() {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        if (debut == null || fin == null) {
            return;
        }

        chargerCartes(debut, fin);
        chargerGraphiqueBarres(debut, fin);
        chargerGraphiqueCamembert();
    }

    private void chargerCartes(LocalDate debut, LocalDate fin) {
        double valeur = statistiqueService.valeurTotaleStock();
        labelValeurStock.setText(String.format("%,.0f FCFA", valeur));

        labelProduitTop.setText(statistiqueService.produitLePlusMouvemente(debut, fin));
        labelCategorieTop.setText(statistiqueService.categoriePlusForteValeur());

        long ruptures = statistiqueService.nombreRupturesEvitees(debut, fin);
        labelRuptures.setText(String.valueOf(ruptures));
    }

    private void chargerGraphiqueBarres(LocalDate debut, LocalDate fin) {
        graphiqueBarres.getData().clear();

        Map<String, Long> donnees = statistiqueService.quantitesParMoisEtType(debut, fin);

        // On sépare les données combinées ("MM/yyyy_ENTRE") en deux séries distinctes
        Map<String, Long> entrees = new TreeMap<>();
        Map<String, Long> sorties = new TreeMap<>();

        for (Map.Entry<String, Long> entree : donnees.entrySet()) {
            String[] parties = entree.getKey().split("_");
            String mois = parties[0];
            String type = parties[1];

            if (type.equals("ENTRE")) {
                entrees.put(mois, entree.getValue());
            } else {
                sorties.put(mois, entree.getValue());
            }
        }

        XYChart.Series<String, Number> serieEntrees = new XYChart.Series<>();
        serieEntrees.setName("Entrées");
        entrees.forEach((mois, quantite) -> serieEntrees.getData().add(new XYChart.Data<>(mois, quantite)));

        XYChart.Series<String, Number> serieSorties = new XYChart.Series<>();
        serieSorties.setName("Sorties");
        sorties.forEach((mois, quantite) -> serieSorties.getData().add(new XYChart.Data<>(mois, quantite)));

        graphiqueBarres.getData().addAll(serieEntrees, serieSorties);
    }

    private void chargerGraphiqueCamembert() {
        Map<String, Double> donnees = statistiqueService.valeurStockParCategorie();

        graphiqueCamembert.setData(FXCollections.observableArrayList(
                donnees.entrySet().stream()
                        .map(entree -> new PieChart.Data(entree.getKey(), entree.getValue()))
                        .toList()
        ));
    }
}