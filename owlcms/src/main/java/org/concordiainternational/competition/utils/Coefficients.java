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

package org.concordiainternational.competition.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;

import org.concordiainternational.competition.ui.CompetitionApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflamy
 * 
 */
public class Coefficients {

    static Logger logger = LoggerFactory.getLogger(Coefficients.class);

    private static HashMap<Integer, Float> smm = null;
    static Properties props = null;
    static Double menCoefficient = null;
    static Double womenCoefficient = null;
    static Double menMaxWeight = null;
    static Double womenMaxWeight = null;

    /**
     * @return
     * @throws IOException
     */
    private static HashMap<Integer, Float> loadSMM() {

        if (props == null) loadProps();

        smm = new HashMap<Integer, Float>((int) (props.size() * 1.4));
        for (Entry<Object, Object> entry : props.entrySet()) {
            String curKey = (String) entry.getKey();
            if (curKey.startsWith("smm.")) {
                smm.put(Integer.valueOf(curKey.replace("smm.", "")), Float.valueOf((String) entry.getValue()));
            }
        }
        return smm;
    }

    /**
     * @throws IOException
     */
    private static void loadProps() {
        props = new Properties();
        try {
            InputStream stream = CompetitionApplication.getCurrent().getResourceAsStream("/sinclair.properties");
            props.load(stream);
            // props.list(System.err);
        } catch (IOException e) {
            LoggerUtils.logException(logger, e);
        }
    }

    /**
     * @throws IOException
     * 
     */
    private static void loadCoefficients() {
        if (props == null) loadProps();
        menCoefficient = Double.valueOf((String) props.get("sinclair.menCoefficient"));
        menMaxWeight = Double.valueOf((String) props.get("sinclair.menMaxWeight"));
        womenCoefficient = Double.valueOf((String) props.get("sinclair.womenCoefficient"));
        womenMaxWeight = Double.valueOf((String) props.get("sinclair.womenMaxWeight"));
    }

    /**
     * @return
     */
    public static Double menCoefficient() {
        if (menCoefficient == null) loadCoefficients();
        return menCoefficient;
    }

    /**
     * @return
     */
    public static Double womenCoefficient() {
        if (womenCoefficient == null) loadCoefficients();
        return womenCoefficient;
    }

    /**
     * @return
     */
    public static Double menMaxWeight() {
        if (menMaxWeight == null) loadCoefficients();
        return menMaxWeight;
    }

    /**
     * @return
     */
    public static Double womenMaxWeight() {
        if (womenMaxWeight == null) loadCoefficients();
        return womenMaxWeight;
    }

    /**
     * @param age
     * @return the Sinclair-Malone-Meltzer Coefficient for that age.
     * @throws IOException
     */
    public static Float getSMMCoefficient(Integer age) throws IOException {
        if (smm == null) loadSMM();
        if (age <= 30) return 1.0F;
        if (age >= 90) return smm.get(90);
        return smm.get(age);
    }

}
