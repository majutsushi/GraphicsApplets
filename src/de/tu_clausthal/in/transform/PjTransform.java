/*
 * GraphicsApplets/PjTransform.java - Part of the Transform Applet
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;

import javax.swing.UIManager;

import jv.geom.PgElementSet;
import jv.object.PsPanel;
import jv.project.PjProject;
import jv.project.PvDisplayIf;
import jv.project.PvLightIf;
import jv.vecmath.PdMatrix;
import jv.vecmath.PdVector;
import jv.viewer.PvCamera;

public class PjTransform extends PjProject {

	protected PsPanel m_pjTransformIP;

	protected PgTransGroup m_cube, m_sphere, m_tetra, m_cone, m_cylinder;

	// save all the solids in a HashSet so that we can iterate over them
	private HashSet<PgTransGroup> m_transGroups;

	public PjTransform() {
		super("Transform");

		m_transGroups = new HashSet<PgTransGroup>(5);

		m_cube = new PgTransGroup();
		m_cube.setName("Cube");
		PgCuboid m_cubeUnit = new PgCuboid();
		m_cubeUnit.setName("CubeUnit");
		m_cube.setUnitGeom(m_cubeUnit);
		PgCuboid m_cubeTrans = new PgCuboid();
		m_cubeTrans.setName("CubeTrans");
		m_cube.setTransGeom(m_cubeTrans);
		PgCuboid m_cubeStack = new PgCuboid();
		m_cubeStack.setName("CubeStack");
		m_cube.setStackGeom(m_cubeStack);
		m_transGroups.add(m_cube);

		m_sphere = new PgTransGroup();
		m_sphere.setName("Sphere");
		PgElementSet m_sphereUnit = new PgElementSet(3);
		m_sphereUnit.computeSphere(20, 20, 1);
		m_sphereUnit.setName("SphereUnit");
		m_sphere.setUnitGeom(m_sphereUnit);
		PgElementSet m_sphereTrans = new PgElementSet(3);
		m_sphereTrans.copy(m_sphereUnit);
		m_sphereTrans.setName("SphereTrans");
		m_sphere.setTransGeom(m_sphereTrans);
		PgElementSet m_sphereStack = new PgElementSet(3);
		m_sphereStack.copy(m_sphereUnit);
		m_sphereStack.setName("SphereStack");
		m_sphere.setStackGeom(m_sphereStack);
		m_transGroups.add(m_sphere);

		m_tetra = new PgTransGroup();
		m_tetra.setName("Tetra");
		PgTetrahedron m_tetraUnit = new PgTetrahedron();
		m_tetraUnit.setName("TetraUnit");
		m_tetra.setUnitGeom(m_tetraUnit);
		PgTetrahedron m_tetraTrans = new PgTetrahedron();
		m_tetraTrans.setName("TetraTrans");
		m_tetra.setTransGeom(m_tetraTrans);
		PgTetrahedron m_tetraStack = new PgTetrahedron();
		m_tetraStack.setName("TetraStack");
		m_tetra.setStackGeom(m_tetraStack);
		m_transGroups.add(m_tetra);

		m_cone = new PgTransGroup();
		m_cone.setName("Cone");
		PgElementSet m_coneUnit = new PgElementSet(3);
		m_coneUnit.computeCone(20, 20, 0.5, 1);
		m_coneUnit.setName("ConeUnit");
		m_cone.setUnitGeom(m_coneUnit);
		PgElementSet m_coneTrans = new PgElementSet(3);
		m_coneTrans.copy(m_coneUnit);
		m_coneTrans.setName("ConeTrans");
		m_cone.setTransGeom(m_coneTrans);
		PgElementSet m_coneStack = new PgElementSet(3);
		m_coneStack.copy(m_coneUnit);
		m_coneStack.setName("ConeStack");
		m_cone.setStackGeom(m_coneStack);
		m_transGroups.add(m_cone);

		m_cylinder = new PgTransGroup();
		m_cylinder.setName("Cylinder");
		PgElementSet m_cylinderUnit = new PgElementSet(3);
		m_cylinderUnit.computeCylinder(20, 20, 0.5, 1);
		m_cylinderUnit.setName("CylinderUnit");
		m_cylinder.setUnitGeom(m_cylinderUnit);
		PgElementSet m_cylinderTrans = new PgElementSet(3);
		m_cylinderTrans.copy(m_cylinderUnit);
		m_cylinderTrans.setName("CylinderTrans");
		m_cylinder.setTransGeom(m_cylinderTrans);
		PgElementSet m_cylinderStack = new PgElementSet(3);
		m_cylinderStack.copy(m_cylinderUnit);
		m_cylinderStack.setName("CylinderStack");
		m_cylinder.setStackGeom(m_cylinderStack);
		m_transGroups.add(m_cylinder);

		if (getClass() == PjTransform.class)
			init();
	}

	public void init() {
		super.init();
		for (PgTransGroup g : m_transGroups) {
			g.getUnitGeom().setGlobalEdgeColor(Color.BLACK);
			g.getUnitGeom().setGlobalElementColor(Color.BLACK);
			g.getUnitGeom().showElements(false);
			g.getTransGeom().setGlobalEdgeColor(Color.RED);
			g.getTransGeom().setGlobalElementColor(Color.RED);
			g.getTransGeom().showElements(false);
		}
	}

	public void start() {
		for (PgTransGroup g : m_transGroups) {
			PgElementSet geomU = g.getUnitGeom();
			PgElementSet geomT = g.getTransGeom();

			geomU.setVisible(false);
			geomT.setVisible(false);
			g.update();

			addGeometry(geomU);
			addGeometry(geomT);
		}

		showSolid(m_cube);

		PvDisplayIf disp = getDisplay();
		if (disp != null) {
			((Component)disp).addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					PvDisplayIf d = (PvDisplayIf)e.getComponent();
					PvCamera cam = (PvCamera)d.getCamera();
					cam.setDist(cam.getDist() + e.getWheelRotation());
					d.update(d);
				}
			});
			disp.setBackgroundColor((Color)UIManager.get("Panel.background"));
			disp.setLightingModel(PvLightIf.MODEL_SURFACE);
			disp.setEnabledZBuffer(true);
			disp.showFrame(true);
		}

		super.start();

		// for some reason this gets ignored sometimes ...
		PvCamera m_camera = (PvCamera)disp.getCamera();
		m_camera.setDist(5.);

		m_pjTransformIP = getInfoPanel();
	}

	// switch to the specified solid
	public void showSolid(PgTransGroup group) {
		for (PgTransGroup g : m_transGroups) {
			if (g == group) {
				g.getUnitGeom().setVisible(true);
				g.getTransGeom().setVisible(true);
			} else {
				g.getUnitGeom().setVisible(false);
				g.getTransGeom().setVisible(false);
			}
			g.update();
		}
	}

	// transform all solids using the given matrix
	public void transformSolid(PdMatrix m) {
		int numV;
		PdVector vu, vun;

		for (PgTransGroup g : m_transGroups) {
			numV = g.getStackGeom().getNumVertices();

			for (int i = 0; i < numV; i++) {
				vu = g.getStackGeom().getVertex(i);
				vun = new PdVector(vu.getEntry(0), vu.getEntry(1), vu.getEntry(2), 1);
				vun.leftMultMatrix(m);
				double w = vun.getEntry(3);
				vu = new PdVector(vun.getEntry(0)/w, vun.getEntry(1)/w, vun.getEntry(2)/w);
				g.getTransGeom().setVertex(i, vu);
			}
			g.update();
		}
	}

	// switch between filled and non-filled mode for the solids
	public void showElements(boolean filled) {
		PgElementSet u, t;
		PvDisplayIf disp = getDisplay();
		if (filled) {
			disp.setLightingModel(PvLightIf.MODEL_LIGHT);
			for (PgTransGroup g : m_transGroups) {
				u = g.getUnitGeom();
				t = g.getTransGeom();
				u.showElements(true);
				u.setGlobalEdgeColor(Color.GRAY);
				t.showElements(true);
				t.setGlobalEdgeColor(Color.BLACK);
				g.update();
			}
		} else {
			disp.setLightingModel(PvLightIf.MODEL_SURFACE);
			for (PgTransGroup g : m_transGroups) {
				u = g.getUnitGeom();
				t = g.getTransGeom();
				u.showElements(false);
				u.setGlobalEdgeColor(Color.BLACK);
				t.showElements(false);
				t.setGlobalEdgeColor(Color.RED);
				g.update();
			}
		}
	}

	// push a transformed solid onto the stack
	public void stackPush() {
		for (PgTransGroup g : m_transGroups) {
			g.getStackGeom().copy(g.getTransGeom());
			g.update();
		}
	}

	// set the stack to the unit geometry, effectively clearing it
	public void stackSetUnit() {
		for (PgTransGroup g : m_transGroups) {
			g.getStackGeom().copy(g.getUnitGeom());
			g.update();
		}
	}

	public boolean update(Object event) {
		// If we do not know about the event then just forward it to the superclass.
		return super.update(event);
	}
}
