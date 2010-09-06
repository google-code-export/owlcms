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

package org.concordiainternational.competition.ui.list;

import java.io.Serializable;
import java.util.List;

import com.vaadin.Application;
import com.vaadin.data.hbnutil.HbnContainer;
import com.vaadin.data.hbnutil.HbnContainer.HbnSessionManager;
import com.vaadin.data.util.BeanItemContainer;

/**
 * Normally we would use an HbnContainer directly, but we need to sort on the
 * lifting order, which we do not want written to the database. HbnContainer
 * uses the database to sort, so we have to create our own container as a
 * wrapper. The resulting BeanItemContainer is used to feed the table.
 * 
 * TODO: implement some mechanism through which this list can subscribe to
 * events when other components of the application update the underlying
 * database, potentially affecting the underlying container.
 * 
 * @author jflamy
 * @param <T>
 */
public abstract class GenericBeanList<T extends Serializable> extends GenericList<T> {

    private static final long serialVersionUID = -5396475029309979597L;
    protected List<T> allPojos;

    public GenericBeanList(Application app, Class<T> parameterizedClass, String caption) {
        super(app, parameterizedClass, caption);
    }

    /**
     * Additional initializations, once super.populateAndConfigureTable() (and
     * hence loadData()) has been done.
     */
    @Override
    protected void init() {
        super.init();
    }

    /**
     * Load container content to Table. We create a BeanItemContainer to gain
     * sorting flexibility. Note: this routine is invoked as part of the super()
     * chain in the constructor, and before our own init is called.
     */
    @Override
    protected void loadData() {
        final HbnContainer<T> hbnCont = new HbnContainer<T>(parameterizedClass, (HbnSessionManager) app);
        allPojos = hbnCont.getAllPojos();
        final BeanItemContainer<T> cont = new BeanItemContainer<T>(allPojos);
        table.setContainerDataSource(cont);
    }

    @Override
    public void clearCache() {
        // the following is brute force!
        System.err.println("GenericBeanList: clearCache()"); //$NON-NLS-1$
        table = null;
        populateAndConfigureTable();
    }

}
