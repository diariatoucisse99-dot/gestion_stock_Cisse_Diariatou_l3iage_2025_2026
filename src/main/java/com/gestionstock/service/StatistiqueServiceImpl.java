package com.gestionstock.service;

import com.gestionstock.model.Mouvement;
import com.gestionstock.model.enums.TypeMouvement;
import com.gestionstock.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatistiqueServiceImpl implements StatistiqueService {

    @Override
    public double valeurTotaleStock() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Double resultat = em.createQuery(
                    "SELECT SUM(p.quantiteStock * p.prix) FROM Produit p", Double.class
            ).getSingleResult();
            return resultat != null ? resultat : 0.0;
        }
    }

    @Override
    public String produitLePlusMouvemente(LocalDate debut, LocalDate fin) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Tuple> resultats = em.createQuery(
                            "SELECT m.produit.nom AS nom, SUM(m.quantite) AS total " +
                                    "FROM Mouvement m " +
                                    "WHERE m.dateMouvement BETWEEN :debut AND :fin " +
                                    "GROUP BY m.produit.nom " +
                                    "ORDER BY total DESC", Tuple.class
                    ).setParameter("debut", debut.atStartOfDay())
                    .setParameter("fin", fin.atTime(23, 59, 59))
                    .setMaxResults(1)
                    .getResultList();

            if (resultats.isEmpty()) {
                return "Aucun mouvement sur cette période";
            }
            return resultats.get(0).get("nom", String.class);
        }
    }

    @Override
    public String categoriePlusForteValeur() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Tuple> resultats = em.createQuery(
                    "SELECT c.nom AS nom, SUM(p.quantiteStock * p.prix) AS valeur " +
                            "FROM Produit p JOIN p.categorie c " +
                            "GROUP BY c.nom " +
                            "ORDER BY valeur DESC", Tuple.class
            ).setMaxResults(1).getResultList();

            if (resultats.isEmpty()) {
                return "Aucune donnée";
            }
            return resultats.get(0).get("nom", String.class);
        }
    }

    @Override
    public String fournisseurAvecPlusDeProduits() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Tuple> resultats = em.createQuery(
                    "SELECT f.nom AS nom, COUNT(p) AS total " +
                            "FROM Produit p JOIN p.fournisseur f " +
                            "GROUP BY f.nom " +
                            "ORDER BY total DESC", Tuple.class
            ).setMaxResults(1).getResultList();

            if (resultats.isEmpty()) {
                return "Aucune donnée";
            }
            return resultats.get(0).get("nom", String.class);
        }
    }

    @Override
    public long nombreRupturesEvitees(LocalDate debut, LocalDate fin) {
        // Une "rupture évitée de justesse" = une sortie qui a fait passer
        // le produit sous son seuil minimum (quantiteStock <= quantiteMin après coup)
        try (EntityManager em = JPAUtil.getEntityManager()) {
            Long total = em.createQuery(
                            "SELECT COUNT(m) FROM Mouvement m " +
                                    "WHERE m.type = :type " +
                                    "AND m.dateMouvement BETWEEN :debut AND :fin " +
                                    "AND m.produit.quantiteStock <= m.produit.quantiteMin",
                            Long.class
                    ).setParameter("type", TypeMouvement.SORTIE)
                    .setParameter("debut", debut.atStartOfDay())
                    .setParameter("fin", fin.atTime(23, 59, 59))
                    .getSingleResult();
            return total != null ? total : 0L;
        }
    }

    @Override
    public Map<String, Long> quantitesParMoisEtType(LocalDate debut, LocalDate fin) {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Mouvement> mouvements = em.createQuery(
                            "SELECT m FROM Mouvement m " +
                                    "WHERE m.dateMouvement BETWEEN :debut AND :fin " +
                                    "ORDER BY m.dateMouvement", Mouvement.class
                    ).setParameter("debut", debut.atStartOfDay())
                    .setParameter("fin", fin.atTime(23, 59, 59))
                    .getResultList();

            DateTimeFormatter formatMois = DateTimeFormatter.ofPattern("MM/yyyy");
            Map<String, Long> resultat = new LinkedHashMap<>();

            for (Mouvement m : mouvements) {
                String mois = m.getDateMouvement().format(formatMois);
                String cle = mois + "_" + m.getType();
                resultat.merge(cle, (long) m.getQuantite(), Long::sum);
            }
            return resultat;
        }
    }

    @Override
    public Map<String, Double> valeurStockParCategorie() {
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Tuple> resultats = em.createQuery(
                    "SELECT c.nom AS nom, SUM(p.quantiteStock * p.prix) AS valeur " +
                            "FROM Produit p JOIN p.categorie c " +
                            "GROUP BY c.nom", Tuple.class
            ).getResultList();

            Map<String, Double> carte = new LinkedHashMap<>();
            for (Tuple t : resultats) {
                carte.put(t.get("nom", String.class), t.get("valeur", Double.class));
            }
            return carte;
        }
    }
}