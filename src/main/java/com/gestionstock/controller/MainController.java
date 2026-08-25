package com.gestionstock.controller;

import com.gestionstock.util.SessionUtilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/*
    -@FXML: Annotation qui connecte un attribut Java à un composant déclaré dans le fichier XML via son fx:id
    -initialize(): méthode spéciale appelée automatiquement par JavaFx après le chargement du FXML
 */

public class MainController {
    @FXML
    private StackPane contenuPrincipale;

    @FXML
    private Label labelUtilisateurConnecte;

    @FXML
    public void initialize() {
        if (SessionUtilisateur.getUtilisateurConnecte() != null) {
            labelUtilisateurConnecte.setText(
                    "Connecté : " + SessionUtilisateur.getUtilisateurConnecte().getNom()
            );
        }
        afficherDashboard();
    }

    @FXML
    private void afficherDashboard() {
        contenuPrincipale.getChildren().clear();
        contenuPrincipale.getChildren().add(new Label("Dashboard"));
    }

    @FXML
    private void afficherProduits() {
        chargerVue("/com/gestionstock/produits.fxml");
    }

    @FXML
    private void afficherCategories() {
        chargerVue("/com/gestionstock/categories.fxml");
    }

    @FXML
    private void afficherFournisseurs() {
        chargerVue("/com/gestionstock/fournisseurs.fxml");
    }

    @FXML
    private void seDeconnecter() {
        SessionUtilisateur.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/LoginView.fxml")
            );
            Parent racine = loader.load();

            Stage stage = (Stage) contenuPrincipale.getScene().getWindow();
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionstock/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Gestion Stock IAGE - Connexion");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerVue(String cheminFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFxml));
            Node vue = loader.load();
            contenuPrincipale.getChildren().clear();
            contenuPrincipale.getChildren().add(vue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}