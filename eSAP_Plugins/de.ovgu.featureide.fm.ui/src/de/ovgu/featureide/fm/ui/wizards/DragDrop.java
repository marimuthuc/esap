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
package de.ovgu.featureide.fm.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.IFeatureProject;

/**
 * Standard project reference page for a wizard that creates a
 * project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be
 * subclassed to suit.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");
 * referencePage.setTitle("Project");
 * referencePage.setDescription("Select referenced projects.");
 * </pre>
 * </p>
 */
public class DragDrop extends WizardPage {
	// widgets

	private CheckboxTableViewer contextCheckboxTableViewer;
	private CheckboxTableViewer featureCheckboxTableViewer;

	private static final String REFERENCED_PROJECTS_TITLE = IDEWorkbenchMessages.WizardNewProjectReferences_title;

	private static final int PROJECT_LIST_MULTIPLIER = 15;

	private Combo comboFeatureProject;
	String comboFeatureProjectText;
	String comboFeatureProjectAddress;

	private Combo comboContextProject;
	String comboContextProjectText;
	String comboContextProjectAddress;

	private final Collection<IFeatureProject> featureProjects = CorePlugin.getFeatureProjects();

	private IFeatureProject featureProject = null;
	private IFeatureProject contextProject = null;

	public List<IFile> adaptationconfigs = new ArrayList<IFile>();
	public List<IFile> configurations;
	public IFile[] featureconfigfiles;
	public IFile[] contextconfigfiles;
	private Table table;
	private Table table_1;

	/**
	 * Creates a new project reference wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public DragDrop(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		final Font font = parent.getFont();

		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout gl_composite = new GridLayout();
		gl_composite.numColumns = 2;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_REFERENCE_WIZARD_PAGE);

		final Label label = new Label(composite, SWT.NULL);
		final GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = 220;
		label.setLayoutData(gd_label);
		label.setText("&FeatureProject:");
		comboFeatureProject = new Combo(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		final GridData gd_comboFeatureProject = new GridData(GridData.BEGINNING);
		gd_comboFeatureProject.grabExcessHorizontalSpace = true;
		gd_comboFeatureProject.horizontalAlignment = SWT.FILL;
		comboFeatureProject.setLayoutData(gd_comboFeatureProject);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		final Label label2 = new Label(composite, SWT.NULL);
		label2.setText("&ContextProject:");
		comboContextProject = new Combo(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		final GridData gd_comboContextProject = new GridData(GridData.BEGINNING);
		gd_comboContextProject.grabExcessHorizontalSpace = true;
		gd_comboContextProject.horizontalAlignment = SWT.FILL;
		comboContextProject.setLayoutData(gd_comboContextProject);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		final Label referenceLabel = new Label(composite, SWT.NONE);
		referenceLabel.setText("&ContextConfig");
		referenceLabel.setFont(font);

		final Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("&FeatureConfig");

		/*
		 * referenceProjectsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		 * referenceProjectsViewer.getTable().setFont(composite.getFont());
		 * final GridData data = new GridData(SWT.LEFT, SWT.FILL, true, true);
		 * data.widthHint = 254;
		 * data.heightHint = getDefaultFontHeight(referenceProjectsViewer.getTable(), PROJECT_LIST_MULTIPLIER);
		 * referenceProjectsViewer.getTable().setLayoutData(data);
		 * referenceProjectsViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		 * referenceProjectsViewer.setContentProvider(getContentProvider());
		 * referenceProjectsViewer.setComparator(new ViewerComparator());
		 * referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());
		 */

		contextCheckboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table = contextCheckboxTableViewer.getTable();
		final GridData gd_table = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_table.widthHint = 233;
		table.setLayoutData(gd_table);
		contextCheckboxTableViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		contextCheckboxTableViewer.setContentProvider(getContextContentProvider());
		contextCheckboxTableViewer.setComparator(new ViewerComparator());
		contextCheckboxTableViewer.setInput(ResourcesPlugin.getWorkspace());

		featureCheckboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		table_1 = featureCheckboxTableViewer.getTable();
		table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		featureCheckboxTableViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		featureCheckboxTableViewer.setContentProvider(getFeatureContentProvider());
		featureCheckboxTableViewer.setComparator(new ViewerComparator());
		featureCheckboxTableViewer.setInput(ResourcesPlugin.getWorkspace());

		initialize();
		addListeners();

		setControl(composite);
		new Label(composite, SWT.NONE);

		final Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ((contextCheckboxTableViewer.getCheckedElements().length == 1) && (featureCheckboxTableViewer.getCheckedElements().length == 1)) {
					System.out.println(getReferencedContextFiles()[0].getName() + " ---> " + getReferencedFeatureFiles()[0].getName());
					try {

						adaptationconfigs.add(getReferencedContextFiles()[0]);
						adaptationconfigs.add(getReferencedFeatureFiles()[0]);
					} catch (final Exception e1) {
						System.out.println("No files added");
					}

					contextCheckboxTableViewer.remove(contextCheckboxTableViewer.getCheckedElements()[0]);
					featureCheckboxTableViewer.remove(featureCheckboxTableViewer.getCheckedElements()[0]);

				}

			}
		});
		btnNewButton.setText("Done");

	}

	private void initialize() {
		for (final IFeatureProject feature : featureProjects) {
			if (feature.getComposerID().equals("de.ovgu.featureide.core.FeatureModeling")) {
				comboFeatureProject.add(feature.getProjectName());
			}
		}
		for (final IFeatureProject feature : featureProjects) {
			if (feature.getComposerID().equals("de.ovgu.featureide.core.ContextModeling")) {
				comboContextProject.add(feature.getProjectName());
			}
		}
	}

	private void addListeners() {

		featureCheckboxTableViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				final Object[] elems = featureCheckboxTableViewer.getCheckedElements();
				featureCheckboxTableViewer.setAllChecked(false);
				if (elems.length != 0) {
					featureCheckboxTableViewer.setChecked(elems[0], true);
				}

			}

		});

		contextCheckboxTableViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				final Object[] elems = contextCheckboxTableViewer.getCheckedElements();
				contextCheckboxTableViewer.setAllChecked(false);
				if (elems.length != 0) {
					contextCheckboxTableViewer.setChecked(elems[0], true);
				}

			}

		});

		comboFeatureProject.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				if (!comboFeatureProject.getText().equalsIgnoreCase(comboFeatureProjectText)) {
					comboFeatureProjectText = comboFeatureProject.getText();

					featureProject = null;
					for (final IFeatureProject feature : featureProjects) {
						if (comboFeatureProjectText.equalsIgnoreCase(feature.getProjectName())) {
							comboFeatureProjectAddress = feature.getConfigPath();
							configurations = feature.getAllConfigurations();
							featureconfigfiles = configurations.toArray(new IFile[configurations.size()]);

							featureProject = feature;
							featureCheckboxTableViewer.refresh();
						}
					}
					if (featureProject != null) {

					}

				}

			}
		});

		comboContextProject.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				if (!comboContextProject.getText().equalsIgnoreCase(comboContextProjectText)) {
					comboContextProjectText = comboContextProject.getText();

					contextProject = null;
					for (final IFeatureProject feature : featureProjects) {
						if (comboContextProjectText.equalsIgnoreCase(feature.getProjectName())) {
							comboContextProjectAddress = feature.getConfigPath();
							configurations = feature.getAllConfigurations();
							contextconfigfiles = configurations.toArray(new IFile[configurations.size()]);

							contextProject = feature;
							contextCheckboxTableViewer.refresh();
						}
					}
					if (contextProject != null) {

					}

				}

			}
		});

	}

	/**
	 * Returns a content provider for the reference project
	 * viewer. It will return all projects in the workspace.
	 *
	 * @return the content provider
	 */
	protected IStructuredContentProvider getContextContentProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object element) {
				if (!(element instanceof IWorkspace)) {
					return new Object[0];
				}
				// final IProject[] projects = ((IWorkspace) element).getRoot().getProjects();

				return contextconfigfiles == null ? new Object[0] : contextconfigfiles;
			}
		};
	}

	protected IStructuredContentProvider getFeatureContentProvider() {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object element) {
				if (!(element instanceof IWorkspace)) {
					return new Object[0];
				}
				// final IProject[] projects = ((IWorkspace) element).getRoot().getProjects();

				return featureconfigfiles == null ? new Object[0] : featureconfigfiles;
			}
		};
	}

	/**
	 * Get the defualt widget height for the supplied control.
	 *
	 * @return int
	 * @param control - the control being queried about fonts
	 * @param lines - the number of lines to be shown on the table.
	 */
	private static int getDefaultFontHeight(Control control, int lines) {
		final FontData[] viewerFontData = control.getFont().getFontData();
		int fontHeight = 10;

		// If we have no font data use our guess
		if (viewerFontData.length > 0) {
			fontHeight = viewerFontData[0].getHeight();
		}
		return lines * fontHeight;

	}

	/**
	 * Initializes the combo containing all feature projects.<br> Selects the feature project corresponding to the selected resource.
	 */

	/**
	 * Returns the referenced projects selected by the user.
	 *
	 * @return the referenced projects
	 */
	public IFile[] getReferencedContextFiles() {
		final Object[] elements = contextCheckboxTableViewer.getCheckedElements();
		final IFile[] contextconfigfiles = new IFile[elements.length];
		System.arraycopy(elements, 0, contextconfigfiles, 0, elements.length);
		return contextconfigfiles;
	}

	public IFile[] getReferencedFeatureFiles() {
		final Object[] elements = featureCheckboxTableViewer.getCheckedElements();
		final IFile[] featureconfigfiles = new IFile[elements.length];
		System.arraycopy(elements, 0, featureconfigfiles, 0, elements.length);
		return featureconfigfiles;
	}
}
