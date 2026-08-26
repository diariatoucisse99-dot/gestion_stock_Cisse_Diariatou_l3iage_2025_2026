package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.service.CategorieService;
import com.gestionstock.service.CategorieServiceImpl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CategorieController {

    @FXML private TextField champNom;
    @FXML private TextField champDescription;
    @FXML private Label labelMessage;

    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colonneNom;
    @FXML private TableColumn<Categorie, String> colonneDescription;
    @FXML private TableColumn<Categorie, Integer> colonneNbProduits;

    private final CategorieService categorieService = new CategorieServiceImpl();
    private ObservableList<Categorie> listeCategories;

    // Si null : on est en mode "ajout". Sinon : on modifie cette catégorie précise.
    private Categorie categorieEnEdition = null;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colonneNbProduits.setCellValueFactory(data -> {
            int nb = categorieService.compterProduitsRattaches(data.getValue().getId());
            return new SimpleIntegerProperty(nb).asObject();
        });
    }

    private void chargerDonnees() {
        List<Categorie> categories = categorieService.findAllCategories();
        listeCategories = FXCollections.observableArrayList(categories);
        tableCategories.setItems(listeCategories);
    }

    @FXML
    private void enregistrerCategorie() {
        String nom = champNom.getText().trim();
        String description = champDescription.getText().trim();

        if (nom.length() < 2) {
            labelMessage.setText("Le nom doit contenir au moins 2 caractères.");
            return;
        }

        try {
            if (categorieEnEdition == null) {
                Categorie nouvelle = new Categorie();
                nouvelle.setNom(nom);
                nouvelle.setDescription(description);
                categorieService.addCategorie(nouvelle);
            } else {
                categorieEnEdition.setNom(nom);
                categorieEnEdition.setDescription(description);
                categorieService.updateCategorie(categorieEnEdition);
            }
            labelMessage.setText("");
            annulerEdition();
            chargerDonnees();

        } catch (Exception e) {
            labelMessage.setText("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void chargerPourModification() {
        Categorie selection = tableCategories.getSelectionModel().getSelectedItem();
        if (selection == null) {
            labelMessage.setText("Sélectionnez d'abord une catégorie.");
            return;
        }
        categorieEnEdition = selection;
        champNom.setText(selection.getNom());
        champDescription.setText(selection.getDescription());
    }

    @FXML
    private void annulerEdition() {
        categorieEnEdition = null;
        champNom.clear();
        champDescription.clear();
    }

    @FXML
    private void supprimerCategorie() {
        Categorie selection = tableCategories.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer la catégorie \"" + selection.getNom() + "\" ?");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                try {
                    categorieService.deleteCategorie(selection.getId());
                    chargerDonnees();
                } catch (IllegalStateException e) {
                    afficherAlerte(Alert.AlertType.WARNING, e.getMessage());
                } catch (Exception e) {
                    afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de la suppression.");
                }
            }
        });
    }

    private void afficherAlerte(Alert.AlertType type, String message) {
        Alert alerte = new Alert(type);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}