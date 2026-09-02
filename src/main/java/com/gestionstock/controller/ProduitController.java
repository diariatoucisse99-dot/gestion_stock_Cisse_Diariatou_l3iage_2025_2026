package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;

import java.util.List;
import java.util.Optional;

public class ProduitController {
    @FXML
    TableView<Produit> tableProduits;
    @FXML
    TableColumn<Produit, String> colonneNom; // CORRIGÉ : Integer -> String
    @FXML
    TableColumn<Produit, Double> colonnePrix;
    @FXML
    TableColumn<Produit, String> colonnePrixPromo;
    @FXML
    TableColumn<Produit, Integer> colonneStock;
    @FXML
    TableColumn<Produit, Integer> colonneStockMin;
    @FXML
    TableColumn<Produit, String> colonneCategorie;
    @FXML
    TableColumn<Produit, String> colonneFournisseur;
    @FXML
    TextField champRecherche;
    @FXML
    private ComboBox<Categorie> comboFiltreCategorie;
    @FXML
    private ComboBox<Fournisseur> comboFiltreFournisseur;
    @FXML
    private CheckBox checkStockBas;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final CategorieService categorieService = new CategorieServiceImpl();
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();

    // Liste complète chargée depuis la base, utilisée comme référence pour la recherche
    private ObservableList<Produit> listeProduits;

    @FXML
    public void initialize() {
        configurerColones();
        configurerAlerteStockBas();
        configurerFiltres();
        chargerDonnees();
    }

    private void configurerAlerteStockBas() {
        //lie le clic du CheckBox directement au filtre de stock bas
        if (checkStockBas != null) {
            checkStockBas.selectedProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        }
    }

    private void configurerFiltres() {
        comboFiltreCategorie.setItems(FXCollections.observableArrayList(categorieService.findAllCategories()));
        comboFiltreFournisseur.setItems(FXCollections.observableArrayList(fournisseurService.findAllFournisseurs()));

        comboFiltreCategorie.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
        comboFiltreFournisseur.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
    }

    @FXML
    private void appliquerFiltres() {
        Categorie categorieChoisie = comboFiltreCategorie.getValue();
        Fournisseur fournisseurChoisi = comboFiltreFournisseur.getValue();
        boolean stockBasUniquement = checkStockBas.isSelected();

        ObservableList<Produit> resultats = listeProduits.filtered(produit -> {
            boolean correspondCategorie = (categorieChoisie == null)
                    || (produit.getCategorie() != null && produit.getCategorie().getId() == categorieChoisie.getId());

            boolean correspondFournisseur = (fournisseurChoisi == null)
                    || (produit.getFournisseur() != null && produit.getFournisseur().getId() == fournisseurChoisi.getId());

            boolean correspondStockBas = !stockBasUniquement
                    || produit.getQuantiteStock() <= produit.getQuantiteMin();

            return correspondCategorie && correspondFournisseur && correspondStockBas;
        });

        tableProduits.setItems(resultats);
    }

    @FXML
    private void reinitialiserFiltres() {
        comboFiltreCategorie.setValue(null);
        comboFiltreFournisseur.setValue(null);
        checkStockBas.setSelected(false);
        tableProduits.setItems(listeProduits);
    }

    private void configurerColones() {
        tableProduits.setRowFactory(tv -> new javafx.scene.control.TableRow<Produit>() {
            @Override
            protected void updateItem(Produit produit, boolean vide) {
                super.updateItem(produit, vide);
                if (produit == null || vide) {
                    setStyle("");
                } else if (produit.getQuantiteStock() <= produit.getQuantiteMin()) {
                    setStyle("-fx-background-color: #ffe0e0;");
                } else {
                    setStyle("");
                }
            }
        });

        // Lier chaque colonne à un attribut de la classe Produit
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonnePrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        colonnePrixPromo.setCellValueFactory(data -> {
            Double promo = data.getValue().getPrixPromo();
            return new SimpleStringProperty(promo != null ? String.valueOf(promo) : "-");
        });

        colonneStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        colonneStockMin.setCellValueFactory(new PropertyValueFactory<>("quantiteMin"));

        colonneCategorie.setCellValueFactory(data -> {
            Categorie cat = data.getValue().getCategorie();
            return new SimpleStringProperty(cat != null ? cat.getNom() : "");
        });

        colonneFournisseur.setCellValueFactory(data -> {
            Fournisseur fournisseur = data.getValue().getFournisseur();
            return new SimpleStringProperty(fournisseur != null ? fournisseur.getNom() : "");
        });
    }

    private void chargerDonnees() {
        List<Produit> produits = produitService.findAllProduits();
        listeProduits = FXCollections.observableArrayList(produits);
        tableProduits.setItems(listeProduits);
    }

    @FXML
    private void rechercherProduits() {
        String recherche = champRecherche.getText();

        if (recherche == null || recherche.isBlank()) {
            tableProduits.setItems(listeProduits);
            return;
        }

        String rechercheMinuscule = recherche.trim().toLowerCase();

        ObservableList<Produit> resultats = listeProduits.filtered(produit ->
                (produit.getNom() != null && produit.getNom().toLowerCase().contains(rechercheMinuscule))
        );

        tableProduits.setItems(resultats);
    }

    @FXML
    private void ouvrirDialogueAjout() {
        ouvrirDialogueProduit(null);
    }

    @FXML
    private void ouvrirDialogueModification() {
        Produit selection = tableProduits.getSelectionModel().getSelectedItem();
        if (selection == null) {
            Alert alerte = new Alert(Alert.AlertType.INFORMATION);
            alerte.setHeaderText(null);
            alerte.setContentText("Veuillez sélectionner un produit à modifier.");
            alerte.showAndWait();
            return;
        }
        ouvrirDialogueProduit(selection);
    }

    private void ouvrirDialogueProduit(Produit produitAModifier) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/AddProduitDialog.fxml")
            );
            Parent racine = loader.load();

            AddProduitController controleur = loader.getController();
            controleur.setSurEnregistrement(this::chargerDonnees);
            if (produitAModifier != null) {
                controleur.chargerPourModification(produitAModifier);
            }

            Stage dialogue = new Stage();
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setTitle(produitAModifier == null ? "Nouveau Produit" : "Modifier le Produit");
            dialogue.setScene(new Scene(racine));
            dialogue.showAndWait();

        } catch (Exception e) {
            Alert erreur = new Alert(Alert.AlertType.ERROR);
            erreur.setHeaderText(null);
            erreur.setContentText("Impossible d'ouvrir le formulaire produit.");
            erreur.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerProduit() {
        Produit produitSelectionne = tableProduits.getSelectionModel().getSelectedItem();

        if (produitSelectionne == null) {
            Alert alerteInfo = new Alert(Alert.AlertType.INFORMATION);
            alerteInfo.setTitle("Aucune sélection");
            alerteInfo.setHeaderText(null);
            alerteInfo.setContentText("Veuillez sélectionner un produit à supprimer.");
            alerteInfo.showAndWait();
            return;
        }

        Alert alerteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        alerteConfirmation.setTitle("Confirmation de suppression");
        alerteConfirmation.setHeaderText(null);
        alerteConfirmation.setContentText("Voulez-vous vraiment supprimer le produit \"" + produitSelectionne.getNom() + "\" ?");

        Optional<ButtonType> reponse = alerteConfirmation.showAndWait();

        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            produitService.deleteProduit(produitSelectionne.getId());
            chargerDonnees();
        }
    }
}