package com.gestionstock.controller;

import com.gestionstock.model.Categorie;
import com.gestionstock.model.Fournisseur;
import com.gestionstock.model.Produit;
import com.gestionstock.service.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddProduitController {

    @FXML private Label labelTitre;
    @FXML private TextField champNom;
    @FXML private ComboBox<Categorie> comboCategorie;
    @FXML private ComboBox<Fournisseur> comboFournisseur;
    @FXML private TextField champPrix;
    @FXML private TextField champPrixPromo;
    @FXML private TextField champQuantiteStock;
    @FXML private TextField champQuantiteMin;
    @FXML private Label labelErreur;

    private final ProduitService produitService = new ProduitServiceImpl();
    private final CategorieService categorieService = new CategorieServiceImpl();
    private final FournisseurService fournisseurService = new FournisseurServiceImpl();

    // Si null : mode "ajout". Sinon : on modifie ce produit précis.
    private Produit produitEnEdition;
    private Runnable surEnregistrement;

    public void setSurEnregistrement(Runnable callback) {
        this.surEnregistrement = callback;
    }

    @FXML
    public void initialize() {
        comboCategorie.setItems(FXCollections.observableArrayList(categorieService.findAllCategories()));
        comboFournisseur.setItems(FXCollections.observableArrayList(fournisseurService.findAllFournisseurs()));
    }

    // Appelée depuis ProduitController pour préremplir le formulaire en mode modification
    public void chargerPourModification(Produit produit) {
        this.produitEnEdition = produit;
        labelTitre.setText("Modifier le produit");

        champNom.setText(produit.getNom());
        comboCategorie.setValue(produit.getCategorie());
        comboFournisseur.setValue(produit.getFournisseur());
        champPrix.setText(String.valueOf(produit.getPrix()));
        champPrixPromo.setText(produit.getPrixPromo() != null ? String.valueOf(produit.getPrixPromo()) : "");
        champQuantiteStock.setText(String.valueOf(produit.getQuantiteStock()));
        champQuantiteMin.setText(String.valueOf(produit.getQuantiteMin()));
    }

    @FXML
    private void enregistrer() {
        labelErreur.setText("");

        String nom = champNom.getText().trim();
        if (nom.length() < 2) {
            labelErreur.setText("Le nom doit contenir au moins 2 caractères.");
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(champPrix.getText().trim());
            if (prix <= 0) {
                labelErreur.setText("Le prix doit être positif.");
                return;
            }
        } catch (NumberFormatException e) {
            labelErreur.setText("Le prix doit être un nombre valide.");
            return;
        }

        Double prixPromo = null;
        String texteProxPromo = champPrixPromo.getText().trim();
        if (!texteProxPromo.isEmpty()) {
            try {
                prixPromo = Double.parseDouble(texteProxPromo);
                if (prixPromo <= 0 || prixPromo >= prix) {
                    labelErreur.setText("Le prix promo doit être positif et inférieur au prix normal.");
                    return;
                }
            } catch (NumberFormatException e) {
                labelErreur.setText("Le prix promo doit être un nombre valide.");
                return;
            }
        }

        int quantiteStock, quantiteMin;
        try {
            quantiteStock = Integer.parseInt(champQuantiteStock.getText().trim());
            quantiteMin = Integer.parseInt(champQuantiteMin.getText().trim());
            if (quantiteStock < 0 || quantiteMin < 0) {
                labelErreur.setText("Les quantités doivent être des entiers positifs ou nuls.");
                return;
            }
        } catch (NumberFormatException e) {
            labelErreur.setText("Les quantités doivent être des nombres entiers.");
            return;
        }

        Categorie categorie = comboCategorie.getValue();
        Fournisseur fournisseur = comboFournisseur.getValue();
        if (categorie == null || fournisseur == null) {
            labelErreur.setText("Veuillez sélectionner une catégorie et un fournisseur.");
            return;
        }

        try {
            if (produitEnEdition == null) {
                Produit nouveau = new Produit(nom, quantiteStock, quantiteMin, prix, categorie, fournisseur);
                nouveau.setPrixPromo(prixPromo);
                produitService.addProduit(nouveau);
            } else {
                produitEnEdition.setNom(nom);
                produitEnEdition.setPrix(prix);
                produitEnEdition.setPrixPromo(prixPromo);
                produitEnEdition.setQuantiteStock(quantiteStock);
                produitEnEdition.setQuantiteMin(quantiteMin);
                produitEnEdition.setCategorie(categorie);
                produitEnEdition.setFournisseur(fournisseur);
                produitService.updateProduit(produitEnEdition);
            }

            if (surEnregistrement != null) {
                surEnregistrement.run();
            }
            fermerFenetre();

        } catch (Exception e) {
            labelErreur.setText("Erreur lors de l'enregistrement : " + e.getMessage());
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