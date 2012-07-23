package org.concordiainternational.competition.tests;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.webapp.WebApplicationConfiguration;

public class SharedTestSetup {

    protected void setUpTest() {
        EntityManagerFactory emf = WebApplicationConfiguration.getTestEntityManagerFactory();
        WebApplicationConfiguration.insertInitialData(emf, true);
        EntityManager entityManager = emf.createEntityManager();
        CompetitionApplication.setThreadLocals(emf,entityManager);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
    }

    protected void tearDownTest() {
        EntityManager entityManager = CompetitionApplication.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.getRollbackOnly()) {
            transaction.rollback();
        } else {
            transaction.commit();
        }
        entityManager.close();
    }

}
