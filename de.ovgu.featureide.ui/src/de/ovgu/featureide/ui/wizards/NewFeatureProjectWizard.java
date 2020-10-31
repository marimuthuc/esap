/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.ui.wizards;

import static de.ovgu.featureide.fm.core.localization.StringTable.CREATING_ANDROID_PROJECT;
import static de.ovgu.featureide.fm.core.localization.StringTable.NEW_FEATUREIDE_PROJECT;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.progress.UIJob;

import de.ovgu.featureide.core.wizardextension.DefaultNewFeatureProjectWizardExtension;
import de.ovgu.featureide.fm.core.base.impl.FeatureStructure;
import de.ovgu.featureide.fm.core.io.xml.XmlAdaptationModel;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.editors.FeatureModelEditor;
import de.ovgu.featureide.fm.ui.wizards.DragDrop;
import de.ovgu.featureide.ui.UIPlugin;

/**
 * A creation wizard for FeatureIDE projects that adds the FeatureIDE nature after creation.
 *
 * @author Marcus Leich
 * @author Thomas Thüm
 * @author Tom Brosch
 * @author Janet Feigenspan
 * @author Sven Schuster
 * @author Lars-Christian Schulz
 * @author Eric Guimatsia
 */
public class NewFeatureProjectWizard extends BasicNewProjectWizard {

	private final static Image colorImage = FMUIPlugin.getDefault().getImageDescriptor("icons/FeatureIconSmall.ico").createImage();
	public static final String ID = UIPlugin.PLUGIN_ID + ".FeatureProjectWizard";

	protected NewFeatureProjectPage page;
	public List<IFile> configfiles;
	public IWizardPage currentPage;
	private DefaultNewFeatureProjectWizardExtension wizardExtension = null;

	@Override
	public void addPages() {
		setWindowTitle(NEW_FEATUREIDE_PROJECT);
		page = new NewFeatureProjectPage();
		final Shell shell = getShell();
		if (shell != null) {
			shell.setImage(colorImage);
		}
		addPage(page);
		super.addPages();
	}

	@Override
	public boolean canFinish() {
		if (page.getCompositionTool().getId().equals("de.ovgu.featureide.preprocessor.munge-android")) {
			return page.isPageComplete();
		}

		if (wizardExtension != null) {
			return wizardExtension.isFinished();
		} else {
			return super.canFinish();
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// determine wizard extension and next page (basic new project page) when composer has been selected
		if (page == this.page) {
//			this.wizardExtension = null;
//			IConfigurationElement[] conf = Platform.getExtensionRegistry().getConfigurationElementsFor("de.ovgu.featureide.ui.wizard");
//			for (IConfigurationElement c : conf) {
//				try {
//					if (c.getAttribute("composerid").equals(this.page.getCompositionTool().getId())) {
//						wizardExtension = (INewFeatureProjectWizardExtension) c.createExecutableExtension("class");
//						wizardExtension.setWizard(this);
//					}
//				} catch (CoreException e) {
//					UIPlugin.getDefault().logError(e);
//				}
//			}
			return super.getNextPage(page);
		} else if (page instanceof WizardNewProjectCreationPage) {
			// determine next page (reference page) after project has been named
			currentPage = super.getNextPage(page);
			if (this.page.getCompositionTool().getId().equals("de.ovgu.featureide.core.AdaptationModeling")) {

				System.out.println("identfied");
				return super.getNextPage(page);
			} else {
				// return super.getNextPage(page);
				return null;
			}
		} else if (wizardExtension != null) {
			final IWizardPage nextExtensionPage = wizardExtension.getNextPage(page);

			if (nextExtensionPage != null) {
				// determine next page (extension pages) when extension exists and reference page or an extension page active
				return nextExtensionPage;
			}
		}
		// every other occurrence
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		List<String> inputFileLocs = null;
		if (!page.hasCompositionTool()) {
			return false;
		}

		if (currentPage instanceof DragDrop) {
			configfiles = ((DragDrop) currentPage).adaptationconfigs;

			inputFileLocs = new ArrayList<String>();
			// check if File exists or not

			try {
				if (!configfiles.isEmpty()) {
					for (int j = 0; j < configfiles.size(); j++) {
						inputFileLocs.add(configfiles.get(j).getLocation().toString());
						// configfiles.get(0).getLocation().toString();
					}
				}
			} catch (final Exception e3) {

			}

			// read from FileReader till the end of file

		}
		/*
		 * for (int i = 0; i < configfiles.length; i++) {
		 * System.out.println("Address of selected project: " + configfiles[i].getFullPath().toString());
		 * }
		 * }
		 */

//		this.wizardExtension = null;
		final IConfigurationElement[] conf = Platform.getExtensionRegistry().getConfigurationElementsFor("de.ovgu.featureide.core.wizard");
		for (final IConfigurationElement c : conf) {
			try {
				if (c.getAttribute("composerid").equals(page.getCompositionTool().getId())) {
					wizardExtension = (DefaultNewFeatureProjectWizardExtension) c.createExecutableExtension("class");
					wizardExtension.setWizard(this);
				}
			} catch (final CoreException e) {
				UIPlugin.getDefault().logError(e);
			}
		}

		if (wizardExtension == null) {
			wizardExtension = new DefaultNewFeatureProjectWizardExtension();
			wizardExtension.setWizard(this);
		}

		if (wizardExtension.performOwnFinish()) {
			final UIJob job = new UIJob(CREATING_ANDROID_PROJECT) {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (wizardExtension.performBeforeFinish(page)) {
						return Status.OK_STATUS;
					} else {
						return Status.CANCEL_STATUS;
					}
				}
			};
			job.setPriority(Job.LONG);
			job.schedule();
			return true;
		} else {
			if (!super.performFinish()) {
				return false;
			}
			// create feature project
			// enhance project depending on extension
			if (wizardExtension.isFinished()) {
				try {
					final IProject newProject1 = getNewProject();

					wizardExtension.enhanceProject(newProject1, page.getCompositionTool().getId(), page.getSourcePath(), page.getConfigPath(),
							page.getBuildPath(), page.sourcePath.isEnabled(), page.buildPath.isEnabled());
					final String filepath = newProject1.getFolder(page.getConfigPath()).getLocation().toString();
					System.out.println(filepath);
					if (page.getCompositionTool().getId().equals("de.ovgu.featureide.core.AdaptationModeling")) {
						final XmlAdaptationModel AdMod = new XmlAdaptationModel(inputFileLocs, filepath + "/AdaptationConfig.xml");
						AdMod.createXML();
						newProject1.getFolder(page.getConfigPath()).refreshLocal(2, null);
					}
					// wizardExtension.enhanceProject(newProject2, page.getCompositionTool().getId(), page.getSourcePath() + "/context",
					// page.getConfigPath() + "/context", page.getBuildPath() + "/context", true, true);
					// page.getConfigPath() + "/feature", page.getBuildPath(), page.sourcePath.isEnabled(), page.buildPath.isEnabled());
					// wizardExtension.enhanceProject(newProject2, page.getCompositionTool().getId(), page.getSourcePath() + "/context",
					// page.getConfigPath() + "/context", page.getBuildPath(), page.sourcePath.isEnabled(), page.buildPath.isEnabled());
					// open editor
					FeatureStructure.COMPID = page.getCompositionTool().getId();
					if (!page.getCompositionTool().getId().equals("de.ovgu.featureide.core.AdaptationModeling")) {
						UIPlugin.getDefault().openEditor(FeatureModelEditor.ID, newProject1.getFile("model.xml"));
					}
				} catch (final CoreException e) {
					UIPlugin.getDefault().logError(e);
				}
			}
		}
		return true;
	}
}
