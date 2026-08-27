package com.gestionstock.controller;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MouvementController {

    @FXML private TableView<Mouvement> tableMouvements;
    @FXML private TableColumn<Mouvement, String> colonneDate;
    @FXML private TableColumn<Mouvement, String> colonneProduit;
    @FXML private TableColumn<Mouvement, String> colonneType;
    @FXML private TableColumn<Mouvement, Integer> colonneQuantite;
    @FXML private TableColumn<Mouvement, String> colonneMotif;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;

    private final MouvementService mouvementService = new MouvementServiceImpl();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurerColonnes();
        filtrerToutes();
    }

    private void configurerColonnes() {
        colonneDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateMouvement().format(FORMAT)));
        colonneProduit.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduit().getNom()));
        colonneType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType().toString()));
        colonneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colonneMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
    }

    private void afficher(List<Mouvement> mouvements) {
        ObservableList<Mouvement> liste = FXCollections.observableArrayList(mouvements);
        tableMouvements.setItems(liste);
    }

    @FXML
    private void filtrerToutes() {
        afficher(mouvementService.findAll());
    }

    @FXML
    private void filtrerEntrees() {
        afficher(mouvementService.findByType(TypeMouvement.ENTRE));
    }

    @FXML
    private void filtrerSorties() {
        afficher(mouvementService.findByType(TypeMouvement.SORTIE));
    }

    @FXML
    private void filtrerParPeriode() {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        if (debut == null || fin == null) {
            afficher(mouvementService.findAll());
            return;
        }

        afficher(mouvementService.findByPeriode(
                debut.atStartOfDay(),
                fin.atTime(23, 59, 59)
        ));
    }

    @FXML
    private void ouvrirDialogueNouveauMouvement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionstock/AddMouvementDialog.fxml")
            );
            Parent racine = loader.load();

            AddMouvementController controleur = loader.getController();
            controleur.setSurEnregistrement(this::filtrerToutes);

            Stage dialogue = new Stage();
            dialogue.initModality(Modality.APPLICATION_MODAL);
            dialogue.setTitle("Nouveau Mouvement");
            dialogue.setScene(new Scene(racine));
            dialogue.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}