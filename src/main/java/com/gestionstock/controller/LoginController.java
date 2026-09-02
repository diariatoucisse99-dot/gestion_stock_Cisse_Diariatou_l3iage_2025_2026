package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
import com.gestionstock.util.SessionUtilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField champEmail;

    @FXML
    private PasswordField champMotDePasse;

    @FXML
    private Label labelErreur;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @FXML
    private void seConnecter() {
        String email = champEmail.getText().trim();
        String motDePasse = champMotDePasse.getText();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        Optional<Utilisateur> resultat = utilisateurService.authentifier(email, motDePasse);

        if (resultat.isEmpty()) {
            labelErreur.setText("Email ou mot de passe incorrect, ou compte désactivé.");
            champMotDePasse.clear();
            return;
        }
    //pour memoriser tout ceux qui sont connecter

        SessionUtilisateur.connecter(resultat.get());
        ouvrirMenuPrincipal();
    }

    private void ouvrirMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/main.fxml")
            );
            Parent racine = loader.load();

            /* ici on récupère la Stage via
                 champEmail pour remplacer la scène de login
                par le menu principal,
                sans ouvrir une nouvelle fenêtre
             */
            Stage stage = (Stage) champEmail.getScene().getWindow();
            Scene scene = new Scene(racine);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionstock/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Gestion Stock IAGE");

        } catch (IOException e) {
            labelErreur.setText("Erreur lors du chargement de l'application.");
            e.printStackTrace();
        }
    }
}