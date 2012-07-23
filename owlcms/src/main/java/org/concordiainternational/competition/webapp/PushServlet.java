/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.webapp;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.icepush.servlet.MainServlet;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

@SuppressWarnings("serial")
public class PushServlet extends ApplicationServlet {

    private MainServlet pushServlet;
    private EntityManagerFactory emf = null;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        pushServlet = new MainServlet(servletConfig.getServletContext());
        emf = WebApplicationConfiguration.getPersistentEntityManagerFactory();
        WebApplicationConfiguration.insertInitialData(emf, false);
    }

    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().endsWith(".icepush")) {
            // delegate the Push request to the IcePush servlet
            try {
                pushServlet.service(request, response);
            } catch (ServletException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Vaadin request, executed in the context of a transaction.
            EntityManager em = null;
            EntityTransaction transaction = null;
            try {
                em = emf.createEntityManager();
                CompetitionApplication.setThreadLocals(emf,em);
                transaction = em.getTransaction();
                
                // service is executed in a transaction, no need to commit.
                // service can call setRollbackOnly() to prevent commit().
                transaction.begin();
                super.service(request, response);
            } finally {
                CompetitionApplication.removeThreadLocals();
                if (transaction != null && transaction.isActive()) {
                    if (transaction.getRollbackOnly()) {
                        transaction.rollback();
                    } else {
                        transaction.commit();
                    }
                }
                if (em != null) {
                    em.close();
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        pushServlet.shutdown();
    }

    @Override
    protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri, HttpServletRequest req) throws IOException {
        super.writeAjaxPageHtmlHeader(page, title, themeUri, req);
        
        // REFACTOR: detect browser
        // the following is mobile safari specific (iPod).  does not harm other browsers.
        // there is no easy way to make this conditional without overriding the whole 
        // writeAjaxPage method, which is not worth it at this stage.
//        page.write("<meta name='viewport' content='width=device-width' />");
//        page.write("<meta name='apple-mobile-web-app-capable' content='yes' />");
        
        page.append("<meta name=\"viewport\" content="
                + "\"user-scalable=no, width=device-width, "
                + "initial-scale=1.0, maximum-scale=1.0;\" />");
        page.append("<meta name=\"apple-touch-fullscreen\" content=\"yes\" />");
        page.append("<meta name=\"apple-mobile-web-app-capable\" "
                + "content=\"yes\" />");
        page.append("<meta name=\"apple-mobile-web-app-status-bar-style\" "
                + "content=\"black\" />");
        page.append("<link rel=\"apple-touch-icon\" "
                + "href=\"TouchKit/VAADIN/themes/touch/img/icon.png\" />");
        page.append("<link rel=\"apple-touch-startup-image\" "
                + "href=\"TouchKit/VAADIN/themes/touch/img/startup.png\" />");
    }
}
