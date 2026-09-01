package com.gestionstock.service;

import com.gestionstock.model.Utilisateur;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

import java.util.Optional;

public class UtilisateurServiceImpl implements UtilisateurService {

    @Override
    public Optional<Utilisateur> authentifier(String email, String motDePasseClair) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Utilisateur> query = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class);
            query.setParameter("email", email);
            Utilisateur utilisateur = query.getResultStream().findFirst().orElse(null);

            if (utilisateur == null || !utilisateur.isActif()) {
                return Optional.empty();
            }

            boolean motDePasseValide = BCrypt.checkpw(motDePasseClair, utilisateur.getMotDePasseHash());
            return motDePasseValide ? Optional.of(utilisateur) : Optional.empty();

        } finally {
            em.close();
        }
    }

    @Override
    public Utilisateur ajouter(Utilisateur utilisateur, String motDePasseClair) {
        String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());
        utilisateur.setMotDePasseHash(hash);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(utilisateur);
            em.getTransaction().commit();
            return utilisateur;
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void activerDesactiver(Long id, boolean actif) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Utilisateur u = em.find(Utilisateur.class, id);
            if (u != null) {
                u.setActif(actif);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Utilisateur> findAllUtilisateurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT u FROM Utilisateur u ORDER BY u.nom", Utilisateur.class
            ).getResultList();
        }
    }
}