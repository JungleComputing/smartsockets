/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package ibis.smartsockets.direct;


import ibis.smartsockets.util.NetworkUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Preference {

    private static final Logger logger =
        LoggerFactory.getLogger("ibis.smartsockets.network.preference");

    private final String name;

    private final boolean strict;

    private ArrayList<Network> preferences = new ArrayList<Network>();

    private boolean noneAllowed = false;

    private boolean siteUsed = false;

    private boolean linkUsed = false;

    private boolean globalUsed = false;

    Preference(String name, boolean strict) {
        this.name = name;
        this.strict = strict;
    }

    int size() {
        return preferences.size();
    }

    void addSite() {

        if (siteUsed) {
            logger.warn("Preference(" + name + "): "
                    + "Site addresses already used.");
            throw new IllegalStateException(name + ": Site addresses "
                    + "already used.");
        }

        if (noneAllowed) {
            logger.warn("Preference(" + name + "): "
                    + "Cannot combine network rule 'site' with rule 'none'!");
            throw new IllegalStateException(name + ": Cannot combine network " +
                    "rule 'site' with rule 'none'!");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preference(" + name
                    + "): Adding site-local addresses to "
                    + "connection preference");
        }

        preferences.add(Network.SITE);
        siteUsed = true;
    }

    void addLink() {

        if (linkUsed) {
            logger.warn("Preference(" + name + "): "
                    + "Link addresses already used.");
            throw new IllegalStateException(name + ": Link addresses "
                    + "already used.");
        }

        if (noneAllowed) {
            logger.warn("Preference(" + name + "): "
                    + "Cannot combine network rule 'link' with rule 'none'!");
            throw new IllegalStateException(name + ": Cannot combine network " +
                    "rule 'link' with rule 'none'!");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preference(" + name + "): Adding "
                    + "link-local addresses to connection preference");
        }

        preferences.add(Network.LINK);
        linkUsed = true;
    }

    void addGlobal() {

        if (noneAllowed) {
            logger.warn("Preference(" + name + "): "
                    + "Cannot combine network rule 'global' with rule 'none'!");
            throw new IllegalStateException(name + ": Cannot combine network " +
                    "rule 'global' with rule 'none'!");
        }

        if (globalUsed) {
            logger.warn("Preference(" + name + "): "
                    + "Global addresses already used.");
            throw new IllegalStateException(name + ": Global addresses "
                    + "already used.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preference(" + name + "): "
                    + "Adding global addresses to connection preference");
        }

        preferences.add(Network.GLOBAL);
        globalUsed = true;
    }

    public void addNone() {

        if (siteUsed || linkUsed || globalUsed) {
            logger.warn("Preference(" + name + "): "
                    + "Cannot combine network rule 'none' with any rules that " +
                            "allow connection!");
            throw new IllegalStateException(name + ": network rule 'none' " +
                    "specified, while other rules already apply!.");
        }

        preferences.add(Network.NONE);
        noneAllowed = true;
    }

    void addNetwork(Network nw) {

        if (noneAllowed) {
            logger.warn("Preference(" + name + "): "
                    + "Cannot combine network rule with rule 'none'!");
            throw new IllegalStateException(name + ": Cannot combine network " +
                    "rule  with rule 'none'!");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preference(" + name + "): "
                    + "Adding network " + nw + " to connection preference");
        }

        preferences.add(nw);
    }

    /*
    void addNetwork(byte[] network, byte[] mask) {

        if (logger.isDebugEnabled()) {
            logger.debug("Preference(" + name + "): "
                    + "Adding network " + NetworkUtils.bytesToString(network)
                    + "/" + NetworkUtils.bytesToString(mask)
                    + " to connection preference");
        }

        preferences.add(new Network(network, mask));
    }
*/

    private int score(InetAddress ad) {

        int score = 0;

        for (Network nw : preferences) {

            if (nw.match(ad)) {
                return score;
            } else {
                score++;
            }
        }

        return score + 1;
    }

    private void sort(Object[] objects, int[] scores) {

        for (int i = 0; i < objects.length - 1; i++) {
            for (int j = 0; j < objects.length - 1 - i; j++) {
                if (scores[j + 1] < scores[j]) {
                    int tmp = scores[j + 1];
                    scores[j + 1] = scores[j];
                    scores[j] = tmp;

                    Object ta = objects[j + 1];
                    objects[j + 1] = objects[j];
                    objects[j] = ta;
                }
            }
        }
    }

    InetSocketAddress[] sort(InetSocketAddress[] ads, boolean inPlace) {

        // TODO: New implementation here ?
        // The global/local/link differences should now be trivial, so many
        // of the simpler rules can be implemented more efficiently....


        // Nothing to if there are no rules, or only 1 address....
        if (preferences.size() == 0 || ads.length == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Preference(" + name + "):"
                        + " No sorting required");
            }
            return ads;
        }

        // First give every address a score based on the preference rules.
        // Also count the number of entries that got a real score.
        int scored = 0;
        int[] scores = new int[ads.length];

        for (int i = 0; i < ads.length; i++) {
            scores[i] = score(ads[i].getAddress());

            if (scores[i] < preferences.size() + 1) {
                scored++;
            }
        }

        InetSocketAddress[] result = null;

        if (strict && !inPlace) {
            // We now remove all the addresses which are not wanted.
            result = new InetSocketAddress[scored];
            int[] tmp = new int[scored];
            int next = 0;

            if (logger.isInfoEnabled()) {
                logger.info("Preference(" + name + "): Strict "
                        + "mode on. Removing unwanted addresses.");
            }

            for (int i = 0; i < ads.length; i++) {
                if (scores[i] < preferences.size() + 1) {
                    result[next] = ads[i];
                    tmp[next] = scores[i];
                    next++;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Preference(" + name + "): "
                                + "Removing address: "
                                + NetworkUtils.ipToString(ads[i].getAddress()));
                    }
                }
            }

            scores = tmp;

        } else if (!inPlace) {
            // When we do not want inplace sorting, we copy the addresses.
            result = ads.clone();

        } else {
            // Else, we use the addresses directly.
            result = ads;
        }

        // Once every one has a score, we'll bubble sort the lot.
        sort(result, scores);
        return result;
    }

    InetAddress[] sort(InetAddress[] ads, boolean inPlace) {

        // Nothing to if there are no rules, or only 1 address....
        if (preferences.size() == 0 || ads.length == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Preference(" + name + "): "
                        + "No sorting required");
            }
            return ads;
        }

        // First give every address a score based on the preference rules.
        // Also count the number of entries that got a real score.
        int scored = 0;
        int[] scores = new int[ads.length];

        for (int i = 0; i < ads.length; i++) {
            scores[i] = score(ads[i]);

            if (scores[i] < preferences.size() + 1) {
                scored++;
            }
        }

        InetAddress[] result = null;

        if (strict && !inPlace) {
            // We now remove all the addresses which are not wanted.
            if (logger.isInfoEnabled()) {
                logger.info("Preference(" + name + "): "
                        + "Strict mode on. Removing unwanted addresses.");
            }

            result = new InetAddress[scored];
            int[] tmp = new int[scored];
            int next = 0;

            for (int i = 0; i < ads.length; i++) {
                if (scores[i] < preferences.size() + 1) {
                    result[next] = ads[i];
                    tmp[next] = scores[i];
                    next++;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Preference(" + name + "): "
                                + "Removing address: "
                                + NetworkUtils.ipToString(ads[i]));
                    }
                }
            }

            scores = tmp;

        } else if (!inPlace) {
            // When we do not want inplace sorting, we copy the addresses.
            result = ads.clone();

        } else {
            // Else, we use the addresses directly.
            result = ads;
        }

        // Once every one has a score, we'll bubble sort the lot.
        sort(result, scores);
        return result;
    }

    public String toString() {

        if (preferences.size() == 0) {
            return name + ": Connection preference: none";
        }

        StringBuffer buf = new StringBuffer(name + ": Connection preference:");

        int i = 0;

        for (Network nw : preferences) {
            buf.append(nw.toString());

            if (++i < preferences.size()) {
                buf.append(",");
            }
        }

        return buf.toString();
    }


}
