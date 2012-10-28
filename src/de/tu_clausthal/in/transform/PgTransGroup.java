/*
 * GraphicsApplets/PgTransGroup.java - Part of the Transform Applet
 * Copyright (C) 2009 Jan Larres
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tu_clausthal.in.transform;

import jv.geom.PgElementSet;

// collect all three geometries needed for one solid in one class
public class PgTransGroup {
	private String name;
	private PgElementSet unitGeom, transGeom, stackGeom;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PgElementSet getUnitGeom() {
		return unitGeom;
	}

	public void setUnitGeom(PgElementSet unitGeom) {
		this.unitGeom = unitGeom;
	}

	public PgElementSet getTransGeom() {
		return transGeom;
	}

	public void setTransGeom(PgElementSet transGeom) {
		this.transGeom = transGeom;
	}
	
	public PgElementSet getStackGeom() {
		return stackGeom;
	}

	public void setStackGeom(PgElementSet stackGeom) {
		this.stackGeom = stackGeom;
	}

	public void update() {
		unitGeom.update(unitGeom);
		transGeom.update(transGeom);
		stackGeom.update(stackGeom);
	}
}
