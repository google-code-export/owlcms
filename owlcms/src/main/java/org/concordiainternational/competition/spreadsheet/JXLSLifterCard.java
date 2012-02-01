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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.concordiainternational.competition.data.CategoryLookup;
import org.concordiainternational.competition.data.LifterContainer;
import org.concordiainternational.competition.data.lifterSort.LifterSorter;
import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extentech.ExtenXLS.WorkBookHandle;

/**
 * Encapsulate a spreadsheet as a StreamSource so that it can be used as a
 * source of data when the user clicks on a link. This class converts the output
 * stream produced by the
 * {@link OutputSheet#writeWorkBook(WorkBookHandle, OutputStream)} method to an
 * input stream that the Vaadin framework can consume.
 */
@SuppressWarnings("serial")
public class JXLSLifterCard extends JXLSWorkbookStreamSource {
    public JXLSLifterCard() {
		super(false);
	}
	
    public JXLSLifterCard(boolean excludeNotWeighed) {
		super(excludeNotWeighed);
	}


	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(JXLSLifterCard.class);
    
    protected CategoryLookup categoryLookup;

    
    @Override
	public InputStream getTemplate() throws IOException {
    	String templateName = "/LifterCardTemplate_"+CompetitionApplication.getCurrentLocale().getLanguage()+".xls";
        final InputStream resourceAsStream = app.getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }


	@Override
	protected void getSortedLifters()  {
		this.lifters = LifterSorter.registrationOrderCopy(new LifterContainer(app, isExcludeNotWeighed()).getAllPojos());
	}


}