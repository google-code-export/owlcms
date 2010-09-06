/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.concordiainternational.competition.spreadsheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.concordiainternational.competition.data.Lifter;
import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.SystemError;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;

/**
 * @author IT Mill
 * @author jflamy - adapted from example in the Book of Vaadin.
 * 
 */
public class SpreadsheetUploader extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener,
        Upload.Receiver, ApplicationView {
    private static final long serialVersionUID = 6843262937708809785L;
    final private static Logger logger = LoggerFactory.getLogger(SpreadsheetUploader.class);

    Panel root; // Root element for contained components.
    Panel resultPanel; // Panel that contains the uploaded image.
    File file; // File to write to.

    private CompetitionApplication app;

    private Label status;

    private Locale locale;
    private String viewName;

    public SpreadsheetUploader(boolean initFromFragment, String viewName) {
        if (initFromFragment) {
            setParametersFromFragment();
        } else {
            this.viewName = viewName;
        }
        
        this.app = CompetitionApplication.getCurrent();
        this.locale = app.getLocale();

        root = new Panel(Messages.getString("SpreadsheetUploader.SpreadsheetUpload", locale)); //$NON-NLS-1$
        setCompositionRoot(root);

        // Create the Upload component.
        //final Upload upload = new Upload(Messages.getString("SpreadsheetUploader.ChooseFile",locale), this); //$NON-NLS-1$
        final Upload upload = new Upload("", this); //$NON-NLS-1$
        upload.setImmediate(true); // start immediately as soon as the file is
                                   // selected.

        // Use a custom button caption instead of plain "Upload".
        upload.setButtonCaption(Messages.getString("SpreadsheetUploader.UploadNow", locale)); //$NON-NLS-1$

        // Listen for events regarding the success of upload.
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        root.addComponent(upload);
        root.addComponent(new Label());

        // Create a panel for displaying the uploaded file.
        resultPanel = new Panel();
        status = new Label(Messages.getString("SpreadsheetUploader.NoSpreadsheetUploadedYet", locale)); //$NON-NLS-1$
        resultPanel.addComponent(status);
        root.addComponent(resultPanel);
    }

    // Callback method to begin receiving the upload.
    public OutputStream receiveUpload(String filename, String MIMEType) {
        FileOutputStream fos = null; // Output stream to write to
        try {
            ServletContext sCtx = app.getServletContext();
            String dirName = sCtx.getRealPath("registration"); //$NON-NLS-1$
            File dir = new File(dirName);
            logger.debug(dir.getAbsolutePath());

            if (!dir.exists()) dir.mkdirs();
            File longPath = new File(filename);
            filename = longPath.getName();
            file = new File(dir, filename);
            logger.debug("writing to {}", file.getAbsolutePath()); //$NON-NLS-1$

            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            this.setComponentError(new SystemError(e));
            throw new SystemError(e);
        }
        return fos; // Return the output stream to write to
    }

    // This is called if the upload is finished.
    public void uploadSucceeded(Upload.SucceededEvent event) {
        // Log the upload on screen.
        final String messageFormat = Messages.getString("SpreadsheetUploader.Status", locale); //$NON-NLS-1$
        final String mimeType = event.getMIMEType();
        status.setValue(MessageFormat.format(messageFormat, event.getFilename(), mimeType));
        processUploadedFile();
    }

    /**
     * @throws SystemError
     */
    private void processUploadedFile() throws SystemError {
        // process the file
        logger.debug("reading from: {}", file); //$NON-NLS-1$
        final FileResource fileResource = new FileResource(file, getApplication());
        DownloadStream ds = fileResource.getStream();
        try {
            List<Lifter> lifters = new WeighInSheet().getAllLifters(ds.getStream(), app);
            for (Lifter curLifter : lifters) {
                app.getHbnSession().save(curLifter);
            }
        } catch (Throwable t) {
            // TODO Auto-generated catch block
            t.printStackTrace();
            throw new SystemError(t);
        }
        // Display the uploaded file in the panel.
        // resultPanel.removeAllComponents();
        // resultPanel.addComponent(new Embedded("", fileResource));
    }

    // This is called if the upload fails.
    public void uploadFailed(Upload.FailedEvent event) {
        // Log the failure on screen.
        final String messageFormat = Messages.getString("SpreadsheetUploader.UploadingFailure", locale); //$NON-NLS-1$
        final String mimeType = event.getMIMEType();
        status.setValue(MessageFormat.format(messageFormat, event.getFilename(), mimeType));
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#needsMenu()
     */
    @Override
    public boolean needsMenu() {
        return true;
    }

    /**
     * @return
     */
    public String getFragment() {
        return viewName;
    }
    

    /* (non-Javadoc)
     * @see org.concordiainternational.competition.ui.components.ApplicationView#setParametersFromFragment(java.lang.String)
     */
    @Override
    public void setParametersFromFragment() {
        String frag = CompetitionApplication.getCurrent().getUriFragmentUtility().getFragment();
        String[] params = frag.split("/");
        if (params.length >= 1) {
            viewName = params[0];
        } else {
            throw new RuleViolationException("Error.ViewNameIsMissing"); 
        }
    }
}