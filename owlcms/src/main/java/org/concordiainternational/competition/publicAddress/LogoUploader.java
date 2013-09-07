/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.publicAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.concordiainternational.competition.data.RuleViolationException;
import org.concordiainternational.competition.i18n.Messages;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.concordiainternational.competition.ui.components.ApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.SystemError;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * @author IT Mill
 * @author jflamy - adapted from example in the Book of Vaadin.
 * 
 */
public class LogoUploader extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener,
Upload.Receiver, ApplicationView {
	private static final long serialVersionUID = 6843262937708809785L;
	final private static Logger logger = LoggerFactory.getLogger(LogoUploader.class);

	Panel root; // Root element for contained components.
	Panel resultPanel; // Panel that contains the uploaded image.
	File file; // File to write to.

	private CompetitionApplication app;

	private Label status;

	private Locale locale;
	private String viewName;

	public LogoUploader(Layout parent) {
		this.app = CompetitionApplication.getCurrent();
		this.locale = app.getLocale();
		Layout compositionRoot = new VerticalLayout();
        this.setCompositionRoot(compositionRoot);
        this.setCaption(Messages.getString("Competition.logo", locale));

		// Create the Upload component.
		//final Upload upload = new Upload(Messages.getString("SpreadsheetUploader.ChooseFile",locale), this); //$NON-NLS-1$
		final Upload upload = new Upload("", this); //$NON-NLS-1$
		upload.setImmediate(true); // start immediately as soon as the file is
		// selected.

		// Use a custom button caption instead of plain "Upload".
		upload.setButtonCaption(Messages.getString("LogoUploader.UploadNow", locale)); //$NON-NLS-1$

		// Listen for events regarding the success of upload.
		upload.addListener((Upload.SucceededListener) this);
		upload.addListener((Upload.FailedListener) this);
		compositionRoot.addComponent(upload);
		compositionRoot.addComponent(new Label());
		status = new Label(""); //$NON-NLS-1$
		compositionRoot.addComponent(status);
	}

	// Callback method to begin receiving the upload.
	@Override
	public OutputStream receiveUpload(String filename, String MIMEType) {
		FileOutputStream fos = null; // Output stream to write to
		try {
			ServletContext sCtx = app.getServletContext();
			String absfilename = sCtx.getRealPath("VAADIN/themes/competition/images/logo.png"); //$NON-NLS-1$
			file = new File(absfilename);
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
	@Override
	public void uploadSucceeded(Upload.SucceededEvent event) {
		// Log the upload on screen.
		final String messageFormat = Messages.getString("SpreadsheetUploader.Status", locale); //$NON-NLS-1$
		final String mimeType = event.getMIMEType();
		status.setValue(MessageFormat.format(messageFormat, event.getFilename(), mimeType));
		processUploadedFile(mimeType);
	}

	/**
	 * @param mimeType 
	 * @throws SystemError
	 */
	private void processUploadedFile(String mimeType) throws SystemError {
	}


	// This is called if the upload fails.
	@Override
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
	@Override
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

	@Override
	public void registerAsListener() {
		app.getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void unregisterAsListener() {
		app.getMainWindow().addListener((CloseListener) this);
	}

	@Override
	public void windowClose(CloseEvent e) {
		unregisterAsListener();	
	}

	/* Called on refresh.
	 * @see com.vaadin.terminal.URIHandler#handleURI(java.net.URL, java.lang.String)
	 */
	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		registerAsListener();
		return null;
	}
	
    @Override
    public boolean needsBlack() {
        return false;
    }

}
