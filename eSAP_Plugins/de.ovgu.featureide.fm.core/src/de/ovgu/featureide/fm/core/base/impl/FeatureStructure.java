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
package de.ovgu.featureide.fm.core.base.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import de.ovgu.featureide.fm.core.FeatureStatus;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent;
import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent.EventType;

/**
 * All structural information of an {@link IFeatureModel}.
 *
 * @author Sebastian Krieter
 * @author Marcus Pinnecke
 */
public class FeatureStructure implements IFeatureStructure {

	public static String COMPID;
	protected boolean and;
	protected boolean composercontext;
	protected boolean iscontext;

	protected final LinkedList<IFeatureStructure> children = new LinkedList<>();
	protected boolean concrete;
	protected boolean energyhungry;

	protected final IFeature correspondingFeature;

	protected boolean hidden;

	protected boolean mandatory;
	protected boolean multiple;

	protected boolean primary;
	protected boolean secondary;
	protected boolean user;

	protected IFeatureStructure parent = null;
	protected List<IConstraint> partOfConstraints = new LinkedList<>();

	protected FeatureStructure(FeatureStructure oldStructure, IFeatureModel newFeatureModel) {
		if (newFeatureModel != null) {
			correspondingFeature = oldStructure.correspondingFeature.clone(newFeatureModel, this);
			newFeatureModel.addFeature(correspondingFeature);
		} else {
			correspondingFeature = oldStructure.correspondingFeature;
		}

		mandatory = oldStructure.mandatory;
		concrete = oldStructure.concrete;
		energyhungry = oldStructure.energyhungry;
		iscontext = oldStructure.iscontext;
		composercontext = oldStructure.composercontext;
		and = oldStructure.and;
		multiple = oldStructure.multiple;
		hidden = oldStructure.hidden;

		primary = oldStructure.primary;
		secondary = oldStructure.secondary;
		user = oldStructure.user;

		for (final IFeatureStructure child : oldStructure.children) {
			addNewChild(child.cloneSubtree(newFeatureModel));
		}

	}

	public FeatureStructure(IFeature correspondingFeature) {
		this.correspondingFeature = correspondingFeature;

		mandatory = true; // prev impl false
		composercontext = false;
		concrete = true;
		energyhungry = false;
		iscontext = false;
		and = false;   // prev impl true
		multiple = true;  // prev impl false
		hidden = false;
		primary = true;
		secondary = false;
		user = false;

	}

	@Override
	public void addChild(IFeatureStructure newChild) {
		addNewChild(newChild);
		fireChildrenChanged();
	}

	@Override
	public void addChildAtPosition(int index, IFeatureStructure newChild) {
		if (index > children.size()) {
			children.add(newChild);
		} else {
			children.add(index, newChild);
		}
		newChild.setParent(this);
	}

	protected void addNewChild(IFeatureStructure newChild) {

		children.add(newChild);
		newChild.setParent(this);

	}

	@Override
	public void changeToAlternative() {
		if (getChildrenCount() <= 1) {
			return;
		}
		and = false;
		multiple = false;
		fireChildrenChanged();
	}

	@Override
	public void changeToAnd() {

		and = true;
		multiple = false;
		fireChildrenChanged();
	}

	@Override
	public void changeToOr() {

		if ((getChildrenCount() <= 1)) {
			return;
		}
		and = false;
		multiple = true;
		fireChildrenChanged();
	}

	@Override
	public IFeatureStructure cloneSubtree(IFeatureModel newFeatureModel) {
		return new FeatureStructure(this, newFeatureModel);
	}

	protected void fireAttributeChanged() {
		final FeatureIDEEvent event = new FeatureIDEEvent(this, EventType.ATTRIBUTE_CHANGED);
		correspondingFeature.fireEvent(event);
	}

	protected void fireChildrenChanged() {
		final FeatureIDEEvent event = new FeatureIDEEvent(this, EventType.GROUP_TYPE_CHANGED, Boolean.FALSE, Boolean.TRUE);
		correspondingFeature.fireEvent(event);
	}

	protected void fireHiddenChanged() {
		final FeatureIDEEvent event = new FeatureIDEEvent(this, EventType.HIDDEN_CHANGED, Boolean.FALSE, Boolean.TRUE);
		correspondingFeature.fireEvent(event);
	}

	protected void fireMandatoryChanged() {
		final FeatureIDEEvent event = new FeatureIDEEvent(this, EventType.MANDATORY_CHANGED, Boolean.FALSE, Boolean.TRUE);
		correspondingFeature.fireEvent(event);
	}

	protected void fireParentChanged() {
		final FeatureIDEEvent event = new FeatureIDEEvent(this, EventType.PARENT_CHANGED, Boolean.FALSE, Boolean.TRUE);
		correspondingFeature.fireEvent(event);
	}

	@Override
	public int getChildIndex(IFeatureStructure feature) {
		return children.indexOf(feature);
	}

	@Override
	public List<IFeatureStructure> getChildren() {	// Changed type LinkedList to List, Marcus Pinnecke 30.08.15
		return children;
	}

	@Override
	public boolean hasVisibleChildren(boolean showHiddenFeature) {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if ((!child.hasHiddenParent() || showHiddenFeature)) {
				check = true;
			}
		}
		return check;
	}

	public boolean hasEnergyHungry() {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if (child.isEnergyHungry()) {
				check = true;
				break;
			}
		}
		return check;
	}

	public boolean hasPrimary() {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if (child.isPrimary()) {
				check = true;
				break;
			}
		}
		return check;
	}

	public boolean hasSecondary() {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if (child.isSecondary()) {
				check = true;
				break;
			}
		}
		return check;
	}

	public boolean hasUser() {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if (child.isUser()) {
				check = true;
				break;
			}
		}
		return check;
	}

	public boolean hasContextModeling() {
		boolean check = false;
		for (final IFeatureStructure child : children) {
			if (child.isContextModeling()) {
				check = true;
				break;
			}
		}
		return check;
	}

	@Override
	public int getChildrenCount() {
		return children.size();
	}

	@Override
	public IFeature getFeature() {
		return correspondingFeature;
	}

	@Override
	public IFeatureStructure getFirstChild() {
		if (children.isEmpty()) {
			return null;
		}
		return children.get(0);
	}

	@Override
	public IFeatureStructure getLastChild() {
		if (!children.isEmpty()) {
			return children.getLast();
		}
		return null;
	}

	@Override
	public IFeatureStructure getParent() {
		return parent;
	}

	@Override
	public Collection<IConstraint> getRelevantConstraints() {
		return partOfConstraints;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public boolean hasHiddenParent() {

		if (isHidden()) {
			return true;
		}
		if (isRoot()) {

			return false;
		}
		IFeatureStructure p = getParent();

		while (!p.isRoot()) {
			if (p.isHidden()) {
				return true;
			}
			p = p.getParent();

		}

		return false;
	}

	/**
	 * Returns true if the rule can be writen in a format like 'Ab [Cd] Ef :: Gh'.
	 */
	@Override
	public boolean hasInlineRule() {
		return (getChildrenCount() > 1) && and && isMandatory() && !multiple;
	}

	@Override
	public boolean isAbstract() {
		if (isComposerContext()) {
			return false;
		}
		return !isConcrete();
	}

	@Override
	public boolean isPrimary() {
		if (!isComposerContext()) {
			return false;
		}
		if (isRoot() || !parent.isRoot()) {
			return false;
		}
		if (primary) {
			setAlternative();
			fireAttributeChanged();
		}

		return primary;
	}

	@Override
	public boolean isSecondary() {
		if (!isComposerContext()) {
			return false;
		}
		if (isRoot() || !parent.isRoot()) {
			return false;
		}
		if (secondary) {
			setAlternative();
			fireAttributeChanged();
		}
		return secondary;
	}

	@Override
	public boolean isUser() {
		if (!isComposerContext()) {
			return false;
		}
		if (isRoot() || !parent.isRoot()) {
			return false;
		}
		if (user) {
			setAlternative();
			fireAttributeChanged();
		}
		return user;
	}

	@Override
	public boolean isAlternative() {

		return (!and && !multiple && (getChildrenCount() > 1)) || isPrimary() || isSecondary() || isUser();
	}

	@Override
	public boolean isAncestorOf(IFeatureStructure parent) {
		IFeatureStructure currParent = getParent();
		while (currParent != null) {
			if (parent == currParent) {
				return true;
			}
			currParent = currParent.getParent();
		}
		return false;
	}

	@Override
	public boolean isAnd() {

		return and || (getChildrenCount() <= 1);
	}

	@Override
	public boolean isANDPossible() {

		if ((parent == null) || parent.isAnd()) {
			return false;
		}
		for (final IFeatureStructure child : children) {
			if (child.isAnd()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isConcrete() {
		if (isComposerContext()) {
			return false;
		}
		return concrete;
	}

	@Override
	public boolean isContextModeling() {
		return false;
	}

	@Override
	public boolean isComposerContext() {
		if (isRoot()) {

			return composercontext;

		} else {
			return parent.isComposerContext();
		}
	}

	@Override
	public boolean isEnergyHungry() {
		if (isComposerContext()) {
			return false;
		}
		if (isRoot()) {
			return false;
		}

		if (energyhungry && !isEnergyEffAlt() && isMandatorySet() && (getChildrenCount() == 0)) {
			setHidden(true);
			// fireChildrenChanged();
			correspondingFeature.getProperty().setFeatureStatus(FeatureStatus.DISALLOWED_EHUNGRY);
		}
		return energyhungry && !isEnergyEffAlt();

		// return energyhungry;
	}

	@Override
	public boolean isEnergyFriendly() {
		if (isComposerContext()) {
			return false;
		}
		if (isRoot()) {
			return false;
		}

		return !isEnergyHungry() && !isEnergyEffAlt();

	}

	@Override
	public boolean isEnergyEffAlt() {
		if (isComposerContext()) {
			return false;
		}
		if (isRoot()) {
			return false;
		}
		if ((parent != null) && parent.isRoot() && parent.isAbstract()) {
			return false;
		}
		if ((parent != null) && parent.isEnergyHungry() && parent.isMandatory()) {
			if (parent.getChildrenCount() >= 1) {
				parent.setHidden(false);
				parent.setAlternative();
				fireChildrenChanged();
			}
			return true;
		} else {
			return false;
		}

		// return false;
	}

	@Override
	public boolean isFirstChild(IFeatureStructure child) {
		return children.indexOf(child) == 0;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public boolean isMandatory() {

		return (parent == null) || !parent.isAnd() || mandatory;
	}

	@Override
	public boolean isMandatorySet() {
		return mandatory;
	}

	@Override
	public boolean isMultiple() {

		return multiple && (getChildrenCount() > 1);
	}

	@Override
	public boolean isOr() {

		return !and && multiple && (getChildrenCount() > 1);
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public void removeChild(IFeatureStructure child) {
		if (!children.remove(child)) {
			throw new NoSuchElementException();
		}
		child.setParent(null);
		fireChildrenChanged();
	}

	@Override
	public IFeatureStructure removeLastChild() {
		final IFeatureStructure child = children.removeLast();
		child.setParent(null);
		fireChildrenChanged();
		return child;
	}

	@Override
	public void replaceChild(IFeatureStructure oldChild, IFeatureStructure newChild) {
		final int index = children.indexOf(oldChild);
		children.set(index, newChild);
		oldChild.setParent(null);
		newChild.setParent(this);
		fireChildrenChanged();
	}

	@Override
	public void setAbstract(boolean value) {
		concrete = !value;
		fireAttributeChanged();
	}

	@Override
	public void setContext(boolean value) {
		iscontext = value;
		fireAttributeChanged();
	}

	@Override
	public void setComposerContext(boolean value) {
		composercontext = value;
		fireAttributeChanged();
	}

	@Override
	public void setPrimary() {
		if (!isComposerContext() || isRoot() || !parent.isRoot()) {
			return;
		}
		primary = true;
		secondary = false;
		user = false;
		setAlternative();
		fireAttributeChanged();
	}

	@Override
	public void setSecondary() {
		if (!isComposerContext() || isRoot() || !parent.isRoot()) {
			return;
		}
		primary = false;
		secondary = true;
		user = false;
		mandatory = false;
		setAlternative();
		fireAttributeChanged();
	}

	@Override
	public void setUser() {
		if (!isComposerContext() || isRoot() || !parent.isRoot()) {
			return;
		}
		primary = false;
		secondary = false;
		user = true;
		mandatory = true;
		setAlternative();
		fireAttributeChanged();
	}

	@Override
	public void setEnergyHungryTag(boolean value) {

		energyhungry = value;

		fireAttributeChanged();
	}

	@Override
	public void setEnergyHungry(boolean value) {
		if (isComposerContext()) {
			return;
		}
		if ((parent != null) && parent.isEnergyFriendly() && value) {
			return;
		}
		if (!value && hasEnergyHungry()) {
			return;
		}

		mandatory = false;
		energyhungry = value;

		fireAttributeChanged();
	}

	@Override
	public void setEnergyFriendly(boolean value) {

		energyhungry = !value;

		fireAttributeChanged();
	}

	@Override
	public void setAlternative() {
		if (isRoot() && isComposerContext()) {
			return;
		}
		and = false;
		multiple = false;
	}

	@Override
	public void setAnd() {
		and = true;
	}

	@Override

	public void setAND(boolean and) {
		this.and = and;
		fireChildrenChanged();
	}

	@Override
	public void setChildren(List<IFeatureStructure> children) {	// Changed type LinkedList to List, Marcus Pinnecke 30.08.15
		this.children.clear();
		for (final IFeatureStructure child : children) {
			addNewChild(child);
		}

		fireChildrenChanged();
	}

	@Override
	public void setHidden(boolean hid) {
		hidden = hid;
		fireHiddenChanged();
	}

	@Override
	public void setMandatory(boolean mandatory) {
		if (!mandatory && (correspondingFeature.getProperty().getFeatureStatus() == FeatureStatus.DISALLOWED_EHUNGRY)) {
			setHidden(false);
			// fireChildrenChanged();
		}
		if (isPrimary()) {
			return;
		}
		this.mandatory = mandatory;
		fireMandatoryChanged();
	}

	@Override
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
		fireChildrenChanged();
	}

	@Override
	public void setOr() {
		if (isRoot() && isComposerContext()) {
			and = false;
		}
		multiple = true;
	}

	@Override
	public void setParent(IFeatureStructure newParent) {
		if (newParent == parent) {
			return;
		}
		parent = newParent;
	}

	@Override
	public void setRelevantConstraints() {
		final List<IConstraint> constraintList = new LinkedList<>();
		for (final IConstraint constraint : correspondingFeature.getFeatureModel().getConstraints()) {
			for (final IFeature f : constraint.getContainedFeatures()) {
				if (f.getName().equals(correspondingFeature.getName())) {
					constraintList.add(constraint);
					break;
				}
			}
		}
		partOfConstraints = constraintList;
	}

	@Override
	public void setRelevantConstraints(List<IConstraint> constraints) {
		partOfConstraints = constraints;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FeatureStructure=(");
		FeatureUtils.print(getFeature(), sb);
		sb.append(")");
		return sb.toString();
	}

}
