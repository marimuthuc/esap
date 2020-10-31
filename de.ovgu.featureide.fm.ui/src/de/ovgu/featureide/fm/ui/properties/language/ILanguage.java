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
package de.ovgu.featureide.fm.ui.properties.language;

/**
 * Interface of the extension point <code>"de.ovgu.featureide.fm.core.language"</code>
 *
 * @author Jens Meinicke
 */
public interface ILanguage {

	/**
	 * @return The translation for: REDUNDANT_CONSTRAINT
	 */
	public String getRedundantConst();

	/**
	 * @return The translation for: IMPLICIT_CONSTRAINT
	 */
	public String getImplicitConst();

	/**
	 * @return The translation for: UNSATISFIABLE_CONSTRAINT
	 */
	public String getUnsatisfiableConst();

	/**
	 * @return The translation for: CONSTRAINT_IS_TAUTOLOGY
	 */
	public String getTautologyConst();

	/**
	 * @return The translation for: CONSTRAINT_MAKES_THE_MODEL_VOID
	 */
	public String getVoidModelConst();

	/**
	 * @return The translation for: CONSTRAINT_MAKES_THE_MODEL_VOID
	 */
	public String getVoidModel();

	/**
	 * @return The translation for: INDETERMINATE_HIDDEN
	 */
	public String getIndetHidden();

	public String getDisEhungry();

	/**
	 * @return The title of the legend
	 */
	public String getLagendTitle();

	/**
	 * @return The name of the language
	 */
	public String getName();

	/**
	 * @return The word for: MANDATORY
	 */
	public String getMandatory();

	/**
	 * @return The word for: ABSTRACT
	 */
	public String getAbstract();

	/**
	 * @return The word for: "imported"
	 */
	public String getImported();

	/**
	 * @return The word for: "inherited"
	 */
	public String getInherited();

	/**
	 * @return The word for: "interfaced"
	 */
	public String getInterfaced();

	/**
	 * @return The word for: "concrete"
	 */
	public String getConcrete();

	public String getEnergyHungry();

	public String getContextModeling();

	public String getEnergyFriendly();

	/**
	 * @return The word for: "Hidden"
	 */
	public String getHidden();

	/**
	 * @return The word for: "Collapsed"
	 */
	public String getCollapsed();

	/**
	 * @return The translation for: "Dead"
	 */
	public String getDead();

	/**
	 * @return The translation for: "False Optional"
	 */
	public String getFalseOptional();

	/**
	 * @return The word for: "alternative"
	 */
	public String getAlternative();

	/**
	 * @return The word for: Alternative Group
	 */
	public String getAlternativeGroup();

	/**
	 * @return The word for: "or"
	 */
	public String getOr();

	public String getEnergyEffAlt();

	/**
	 * @return The word for: Or Group
	 */
	public String getOrGroup();

	/**
	 * @return The word for: OPTIONAL
	 */
	public String getOptional();

	public String getExplanation();

	public String getLikelyCause();

	public String getUnlikelyCause();

	public String getFeature();

	/**
	 * @return
	 */
	public String getPrimary();

	/**
	 * @return
	 */
	public String getSecondary();

	/**
	 * @return
	 */
	public String getUser();

}
