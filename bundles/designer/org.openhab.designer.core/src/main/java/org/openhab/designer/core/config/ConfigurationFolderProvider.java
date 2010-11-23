/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */

package org.openhab.designer.core.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.openhab.designer.core.CoreActivator;
import org.openhab.designer.core.DesignerCoreConstants;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFolderProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationFolderProvider.class);
	
	private static IFolder folder; 
	
	static public synchronized IFolder getRootConfigurationFolder() throws CoreException {
		if(folder==null) {
			IProject defaultProject = ResourcesPlugin.getWorkspace().getRoot().getProject("config");
			if(!defaultProject.exists()) {
				defaultProject.create(null);
				defaultProject.open(null);
				initialize(defaultProject);
			}
			File configFolder = getFolderFromPreferences();
			if(configFolder!=null) {
				folder = defaultProject.getFolder("config");
				folder.createLink(configFolder.toURI(), IResource.BACKGROUND_REFRESH|IResource.REPLACE, null);
			}
		}
		return folder;
	}
	
	static public synchronized void setRootConfigurationFolder(final File configFolder) throws CoreException {
		if(folder==null) {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					IProject defaultProject = ResourcesPlugin.getWorkspace().getRoot().getProject("config");
					if(!defaultProject.exists()) {
						defaultProject.create(null);
						defaultProject.open(null);
						initialize(defaultProject);
					}
					if(configFolder!=null) {
						folder = defaultProject.getFolder("config");
						if(folder.exists()) {
							folder.delete(true, null);
						}
						folder.createLink(configFolder.toURI(), IResource.BACKGROUND_REFRESH|IResource.REPLACE, null);
					}
				}
			};
			ResourcesPlugin.getWorkspace().run(runnable, null);
		}
	}	
	
	private static void initialize(IProject project) {
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] { "org.eclipse.xtext.ui.shared.xtextNature", "org.eclipse.pde.PluginNature" } );
			project.setDescription(desc, null);
			
			IFolder metaInfFolder = project.getFolder("META-INF");
			metaInfFolder.create(true, true, null);
			IFile manifestFile = metaInfFolder.getFile("MANIFEST.MF");
			
			InputStream is = FileLocator.openStream(CoreActivator.getDefault().getBundle(), new Path("resources/MANIFEST.MF"), false);
			manifestFile.create(is, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getFolderFromPreferences() {
		IPreferencesService service = Platform.getPreferencesService();
		Preferences node = service.getRootNode().node(DefaultScope.SCOPE).node(CoreActivator.PLUGIN_ID);
		if(node!=null) {
			String folderPath = node.get(DesignerCoreConstants.CONFIG_FOLDER_PREFERENCE, null);
			if(folderPath!=null) {
				File file = new File(folderPath);
				if(file!=null && file.isDirectory()) {
					return file;
				} else {
					logger.warn("'{}' is no valid directory.", folderPath);
				}
			}
		}
		return null;
	}
}
