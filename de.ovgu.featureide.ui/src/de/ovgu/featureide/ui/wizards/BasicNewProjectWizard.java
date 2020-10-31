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

/**
 * TODO description
 *
 * @author Vinith
 */

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import de.ovgu.featureide.fm.ui.wizards.DragDrop;

/**
 * Standard workbench wizard that creates a new project resource in the
 * workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewProjectResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 *
 * During the call to <code>open</code>, the wizard dialog is presented to
 * the user. When the user hits Finish, a project resource with the
 * user-specified name is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BasicNewProjectWizard extends BasicNewResourceWizard implements IExecutableExtension {

	/**
	 * The wizard id for creating new projects in the workspace.
	 *
	 * @since 3.4
	 */
	public static final String WIZARD_ID = "org.eclipse.ui.wizards.new.project"; //$NON-NLS-1$

	private WizardNewProjectCreationPage mainPage;

	public WizardNewProjectReferencePage referencePage;

	public DragDrop DropPage;

	public SelectProjectWizardPage projectPage;

	// cache of newly-created project
	private IProject newProject;

	/**
	 * The config element which declares this wizard.
	 */
	private IConfigurationElement configElement;

	private static String WINDOW_PROBLEMS_TITLE = ResourceMessages.NewProject_errorOpeningWindow;

	/**
	 * Extension attribute name for final perspective.
	 */
	private static final String FINAL_PERSPECTIVE = "finalPerspective"; //$NON-NLS-1$

	/**
	 * Extension attribute name for preferred perspectives.
	 */
	private static final String PREFERRED_PERSPECTIVES = "preferredPerspectives"; //$NON-NLS-1$

	/**
	 * Creates a wizard for creating a new project resource in the workspace.
	 */
	public BasicNewProjectWizard() {
		final IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(), new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle(ResourceMessages.NewProject_title);
		mainPage.setDescription(ResourceMessages.NewProject_description);
		addPage(mainPage);

		// only add page if there are already projects in the workspace

		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			DropPage = new DragDrop("basicDropProjectPage");//$NON-NLS-1$
			DropPage.setTitle(ResourceMessages.NewProject_referenceTitle);
			DropPage.setDescription(ResourceMessages.NewProject_referenceDescription);
			addPage(DropPage);
		}

		/*
		 * if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
		 * referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$
		 * referencePage.setTitle(ResourceMessages.NewProject_referenceTitle);
		 * referencePage.setDescription(ResourceMessages.NewProject_referenceDescription);
		 * addPage(referencePage);
		 * }
		 */

		// if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
		// projectPage = new SelectProjectWizardPage();
		// projectPage.setTitle("Select Project");
		// projectPage.setDescription(ResourceMessages.NewProject_referenceDescription);
		// addPage(projectPage);
		// }
	}

	public void addPages(boolean value) {
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(), new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle(ResourceMessages.NewProject_title);
		mainPage.setDescription(ResourceMessages.NewProject_description);
		addPage(mainPage);

		if (value) {
			if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
				DropPage = new DragDrop("basicDropProjectPage");//$NON-NLS-1$
				DropPage.setTitle(ResourceMessages.NewProject_referenceTitle);
				DropPage.setDescription(ResourceMessages.NewProject_referenceDescription);
				addPage(DropPage);
			}
		} else {
			// only add page if there are already projects in the workspace

			if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
				referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$
				referencePage.setTitle(ResourceMessages.NewProject_referenceTitle);
				referencePage.setDescription(ResourceMessages.NewProject_referenceDescription);
				addPage(referencePage);
			}
		}

		// if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
		// projectPage = new SelectProjectWizardPage();
		// projectPage.setTitle("Select Project");
		// projectPage.setDescription(ResourceMessages.NewProject_referenceDescription);
		// addPage(projectPage);
		// }
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish
	 * on the wizard; the enablement of the Finish button implies that all
	 * controls on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been
	 * successfully created; subsequent invocations of this method will answer
	 * the same project resource without attempting to create it again.
	 * </p>
	 *
	 * @return the created project resource, or <code>null</code> if the
	 *         project was not created
	 */
	private IProject createNewProject() {
		if (newProject != null) {
			return newProject;
		}

		// get a project handle
		final IProject newProjectHandle = mainPage.getProjectHandle();

		// get a project descriptor
		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		// update the referenced project if provided
		/*
		 * if (referencePage != null) {
		 * final IProject[] refProjects = referencePage.getReferencedFiles();
		 * if (refProjects.length > 0) {
		 * description.setReferencedProjects(refProjects);
		 * }
		 * }
		 */

		// create the new project operation
		final IRunnableWithProgress op = monitor -> {
			final CreateProjectOperation op1 = new CreateProjectOperation(description, ResourceMessages.NewProject_windowTitle);
			try {
				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
				// directly execute the operation so that the undo state is
				// not preserved. Making this undoable resulted in too many
				// accidental file deletions.
				op1.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
			} catch (final ExecutionException e) {
				throw new InvocationTargetException(e);
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (final InterruptedException e) {
			return null;
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getTargetException();
			if ((t instanceof ExecutionException) && (t.getCause() instanceof CoreException)) {
				final CoreException cause = (CoreException) t.getCause();
				StatusAdapter status;
				if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					status = new StatusAdapter(StatusUtil.newStatus(IStatus.WARNING,
							NLS.bind(ResourceMessages.NewProject_caseVariantExistsError, newProjectHandle.getName()), cause));
				} else {
					status = new StatusAdapter(StatusUtil.newStatus(cause.getStatus().getSeverity(), ResourceMessages.NewProject_errorMessage, cause));
				}
				status.setProperty(StatusAdapter.TITLE_PROPERTY, ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			} else {
				final StatusAdapter status = new StatusAdapter(new Status(IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH, 0,
						NLS.bind(ResourceMessages.NewProject_internalError, t.getMessage()), t));
				status.setProperty(StatusAdapter.TITLE_PROPERTY, ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

	/**
	 * Returns the newly created project.
	 *
	 * @return the created project, or <code>null</code> if project not
	 *         created
	 */
	public IProject getNewProject() {
		return newProject;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(ResourceMessages.NewProject_windowTitle);
	}

	@Override
	protected void initializeDefaultPageImageDescriptor() {
		final ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	private static void openInNewWindow(IPerspectiveDescriptor desc) {

		// Open the page.
		try {
			PlatformUI.getWorkbench().openWorkbenchWindow(desc.getId(), ResourcesPlugin.getWorkspace().getRoot());
		} catch (final WorkbenchException e) {
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				ErrorDialog.openError(window.getShell(), WINDOW_PROBLEMS_TITLE, e.getMessage(), e.getStatus());
			}
		}
	}

	@Override
	public boolean performFinish() {
		createNewProject();

		if (newProject == null) {
			return false;
		}

		final IWorkingSet[] workingSets = mainPage.getSelectedWorkingSets();
		getWorkbench().getWorkingSetManager().addToWorkingSets(newProject, workingSets);

		updatePerspective();
		selectAndReveal(newProject);

		return true;
	}

	private static void replaceCurrentPerspective(IPerspectiveDescriptor persp) {

		// Get the active page.
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		final IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}

		// Set the perspective.
		page.setPerspective(persp);
	}

	/**
	 * Stores the configuration element for the wizard. The config element will
	 * be used in <code>performFinish</code> to set the result perspective.
	 */
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		configElement = cfig;
	}

	/**
	 * Updates the perspective for the active page within the window.
	 */
	protected void updatePerspective() {
		updatePerspective(configElement);
	}

	/**
	 * Updates the perspective based on the current settings in the
	 * Workbench/Perspectives preference page.
	 *
	 * Use the setting for the new perspective opening if we are set to open in
	 * a new perspective.
	 * <p>
	 * A new project wizard class will need to implement the
	 * <code>IExecutableExtension</code> interface so as to gain access to the
	 * wizard's <code>IConfigurationElement</code>. That is the configuration
	 * element to pass into this method.
	 * </p>
	 *
	 * @param configElement -
	 *        the element we are updating with
	 *
	 * @see IPreferenceConstants#OPM_NEW_WINDOW
	 * @see IPreferenceConstants#OPM_ACTIVE_PAGE
	 * @see IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
	 */
	public static void updatePerspective(IConfigurationElement configElement) {
		// Do not change perspective if the configuration element is
		// not specified.
		if (configElement == null) {
			return;
		}

		// Retrieve the new project open perspective preference setting
		final String perspSetting = PrefUtil.getAPIPreferenceStore().getString(IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE);

		final String promptSetting = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);

		// Return if do not switch perspective setting and are not prompting
		if (!(promptSetting.equals(MessageDialogWithToggle.PROMPT)) && perspSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE)) {
			return;
		}

		// Read the requested perspective id to be opened.
		final String finalPerspId = configElement.getAttribute(FINAL_PERSPECTIVE);
		if (finalPerspId == null) {
			return;
		}

		// Map perspective id to descriptor.
		final IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();

		// leave this code in - the perspective of a given project may map to
		// activities other than those that the wizard itself maps to.
		final IPerspectiveDescriptor finalPersp = reg.findPerspectiveWithId(finalPerspId);
		if ((finalPersp != null) && (finalPersp instanceof IPluginContribution)) {
			final IPluginContribution contribution = (IPluginContribution) finalPersp;
			if (contribution.getPluginId() != null) {
				final IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
				final IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
				final IIdentifier identifier = activityManager.getIdentifier(WorkbenchActivityHelper.createUnifiedId(contribution));
				final Set idActivities = identifier.getActivityIds();

				if (!idActivities.isEmpty()) {
					final Set enabledIds = new HashSet(activityManager.getEnabledActivityIds());

					if (enabledIds.addAll(idActivities)) {
						workbenchActivitySupport.setEnabledActivityIds(enabledIds);
					}
				}
			}
		} else {
			IDEWorkbenchPlugin.log("Unable to find persective " //$NON-NLS-1$
				+ finalPerspId + " in BasicNewProjectResourceWizard.updatePerspective"); //$NON-NLS-1$
			return;
		}

		// gather the preferred perspectives
		// always consider the final perspective (and those derived from it)
		// to be preferred
		final ArrayList preferredPerspIds = new ArrayList();
		addPerspectiveAndDescendants(preferredPerspIds, finalPerspId);
		final String preferred = configElement.getAttribute(PREFERRED_PERSPECTIVES);
		if (preferred != null) {
			final StringTokenizer tok = new StringTokenizer(preferred, " \t\n\r\f,"); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				addPerspectiveAndDescendants(preferredPerspIds, tok.nextToken());
			}
		}

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				final IPerspectiveDescriptor currentPersp = page.getPerspective();

				// don't switch if the current perspective is a preferred
				// perspective
				if ((currentPersp != null) && preferredPerspIds.contains(currentPersp.getId())) {
					return;
				}
			}

			// prompt the user to switch
			if (!confirmPerspectiveSwitch(window, finalPersp)) {
				return;
			}
		}

		final int workbenchPerspectiveSetting = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.OPEN_PERSP_MODE);

		// open perspective in new window setting
		if (workbenchPerspectiveSetting == IPreferenceConstants.OPM_NEW_WINDOW) {
			openInNewWindow(finalPersp);
			return;
		}

		// replace active perspective setting otherwise
		replaceCurrentPerspective(finalPersp);
	}

	/**
	 * Adds to the list all perspective IDs in the Workbench who's original ID
	 * matches the given ID.
	 *
	 * @param perspectiveIds
	 *        the list of perspective IDs to supplement.
	 * @param id
	 *        the id to query.
	 * @since 3.0
	 */
	private static void addPerspectiveAndDescendants(List perspectiveIds, String id) {
		final IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		for (final IPerspectiveDescriptor perspective : registry.getPerspectives()) {
			// @issue illegal ref to workbench internal class;
			// consider adding getOriginalId() as API on IPerspectiveDescriptor
			final PerspectiveDescriptor descriptor = ((PerspectiveDescriptor) perspective);
			if (descriptor.getOriginalId().equals(id)) {
				perspectiveIds.add(descriptor.getId());
			}
		}
	}

	/**
	 * Prompts the user for whether to switch perspectives.
	 *
	 * @param window
	 *        The workbench window in which to switch perspectives; must not
	 *        be <code>null</code>
	 * @param finalPersp
	 *        The perspective to switch to; must not be <code>null</code>.
	 *
	 * @return <code>true</code> if it's OK to switch, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPerspectiveSwitch(IWorkbenchWindow window, IPerspectiveDescriptor finalPersp) {
		final IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		final String pspm = store.getString(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
		if (!IDEInternalPreferences.PSPM_PROMPT.equals(pspm)) {
			// Return whether or not we should always switch
			return IDEInternalPreferences.PSPM_ALWAYS.equals(pspm);
		}
		final String desc = finalPersp.getDescription();
		String message;
		if ((desc == null) || (desc.length() == 0)) {
			message = NLS.bind(ResourceMessages.NewProject_perspSwitchMessage, finalPersp.getLabel());
		} else {
			message = NLS.bind(ResourceMessages.NewProject_perspSwitchMessageWithDesc, new String[] { finalPersp.getLabel(), desc });
		}

		final LinkedHashMap<String, Integer> buttonLabelToId = new LinkedHashMap<>();
		buttonLabelToId.put(ResourceMessages.NewProject_perspSwitchButtonLabel, IDialogConstants.YES_ID);
		buttonLabelToId.put(IDialogConstants.NO_LABEL, IDialogConstants.NO_ID);
		final MessageDialogWithToggle dialog =
			MessageDialogWithToggle.open(MessageDialog.QUESTION, window.getShell(), ResourceMessages.NewProject_perspSwitchTitle, message, null, false, store,
					IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE, SWT.NONE, buttonLabelToId);
		final int result = dialog.getReturnCode();

		// If we are not going to prompt anymore propogate the choice.
		if (dialog.getToggleState()) {
			String preferenceValue;
			if (result == IDialogConstants.YES_ID) {
				// Doesn't matter if it is replace or new window
				// as we are going to use the open perspective setting
				preferenceValue = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
			} else {
				preferenceValue = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
			}

			// update PROJECT_OPEN_NEW_PERSPECTIVE to correspond
			PrefUtil.getAPIPreferenceStore().setValue(IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE, preferenceValue);
		}
		return result == IDialogConstants.YES_ID;
	}
}
