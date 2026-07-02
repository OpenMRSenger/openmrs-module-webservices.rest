/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.openmrs.util.OpenmrsClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathPackageScanner {

    private ClasspathPackageScanner() {
    }

    public static List<Class<?>> getClassesForPackage(String pkgname, String suffix) throws IOException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        String relPath = pkgname.replace('.', '/');
        Enumeration<URL> resources = OpenmrsClassLoader.getInstance().getResources(relPath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource == null) {
                throw new IllegalArgumentException("No resource for " + relPath);
            }

            File directory = null;
            try {
                directory = new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                        pkgname + " (" + resource
                                + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
                        e);
            } catch (IllegalArgumentException ex) {
                directory = null;
            }

            if (directory != null && directory.exists()) {
                scanDirectory(directory, pkgname, suffix, classes);
            } else {
                scanJar(resource, relPath, suffix, classes);
            }
        }

        return classes;
    }

    private static void scanDirectory(File directory, String pkgname, String suffix, ArrayList<Class<?>> classes) {
        String[] files = directory.list();
        if (files == null) {
            return;
        }
        for (String file : files) {
            if (file.endsWith(suffix)) {
                String className = pkgname + '.' + file.substring(0, file.length() - suffix.length());
                try {
                    Class<?> cls = Class.forName(className);
                    if (!cls.isInterface()) {
                        classes.add(cls);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("ClassNotFoundException loading " + className, e);
                }
            }
        }
    }

    private static void scanJar(URL resource, String relPath, String suffix, ArrayList<Class<?>> classes) throws IOException {
        String fullPath = resource.getFile();
        int bang = fullPath.indexOf("!/");
        String jarPath = (bang == -1) ? fullPath : fullPath.substring(0, bang);
        if (jarPath.startsWith("file:")) {
            try {
                jarPath = new URL(jarPath).toURI().getPath();
            } catch (URISyntaxException e) {
                jarPath = jarPath.replaceFirst("file:", "");
            }
        }
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.startsWith(relPath) || !entryName.endsWith(suffix)) {
                    continue;
                }
                String className = entryName.replace('/', '.').replace('\\', '.');
                className = className.substring(0, className.length() - suffix.length());
                try {
                    Class<?> cls = Class.forName(className);
                    if (!cls.isInterface()) {
                        classes.add(cls);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("ClassNotFoundException loading " + className, e);
                }
            }
        }
    }
}
