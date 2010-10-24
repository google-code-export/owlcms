package org.concordiainternational.competition.webapp;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.icepush.servlet.MainServlet;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

@SuppressWarnings("serial")
public class PushServlet extends ApplicationServlet {

    private MainServlet pushServlet;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        pushServlet = new MainServlet(servletConfig.getServletContext());
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
            // Vaadin request
            super.service(request, response);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        pushServlet.shutdown();
    }

    @Override
    protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri) throws IOException {
        super.writeAjaxPageHtmlHeader(page, title, themeUri);
        
        // REFACTOR: detect browser
        // the following is mobile safari specific (iPod).  does not harm other browsers.
        // there is no easy way to make this conditional without overriding the whole 
        // writeAjaxPage method, which is not worth it at this stage.
        page.write("<meta name='viewport' content='width=device-width' />");
        page.write("<meta name='apple-mobile-web-app-capable' content='yes' />");
    }
}
