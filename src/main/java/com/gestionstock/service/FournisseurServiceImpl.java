package com.gestionstock.service;

import com.gestionstock.model.Fournisseur;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class FournisseurServiceImpl implements FournisseurService {

    @Override
    public List<Fournisseur> findAllFournisseurs() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT f FROM Fournisseur f ORDER BY f.nom", Fournisseur.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Fournisseur> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Fournisseur.class, id));
        }
    }

    @Override
    public void addFournisseur(Fournisseur fournisseur) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(fournisseur);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateFournisseur(Fournisseur fournisseur) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(fournisseur);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la modification du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteFournisseur(int id) {
        if (compterProduitsRattaches(id) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce fournisseur : des produits y sont encore rattachés."
            );
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Fournisseur fournisseur = em.find(Fournisseur.class, id);
            if (fournisseur != null) {
                em.remove(fournisseur);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression du fournisseur");
        } finally {
            em.close();
        }
    }

    @Override
    public int compterProduitsRattaches(int fournisseurId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Long total = em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.fournisseur.id = :fourId", Long.class
            ).setParameter("fourId", fournisseurId).getSingleResult();
            return total.intValue();
        }
    }
}