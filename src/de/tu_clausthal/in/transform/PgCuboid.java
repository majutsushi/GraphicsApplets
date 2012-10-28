/*
 * GraphicsApplets/PgCuboid.java - Part of the Transform Applet
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

public class PgCuboid extends PgElementSet {

	protected double m_lengthX, m_lengthY, m_lengthZ;
	protected int m_numVertices = 8;
	protected int m_numSides = 6;

	public PgCuboid() {
		this(1., 1., 1.);
	}

	public PgCuboid(double x, double y, double z) {
		super(3);
		init();

		setNumVertices(m_numVertices);

		// set the eight corners of the cuboid
		setVertex(0, -x/2, -y/2, -z/2);
		setVertex(1,  x/2, -y/2, -z/2);
		setVertex(2,  x/2,  y/2, -z/2);
		setVertex(3, -x/2,  y/2, -z/2);

		setVertex(4, -x/2, -y/2,  z/2);
		setVertex(5,  x/2, -y/2,  z/2);
		setVertex(6,  x/2,  y/2,  z/2);
		setVertex(7, -x/2,  y/2,  z/2);

		makeElements();
		makeElementNormals();
		makeVertexNormals();
	}

	private void makeElements() {
		setDimOfElements(4);
		setNumElements(m_numSides);

		// connect the corners to planes
		setElement(0, 3, 2, 1, 0);
		setElement(1, 4, 5, 6, 7);
		setElement(2, 0, 1, 5, 4);
		setElement(3, 1, 2, 6, 5);
		setElement(4, 2, 3, 7, 6);
		setElement(5, 3, 0, 4, 7);

		makeNeighbour();
	}
}
