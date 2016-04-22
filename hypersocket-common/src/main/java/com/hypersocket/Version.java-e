/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket;


public class Version implements Comparable<Version> {
    private int[] elements;
    private String versionString;

    public Version(String versionString) {
        parseFromString(versionString);
    }

    public void parseFromString(String versionString) {
        this.versionString = versionString;
        String[] stringElements = versionString.split("[^a-zA-Z0-9]+");
        if (stringElements.length < 2 || stringElements.length > 4) {
            throw new IllegalArgumentException("Version number '" + versionString + "' incorrect. Must be in the format <major>.<minor>.<release>[?TAG]");
        } else if (stringElements.length == 3) {
            stringElements = new String[] { stringElements[0], stringElements[1], stringElements[2], "base" };
        } else if (stringElements.length == 2) {
            stringElements = new String[] { stringElements[0], stringElements[1], "0", "base" };
        }
        elements = new int[stringElements.length];
        int idx = 0;
        int element;
        for (String string : stringElements) {
            if (idx == 3) {
                if (string.equalsIgnoreCase("base")) {
                    element = 0;
                } else if (string.toLowerCase().startsWith("ga")) {
                    String substring = string.substring(2);
                    element = -199 + ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else if (string.toLowerCase().startsWith("rc")) {
                    String substring = string.substring(2);
                    element = -299 + ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else if (string.toLowerCase().startsWith("beta")) {
                    String substring = string.substring(4);
                    element = -399 + ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else if (string.toLowerCase().startsWith("alpha")) {
                    String substring = string.substring(5);
                    element = -499 + ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else if (string.toLowerCase().startsWith("snapshot")) {
                    String substring = string.substring(8);
                    element = -599 + ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else if (string.toLowerCase().startsWith("local")) {
                    element = Integer.MAX_VALUE;
                } else if (string.toLowerCase().startsWith("r")) {
                    String substring = string.substring(1);
                    element = ( substring.equals("") ? 0 : Integer.parseInt(substring) );
                } else {
                    element = Integer.parseInt(string);
                }
            } else {
                element = Integer.parseInt(string);
            }
            elements[idx] = element;
            idx++;
        }
    }

    public int hashCode() {
        return toString().hashCode();
    }
    
    public boolean equals(Object o) {
        return o != null  && o instanceof Version && ((Version)o).compareTo(this) == 0;
    }

    public int[] getVersionElements() {
        return elements;
    }

    public String toString() {
        return versionString;
    }

    public int compareTo(Version version) {
        if (version == null) {
            return 1;
        }
        int[] otherElements = version.getVersionElements();
        for(int i = 0 ; i < 4 ; i++) {
            if(elements[i] != otherElements[i]) {
                return elements[i] - otherElements[i];
            }
        }
        return 0;
    }
  
}
