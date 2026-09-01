package com.gestionstock.service;

import com.gestionstock.model.Utilisateur;
import java.util.List;
import java.util.Optional;

public interface UtilisateurService {

    Optional<Utilisateur> authentifier(String email, String motDePasseClair);

    Utilisateur ajouter(Utilisateur utilisateur, String motDePasseClair);

    void activerDesactiver(Long id, boolean actif);

    List<Utilisateur> findAllUtilisateurs();
}