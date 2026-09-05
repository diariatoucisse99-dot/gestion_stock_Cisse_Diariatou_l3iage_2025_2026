package com.gestionstock.controller;

import com.gestionstock.util.SessionUtilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/*
    -@FXML: Annotation qui connecte un attribut Java à un composant déclaré dans le fichier XML via son fx:id
    -initialize(): méthode spéciale appelée automatiquement par JavaFx après le chargement du FXML
 */

public class MainController {
    @FXML
    private Button btnUtilisateurs;

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
            btnUtilisateurs.setVisible(SessionUtilisateur.estAdmin());
            btnUtilisateurs.setManaged(SessionUtilisateur.estAdmin());
        }
        afficherDashboard();
    }

    @FXML
    private void afficherDashboard() {
        chargerVue("/com/gestionstock/dashboard.fxml");
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
    private void afficherMouvements() {chargerVue("/com/gestionstock/mouvements.fxml");}

    @FXML
    private void afficherStatistiques() {chargerVue("/com/gestionstock/statistiques.fxml");}

    @FXML
    private void afficherUtilisateurs() {
        chargerVue("/com/gestionstock/utilisateurs.fxml");
    }
    @FXML
    private void seDeconnecter() {
        SessionUtilisateur.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/LoginView.fxml")
            );
            Parent racine = loader.load(); // charge le fichier xml et recupere lement racine

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