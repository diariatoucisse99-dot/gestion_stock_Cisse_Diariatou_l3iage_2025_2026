package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.model.enums.RoleUtilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddUtilisateurController {

    @FXML private TextField champNom;
    @FXML private TextField champEmail;
    @FXML private PasswordField champMotDePasse;
    @FXML private ComboBox<RoleUtilisateur> comboRole;
    @FXML private Label labelErreur;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();
    private Runnable surEnregistrement;

    public void setSurEnregistrement(Runnable callback) {
        this.surEnregistrement = callback;
    }

    @FXML
    public void initialize() {
        comboRole.setItems(FXCollections.observableArrayList(RoleUtilisateur.values()));
        comboRole.getSelectionModel().selectFirst();
    }

    @FXML
    private void enregistrer() {
        String nom = champNom.getText().trim();
        String email = champEmail.getText().trim();
        String motDePasse = champMotDePasse.getText();
        RoleUtilisateur role = comboRole.getValue();

        if (nom.length() < 2) {
            labelErreur.setText("Le nom doit contenir au moins 2 caractères.");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            labelErreur.setText("Email invalide.");
            return;
        }
        if (motDePasse.length() < 6) {
            labelErreur.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (role == null) {
            labelErreur.setText("Veuillez choisir un rôle.");
            return;
        }

        try {
            Utilisateur nouveau = new Utilisateur();
            nouveau.setNom(nom);
            nouveau.setEmail(email);
            nouveau.setRole(role);
            // Le hachage BCrypt est fait à l'intérieur du service, pas ici
            utilisateurService.ajouter(nouveau, motDePasse);

            if (surEnregistrement != null) {
                surEnregistrement.run();
            }
            fermerFenetre();

        } catch (Exception e) {
            labelErreur.setText("Erreur : cet email existe peut-être déjà.");
        }
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) champNom.getScene().getWindow();
        stage.close();
    }
}