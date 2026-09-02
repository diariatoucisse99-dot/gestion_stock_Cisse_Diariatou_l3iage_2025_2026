package com.gestionstock.controller;

import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.service.MouvementService;
import com.gestionstock.service.MouvementServiceImpl;
import com.gestionstock.service.ProduitService;
import com.gestionstock.service.ProduitServiceImpl;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.gestionstock.util.SessionUtilisateur;
import com.gestionstock.model.Utilisateur;

public class AddMouvementController {

    @FXML private ComboBox<Produit> comboProduit;
    @FXML private RadioButton radioEntree;
    @FXML private RadioButton radioSortie;
    @FXML private TextField champQuantite;
    @FXML private TextField champMotif;
    @FXML private Label labelApercu;
    @FXML private Label labelErreur;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final MouvementService mouvementService = new MouvementServiceImpl();

    private Runnable surEnregistrement;

    public void setSurEnregistrement(Runnable callback) {
        this.surEnregistrement = callback;
    }

    @FXML
    public void initialize() {
        ToggleGroup groupe = new ToggleGroup();
        radioEntree.setToggleGroup(groupe);
        radioSortie.setToggleGroup(groupe);

        comboProduit.setItems(FXCollections.observableArrayList(produitService.findAllProduits()));

        ChangeListener<Object> majApercu = (obs, old, val) -> mettreAJourApercu();
        comboProduit.valueProperty().addListener(majApercu);
        champQuantite.textProperty().addListener(majApercu);
        radioEntree.selectedProperty().addListener(majApercu);
        radioSortie.selectedProperty().addListener(majApercu);
    }

    private void mettreAJourApercu() {
        Produit produit = comboProduit.getValue();
        if (produit == null || champQuantite.getText().isBlank()) {
            labelApercu.setText("");
            return;
        }
        try {
            int quantite = Integer.parseInt(champQuantite.getText().trim());
            int stockResultant = radioEntree.isSelected()
                    ? produit.getQuantiteStock() + quantite
                    : produit.getQuantiteStock() - quantite;
            labelApercu.setText("Stock actuel : " + produit.getQuantiteStock()
                    + "  →  Stock après mouvement : " + stockResultant);
        } catch (NumberFormatException e) {
            labelApercu.setText("");
        }
    }

    @FXML
    private void enregistrer() {
        labelErreur.setText("");

        Produit produit = comboProduit.getValue();
        if (produit == null) {
            labelErreur.setText("Veuillez sélectionner un produit.");
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(champQuantite.getText().trim());
        } catch (NumberFormatException e) {
            labelErreur.setText("La quantité doit être un nombre entier.");
            return;
        }

        TypeMouvement type = radioEntree.isSelected() ? TypeMouvement.ENTRE : TypeMouvement.SORTIE;
        String motif = champMotif.getText().trim();

        try {
            Long utilisateurId = SessionUtilisateur.getUtilisateurConnecte() != null
                    ? SessionUtilisateur.getUtilisateurConnecte().getId()
                    : null;

            mouvementService.enregistrerMouvement(produit.getId(), type, quantite, motif, utilisateurId);
            fermerFenetre();
        } catch (Exception e) {
            labelErreur.setText(e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) champQuantite.getScene().getWindow();
        stage.close();
    }
}