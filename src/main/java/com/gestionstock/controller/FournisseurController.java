package com.gestionstock.controller;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.service.FournisseurService;
import com.gestionstock.service.FournisseurServiceImpl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.regex.Pattern;

public class FournisseurController {

    @FXML private TextField champNom;
    @FXML private TextField champEmail;
    @FXML private TextField champTel;
    @FXML private Label labelMessage;

    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, String> colonneNom;
    @FXML private TableColumn<Fournisseur, String> colonneEmail;
    @FXML private TableColumn<Fournisseur, String> colonneTel;
    @FXML private TableColumn<Fournisseur, Integer> colonneNbProduits;

    private final FournisseurService fournisseurService = new FournisseurServiceImpl();
    private ObservableList<Fournisseur> listeFournisseurs;
    private Fournisseur fournisseurEnEdition = null;

    // Numéro sénégalais : 9 chiffres commençant par 77, 78, 75, 76 ou 70
    private static final Pattern PATTERN_TEL = Pattern.compile("^(77|78|75|76|70)\\d{7}$");
    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colonneNbProduits.setCellValueFactory(data -> {
            int nb = fournisseurService.compterProduitsRattaches(data.getValue().getId());
            return new SimpleIntegerProperty(nb).asObject();
        });
    }

    private void chargerDonnees() {
        List<Fournisseur> fournisseurs = fournisseurService.findAllFournisseurs();
        listeFournisseurs = FXCollections.observableArrayList(fournisseurs);
        tableFournisseurs.setItems(listeFournisseurs);
    }

    @FXML
    private void enregistrerFournisseur() {
        String nom = champNom.getText().trim();
        String email = champEmail.getText().trim();
        String tel = champTel.getText().trim();

        if (nom.length() < 2) {
            labelMessage.setText("Le nom doit contenir au moins 2 caractères.");
            return;
        }
        if (!email.isEmpty() && !PATTERN_EMAIL.matcher(email).matches()) {
            labelMessage.setText("Format d'email invalide.");
            return;
        }
        if (!tel.isEmpty() && !PATTERN_TEL.matcher(tel).matches()) {
            labelMessage.setText("Téléphone invalide : 9 chiffres commençant par 77, 78, 75, 76 ou 70.");
            return;
        }

        try {
            if (fournisseurEnEdition == null) {
                Fournisseur nouveau = new Fournisseur();
                nouveau.setNom(nom);
                nouveau.setEmail(email);
                nouveau.setTel(tel);
                fournisseurService.addFournisseur(nouveau);
            } else {
                fournisseurEnEdition.setNom(nom);
                fournisseurEnEdition.setEmail(email);
                fournisseurEnEdition.setTel(tel);
                fournisseurService.updateFournisseur(fournisseurEnEdition);
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
        Fournisseur selection = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            labelMessage.setText("Sélectionnez d'abord un fournisseur.");
            return;
        }
        fournisseurEnEdition = selection;
        champNom.setText(selection.getNom());
        champEmail.setText(selection.getEmail());
        champTel.setText(selection.getTel());
    }

    @FXML
    private void annulerEdition() {
        fournisseurEnEdition = null;
        champNom.clear();
        champEmail.clear();
        champTel.clear();
    }

    @FXML
    private void supprimerFournisseur() {
        Fournisseur selection = tableFournisseurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Veuillez sélectionner un fournisseur à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer le fournisseur \"" + selection.getNom() + "\" ?");
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                try {
                    fournisseurService.deleteFournisseur(selection.getId());
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