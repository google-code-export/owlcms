/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

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
