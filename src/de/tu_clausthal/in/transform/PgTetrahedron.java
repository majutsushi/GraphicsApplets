/*
 * GraphicsApplets/PgTetrahedron.java - Part of the Transform Applet
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

public class PgTetrahedron extends PgElementSet {

	protected double m_h, m_R, m_r;
	protected int m_numVertices = 4;
	protected int m_numSides = 4;

	public PgTetrahedron() {
		this(1.);
	}

	public PgTetrahedron(double a) {
		super(3);
		init();

		setNumVertices(m_numVertices);

		m_h = (Math.sqrt(3)/2.)  * a;
		m_R = (Math.sqrt(6)/4.)  * a;
		m_r = (Math.sqrt(6)/12.) * a;

		// set the four corners of the tetrahedron
		setVertex(0, -((2./3.) * m_h),    0, -m_r);
		setVertex(1,    (1./3.) * m_h, -0.5, -m_r);
		setVertex(2,    (1./3.) * m_h,  0.5, -m_r);
		setVertex(3,                0,    0,  m_R);

		makeElements();
		makeElementNormals();
		makeVertexNormals();
	}

	private void makeElements() {
		setDimOfElements(3);
		setNumElements(m_numSides);

		// connect the corners to planes
		setElement(0, 2, 1, 0);
		setElement(1, 3, 0, 1);
		setElement(2, 3, 1, 2);
		setElement(3, 3, 2, 0);

		makeNeighbour();
	}

}
