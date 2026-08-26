package com.gestionstock.service;

import com.gestionstock.model.Categorie;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class CategorieServiceImpl implements CategorieService {

    @Override
    public List<Categorie> findAllCategories() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class
            ).getResultList();
        }
    }

    @Override
    public Optional<Categorie> findById(int id) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            return Optional.ofNullable(em.find(Categorie.class, id));
        }
    }

    @Override
    public void addCategorie(Categorie categorie) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(categorie);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateCategorie(Categorie categorie) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(categorie);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la modification de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteCategorie(int id) {
        if (compterProduitsRattaches(id) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer cette catégorie : des produits y sont encore rattachés."
            );
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                em.remove(categorie);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace(); //je verifie au cas ou ya pas de produit rattache pour supp
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression de la catégorie");
        } finally {
            em.close();
        }
    }

    @Override
    public int compterProduitsRattaches(int categorieId) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Long total = em.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.categorie.id = :catId", Long.class
            ).setParameter("catId", categorieId).getSingleResult();
            return total.intValue();
        }
    }
}