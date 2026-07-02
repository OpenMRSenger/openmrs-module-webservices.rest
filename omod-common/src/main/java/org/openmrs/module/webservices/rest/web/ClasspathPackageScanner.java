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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathPackageScanner {
	
	public static ArrayList<Class<?>> getClassesForPackage(String pkgname, String suffix) throws IOException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		String relPath = pkgname.replace('.', '/');
		Enumeration<URL> resources = OpenmrsClassLoader.getInstance().getResources(relPath);
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			if (resource == null) {
				throw new RuntimeException("No resource for " + relPath);
			}
			
			File directory = null;
			try {
				directory = new File(resource.toURI());
			}
			catch (URISyntaxException e) {
				throw new RuntimeException(
				        pkgname + " (" + resource
				                + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
				        e);
			}
			catch (IllegalArgumentException ex) {}
			
			if (directory != null && directory.exists()) {
				scanDirectory(directory, pkgname, suffix, classes);
			} else {
				scanJar(resource, relPath, pkgname, suffix, classes);
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
				String className = pkgname + '.' + file.substring(0, file.length() - 6);
				try {
					Class<?> cls = Class.forName(className);
					if (!cls.isInterface()) {
						classes.add(cls);
					}
				}
				catch (ClassNotFoundException e) {
					throw new RuntimeException("ClassNotFoundException loading " + className);
				}
			}
		}
	}
	
	private static void scanJar(URL resource, String relPath, String pkgname, String suffix, ArrayList<Class<?>> classes) throws IOException {
		JarFile jarFile = null;
		try {
			String fullPath = resource.getFile();
			String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
			jarFile = new JarFile(jarPath);
			
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String entryName = entry.getName();
				
				if (!entryName.endsWith(suffix)) {
					continue;
				}
				
				if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
					String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
					try {
						Class<?> cls = Class.forName(className);
						if (!cls.isInterface()) {
							classes.add(cls);
						}
					}
					catch (ClassNotFoundException e) {
						throw new RuntimeException("ClassNotFoundException loading " + className);
					}
				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException(pkgname + " (" + resource + ") does not appear to be a valid package", e);
		}
		finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
	}
}
