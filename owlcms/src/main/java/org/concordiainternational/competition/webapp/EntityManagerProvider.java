package org.concordiainternational.competition.webapp;

import javax.persistence.EntityManager;

public interface EntityManagerProvider {

    EntityManager getEntityManager();

}