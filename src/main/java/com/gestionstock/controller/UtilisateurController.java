package com.gestionstock.controller;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.service.UtilisateurService;
import com.gestionstock.service.UtilisateurServiceImpl;
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

import java.util.List;

public class UtilisateurController {

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, String> colonneNom;
    @FXML private TableColumn<Utilisateur, String> colonneEmail;
    @FXML private TableColumn<Utilisateur, String> colonneRole;
    @FXML private TableColumn<Utilisateur, String> colonneActif;

    private final UtilisateurService utilisateurService = new UtilisateurServiceImpl();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colonneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colonneEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colonneRole.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRole().toString()));
        colonneActif.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().isActif() ? "Actif" : "Désactivé"));
    }

    private void chargerDonnees() {
        List<Utilisateur> utilisateurs = utilisateurService.findAllUtilisateurs();
        ObservableList<Utilisateur> liste = FXCollections.observableArrayList(utilisateurs);
        tableUtilisateurs.setItems(liste);
    }

    @FXML
    private void ouvrirDialogueNouveau() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/AddUtilisateurDialog.fxml")
            );
            Parent racine = loader.load();

            AddUtilisateurController controleur = loader.getController();
            controleur.setSurEnregistrement(this::chargerDonnees);

            Stage dialogue = new Stage();
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setTitle("Nouvel utilisateur");
            dialogue.setScene(new Scene(racine));
            dialogue.showAndWait();

        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire.");
            e.printStackTrace();
        }
    }

    @FXML
    private void activerUtilisateur() {
        changerStatut(true);
    }

    @FXML
    private void desactiverUtilisateur() {
        changerStatut(false);
    }

    private void changerStatut(boolean actif) {
        Utilisateur selection = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Veuillez sélectionner un utilisateur.");
            return;
        }

        try {
            utilisateurService.activerDesactiver(selection.getId(), actif);
            chargerDonnees();
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de la mise à jour du statut.");
        }
    }

    private void afficherAlerte(Alert.AlertType type, String message) {
        Alert alerte = new Alert(type);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}