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
package de.ovgu.featureide.fm.ui.editors.featuremodel.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.editors.featuremodel.operations.SetFeatureToSecondaryOperation;

/**
 * Action to mark a feature as Secondary.
 *
 * @author Marcus Pinnecke (Feature Interface)
 * @author Chico Sundermann
 * @author Paul Westphal
 */
public class SecondaryAction extends MultipleSelectionAction {

	public static final String ID = "de.ovgu.featureide.Secondary";

	private final IFeatureModel featureModel;

	public SecondaryAction(Object viewer, IFeatureModel featureModel) {
		super("Secondary", viewer, ID);
		this.featureModel = featureModel;
	}

	@Override
	public void run() {
		changeSecondaryStatus(isEveryFeatureSecondary());
		setChecked(isEveryFeatureSecondary());
	}

	private boolean isEveryFeatureSecondary() {
		for (final IFeature tempFeature : featureArray) {
			if (!(tempFeature.getStructure().isSecondary())) {
				return false;
			}
		}
		return true;
	}

	private void changeSecondaryStatus(boolean allSecondary) {
		final SetFeatureToSecondaryOperation op = new SetFeatureToSecondaryOperation(featureModel, allSecondary, getSelectedFeatures());
		try {
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, null, null);
		} catch (final ExecutionException e) {
			FMUIPlugin.getDefault().logError(e);
		}
	}

	@Override
	protected void updateProperties() {
		setEnabled(true);
		// A selection of features is considered hidden if every feature is hidden.
		setChecked(isEveryFeatureSecondary());
	}

}
