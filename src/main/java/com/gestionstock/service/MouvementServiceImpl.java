package com.gestionstock.service;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.Produit;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import com.gestionstock.model.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;

public class MouvementServiceImpl implements MouvementService {

    @Override
    public List<Mouvement> findAll() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "LEFT JOIN FETCH m.produit " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).getResultList();
        }
    }

    @Override
    public List<Mouvement> findByProduit(int produitId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "WHERE m.produit.id = :produitId " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).setParameter("produitId", produitId).getResultList();
        }
    }

    @Override
    public List<Mouvement> findByType(TypeMouvement type) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "WHERE m.type = :type " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).setParameter("type", type).getResultList();
        }
    }

    @Override
    public List<Mouvement> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT m FROM Mouvement m " +
                            "WHERE m.dateMouvement BETWEEN :debut AND :fin " +
                            "ORDER BY m.dateMouvement DESC",
                    Mouvement.class
            ).setParameter("debut", debut).setParameter("fin", fin).getResultList();
        }
    }

    @Override
    public void enregistrerMouvement(int produitId, TypeMouvement type, int quantite, String motif) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive.");
        }
        if (type == TypeMouvement.SORTIE && (motif == null || motif.isBlank())) {
            throw new IllegalArgumentException("Le motif est obligatoire pour une sortie de stock.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, produitId);
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable.");
            }

            if (type == TypeMouvement.ENTRE) {
                produit.setQuantiteStock(produit.getQuantiteStock() + quantite);
            } else {
                if (quantite > produit.getQuantiteStock()) {
                    throw new IllegalStateException(
                            "Stock insuffisant : disponible = " + produit.getQuantiteStock() + ", demandé = " + quantite
                    );
                }
                produit.setQuantiteStock(produit.getQuantiteStock() - quantite);
            }

            Mouvement mouvement = new Mouvement();
            mouvement.setProduit(produit);
            mouvement.setType(type);
            mouvement.setQuantite(quantite);
            mouvement.setMotif(motif);
            mouvement.setDateMouvement(LocalDateTime.now());

            em.merge(produit);
            em.persist(mouvement);

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    @Override
    public void enregistrerMouvement(int produitId, TypeMouvement type, int quantite, String motif, Long utilisateurId) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être strictement positive.");
        }
        if (type == TypeMouvement.SORTIE && (motif == null || motif.isBlank())) {
            throw new IllegalArgumentException("Le motif est obligatoire pour une sortie de stock.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, produitId);
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable.");
            }

            if (type == TypeMouvement.ENTRE) {
                produit.setQuantiteStock(produit.getQuantiteStock() + quantite);
            } else {
                if (quantite > produit.getQuantiteStock()) {
                    throw new IllegalStateException(
                            "Stock insuffisant : disponible = " + produit.getQuantiteStock() + ", demandé = " + quantite
                    );
                }
                produit.setQuantiteStock(produit.getQuantiteStock() - quantite);
            }

            Mouvement mouvement = new Mouvement();
            mouvement.setProduit(produit);
            mouvement.setType(type);
            mouvement.setQuantite(quantite);
            mouvement.setMotif(motif);
            mouvement.setDateMouvement(LocalDateTime.now());

            if (utilisateurId != null) {
                Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
                mouvement.setUtilisateur(utilisateur);
            }

            em.merge(produit);
            em.persist(mouvement);

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}