/*
 * GraphicsApplets/PjClipping.java - Part of the Clipping Applet
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

package de.tu_clausthal.in.clipping;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import jv.geom.PgElementSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.project.PjProject;
import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.project.PvLightIf;
import jv.project.PvPickEvent;
import jv.vecmath.PdVector;
import jv.viewer.PvCamera;

public class PjClipping extends PjProject {

	// width/height of the grid. don't set it too large since
	// it has to be redrawn quite often.
	private static final int GRIDSIZE = 50;
	// ensure that the grid always fills the display. if you enlarge the grid,
	// also increase ZOOM_MAX accordingly.
	private int ZOOM_MIN = 5;
	private int ZOOM_MAX = 45;

	private PgElementSet m_grid;

	private Color[] m_gridColors;
	private Color[][] m_gridSideColors;

	protected enum ClipMode { MOVE, ADD, DELETE };
	private ClipMode m_currentMode;

	// the info panel
	protected PjClipping_IP m_pjClippingIP;

	// this saves the rectangle and the user-created polygon
	// they are both in the same structure so that they both can be
	// moved in 'move' mode (javaview limitation)
	private PgPolygonSet m_polygons;
	// the polygon that during the clipping steps shows how the clipped
	// polygon will look like
	private PgPolygonSet m_stepPolygon;
	// this saves the original, unclipped polygon so you can jump
	// back to it
	private PgPolygonSet m_polygonsOrig;
	// this saves the clipped polygons after each step so you can jump
	// back and forth to them
	private PgPolygonSet[] m_stepPolygons;

	private Color m_backgroundColor, m_rectangleColor, m_polygonColor, m_polygonStepColor;

	private boolean m_q0inside, m_q1inside;
	private int m_sideFinished;

	// the current clipping step of a side of the rectangle
	private int m_clipStep;
	// the currently clipped side of the rectangle
	private int m_rectSide;
	// whether the clipping is complete (after all four sides)
	private boolean m_clipEnd;

	public PjClipping() {
		super("Clipping");

		m_grid = new PgElementSet(2);
		m_grid.setName("Grid");

		m_polygons = new PgPolygonSet(2);
		m_polygons.setName("Polygons");

		m_polygonsOrig = new PgPolygonSet(2);
		m_polygonsOrig.setName("PolygonsOrig");

		m_stepPolygon = new PgPolygonSet(2);
		m_stepPolygon.setName("StepPolygon");

		m_stepPolygons = new PgPolygonSet[4];
		for (int i = 0; i < 4; i++) {
			m_stepPolygons[i] = new PgPolygonSet(2);
			m_stepPolygons[i].setName("StepPolygon" + i);
		}

		if (getClass() == PjClipping.class)
			init();
	}

	public void init() {
		super.init();

		m_currentMode = ClipMode.ADD;

		m_backgroundColor = new Color(238, 238, 238);
		m_rectangleColor = Color.BLUE;
		m_polygonColor = Color.BLACK;
		m_polygonStepColor = Color.YELLOW;
		m_clipStep = 0;
		m_rectSide = 0;
		m_clipEnd = false;
		m_q0inside = m_q1inside = false;
		m_sideFinished = 0;

		initGrid();

		m_polygons.setNumVertices(4);
		m_polygons.showPolygons(false);
		m_polygons.showVertices(true);
		m_polygons.setGlobalVertexSize(5.);
		m_polygons.setVertex(0, 15, 15);
		m_polygons.setVertex(1, 28, 15);
		m_polygons.setVertex(2, 28, 26);
		m_polygons.setVertex(3, 15, 26);
		m_polygons.setParent(this);

		m_stepPolygon.setNumVertices(0);
		m_stepPolygon.setGlobalPolygonColor(Color.RED);
		m_stepPolygon.showPolygonLabels(false);
		m_stepPolygon.showVertices(true);
		m_stepPolygon.showVertexLabels(true);
		m_stepPolygon.setGlobalVertexSize(5.);
		m_stepPolygon.setParent(this);
	}

	private void initGrid() {
		m_gridColors = new Color[GRIDSIZE*GRIDSIZE];
		for (int i = 0; i < GRIDSIZE*GRIDSIZE; i++) {
			m_gridColors[i] = m_backgroundColor;
		}

		m_gridSideColors = new Color[4][GRIDSIZE*GRIDSIZE];

		m_grid.setNumVertices((GRIDSIZE+1)*(GRIDSIZE+1));
		m_grid.setDimOfElements(4);
		m_grid.setNumElements(GRIDSIZE*GRIDSIZE);
		m_grid.assureElementColors();
		m_grid.setGlobalEdgeColor(Color.GRAY);
		m_grid.showElementColors(true);

		for (int i = 0; i < GRIDSIZE + 1; i++) {
			for (int j = 0; j < GRIDSIZE + 1; j++) {
				m_grid.setVertex(j+(i*(GRIDSIZE+1)), j-0.5, i-0.5);
			}
		}

		int s;
		for (int i = 0; i < GRIDSIZE; i++) {
			for (int j = 0; j < GRIDSIZE; j++) {
				s = j + (i * GRIDSIZE);
				m_grid.setElement(s, s+i, s+i+1, s+i+GRIDSIZE+2, s+i+GRIDSIZE+1);
			}
		}
		drawGrid();
	}

	public void start() {
		m_pjClippingIP = (PjClipping_IP)getInfoPanel();

		addGeometry(m_grid);
		addGeometry(m_polygons);
		addGeometry(m_stepPolygon);
		selectGeometry(m_polygons);
		drawRectangle(-1);
		m_grid.update(m_grid);
		m_polygons.update(m_polygons);

		PvDisplayIf disp = getDisplay();
		if (disp != null) {
			((Component)disp).addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					int rot = e.getWheelRotation();
					if (rot < 0)
						zoomIn();
					else
						zoomOut();
				}
			});
			disp.selectCamera(PvCameraIf.CAMERA_ORTHO_XY);
			disp.setMajorMode(PvDisplayIf.MODE_DISPLAY_PICK);
			disp.setEnabledIntegerPick(true);
			disp.setBackgroundColor(m_backgroundColor);
			disp.setLightingModel(PvLightIf.MODEL_SURFACE);
			disp.setEnabledZBuffer(true);
		}

		super.start();

		// for some reason this gets ignored sometimes ...
		PvCamera m_camera = (PvCamera)disp.getCamera();
		m_camera.setDist(30.);
	}

	// redraw the grid after something has been moved on it
	private void drawGrid() {
		m_grid.setElementColors(m_gridColors);
	}

	// draw the grid with the side currently being clipped in gray
	private void drawGrid(int side) {
		m_grid.setElementColors(m_gridSideColors[side]);
	}

	// draw the rectangle that is used for clipping
	private void drawRectangle(int pickIndex) {
		PdVector botleft = m_polygons.getVertex(0);
		int botleftX = (int) Math.round(botleft.getEntry(0));
		int botleftY = (int) Math.round(botleft.getEntry(1));
		PdVector botright = m_polygons.getVertex(1);
		int botrightX = (int) Math.round(botright.getEntry(0));
		int botrightY = (int) Math.round(botright.getEntry(1));
		PdVector topright = m_polygons.getVertex(2);
		int toprightX = (int) Math.round(topright.getEntry(0));
		int toprightY = (int) Math.round(topright.getEntry(1));
		PdVector topleft = m_polygons.getVertex(3);
		int topleftX = (int) Math.round(topleft.getEntry(0));
		int topleftY = (int) Math.round(topleft.getEntry(1));

		// move the adjacent points according to the picked one
		// so that the rectangle is preserved. unfortunately javaview
		// seems to be a bit slow with this ...
		switch (pickIndex) {
		case 0:
			topleft.setEntry(0, botleftX);
			botright.setEntry(1, botleftY);
			break;
		case 1:
			topright.setEntry(0, botrightX);
			botleft.setEntry(1, botrightY);
			break;
		case 2:
			botright.setEntry(0, toprightX);
			topleft.setEntry(1, toprightY);
			break;
		case 3:
			botleft.setEntry(0, topleftX);
			topright.setEntry(1, topleftY);
			break;
		}

		int minY = Math.min(Math.min(botleftY, topleftY), Math.min(toprightY, botrightY));
		int maxY = Math.max(Math.max(botleftY, topleftY), Math.max(toprightY, botrightY));
		int minX = Math.min(Math.min(botleftX, topleftX), Math.min(toprightX, botrightX));
		int maxX = Math.max(Math.max(botleftX, topleftX), Math.max(toprightX, botrightX));

		for (int i = minY; i <= maxY; i++) {
			setElementColorFromCoords(minX, i, m_rectangleColor);
		}
		for (int i = minX; i <= maxX; i++) {
			setElementColorFromCoords(i, maxY, m_rectangleColor);
		}
		for (int i = minY; i <= maxY; i++) {
			setElementColorFromCoords(maxX, i, m_rectangleColor);
		}
		for (int i = minX; i <= maxX; i++) {
			setElementColorFromCoords(i, minY, m_rectangleColor);
		}
	}

	// draw the user-created polygon that is to be clipped
	private void drawPolygon() {
		int numV = m_polygons.getNumVertices();

		if (numV > 5) {
			for (int i = 4; i < numV; i++) {
				int end = i+1;
				if (end >= numV)
					end = (end % numV) + 4;
				computePolyBresenham(m_polygons.getVertex(i), true, m_polygons.getVertex(end), true, 0, false, false);
			}
		}
	}

	// compute and draw the lines of the polygon using the bresenham algorithm
	// 'initial' means we are at the initial computing step when the user pressed 'visualize'
	// and we save the polygons after each step to be able to jump to them quickly
	// 'step' means we are in the actual clipping step mode and not the drawing of the
	// base polygon
	private void computePolyBresenham(PdVector start, boolean isInStart, PdVector end, boolean isInEnd,
									  double a, boolean initial, boolean step) {
		int startX = (int) Math.round(start.getEntry(0));
		int startY = (int) Math.round(start.getEntry(1));
		int endX = (int) Math.round(end.getEntry(0));
		int endY = (int) Math.round(end.getEntry(1));

		// is the line steeper than 45 degrees?
		boolean steep = Math.abs(endY - startY) > Math.abs(endX - startX);

		int tmp;
		if (steep) {
			tmp = startX;
			startX = startY;
			startY = tmp;
			tmp = endX;
			endX = endY;
			endY = tmp;
		}

		int xstep = (startX < endX) ? 1 : -1;
		int ystep = (startY < endY) ? 1 : -1;

		int x = startX;
		int y = startY;
		int dx = Math.abs(endX - startX);

		int dx2 = dx + dx;
		int dy = Math.abs(endY - startY);
		int dy2 = dy + dy;
		int f = dy2 - dx;

		// if the line has to be clipped because the points are on different sides of the clipping
		// line, determine from 'a' the range that will be on the inside
		int stepStart, stepEnd;
		if (isInStart && !isInEnd) { // inside -> outside
			stepStart = 0;
			stepEnd = (int)Math.round(a * dx);
		} else if (!isInStart && isInEnd) { // outside -> inside
			stepStart = (int)Math.round(a * dx);
			stepEnd = dx;
		} else {
			stepStart = 0;
			stepEnd = dx;
		}

		// the actual bresenham algorithm
		int numSV = m_stepPolygon.getNumVertices();
		for (int i = 0; i <= dx; i++) {
			if (step) { // we are in step mode
				if (steep) {
					if (!initial)
						setElementColorFromCoords(y, x, m_polygonStepColor);
					if (i == stepStart && ((numSV == 0 && isInStart) || (!isInStart && isInEnd))) {
						m_stepPolygon.addVertex(new PdVector(y, x));
					} else if (i == stepEnd && (isInEnd || isInStart)) {
						m_stepPolygon.addVertex(new PdVector(y, x));
					}
				} else {
					if (!initial)
						setElementColorFromCoords(x, y, m_polygonStepColor);
					if (i == stepStart && ((numSV == 0 && isInStart) || (!isInStart && isInEnd))) {
						m_stepPolygon.addVertex(new PdVector(x, y));
					} else if (i == stepEnd && (isInEnd || isInStart)) {
						m_stepPolygon.addVertex(new PdVector(x, y));
					}
				}
			} else { // we are in normal drawing mode
				if (steep) {
					setElementColorFromCoords(y, x, m_polygonColor);
				} else {
					setElementColorFromCoords(x, y, m_polygonColor);
				}
			}
			x += xstep;
			if (f >= 0) {
				y += ystep;
				f -= dx2;
			}
			f += dy2;
		}

		if (!initial) {
			m_stepPolygon.update(m_stepPolygon);
		}
	}

	// start the clipping, this does most of the 'housekeeping' around the actual
	// clipping algorithm
	protected void startClipping(boolean initial) {
		int numV = m_polygons.getNumVertices();

		if (m_clipStep == 0) {
			// only happens in stepStart() / stepBack()

			m_stepPolygon.setNumPolygons(0);
			m_stepPolygon.setNumVertices(0);

			m_sideFinished = 0;

			drawGrid();
			drawRectangle(-1);
			drawPolygon();
			m_grid.update(m_grid);
			m_polygons.update(m_polygons);

			return;
		} else if (m_clipStep + 4 <= numV
				   && !(numV == 6 && m_clipStep == 2) // special case for polygon with only 2 vertices
				  ) {
			// draw clipped polygon for current step

			if (!initial) {
				drawGrid(m_rectSide);
				drawRectangle(-1);
				m_grid.update(m_grid);
				m_polygons.update(m_polygons);
			}

			m_sideFinished = -1;

			// clear polygon from previous step
			m_stepPolygon.setNumPolygons(0);
			m_stepPolygon.setNumVertices(0);

			if (numV > 6) {
				// only draw in a "circle" if polygon has more than 2 vertices
				for (int i = 4; i < m_clipStep + 4; i++) {
					// compute the clipping for each line of the current step
					if (!initial)
						drawPolygon();
					int end = i+1;
					if (end >= numV)
						end = (end % numV) + 4;
					computeClipping(m_polygons.getVertex(m_rectSide), m_polygons.getVertex((m_rectSide + 1) % 4),
							m_polygons.getVertex(i), m_polygons.getVertex(end), initial);
				}
			} else {
				computeClipping(m_polygons.getVertex(m_rectSide), m_polygons.getVertex((m_rectSide + 1) % 4),
						m_polygons.getVertex(4), m_polygons.getVertex(5), initial);
			}

			int numSV = m_stepPolygon.getNumVertices();

			// remove the last vertex if it is the same as the first one
			if (numSV > 1 && m_stepPolygon.getVertex(0).equals(m_stepPolygon.getVertex(numSV-1)))
				m_stepPolygon.removeVertex(numSV - 1);

			numSV = m_stepPolygon.getNumVertices();

			// draw the temporary step polygons between the new vertices
			m_stepPolygon.setNumPolygons(numSV);
			for (int i = 0; i < numSV; i++) {
				m_stepPolygon.setPolygon(i, i, (i+1) % numSV);
			}

			if (!initial) {
				m_grid.update(m_grid);
				m_polygons.update(m_polygons);
				m_stepPolygon.update(m_stepPolygon);
			}
		} else {
			// actually clip the old polygon

			m_sideFinished = m_rectSide + 1;

			int numSV = m_stepPolygon.getNumVertices();

			// replace the old polygon vertices with the new stepPolygon ones
			m_polygons.setNumVertices(numSV + 4);
			for (int i = 0; i < numSV; i++) {
				m_polygons.setVertex(i+4, m_stepPolygon.getVertex(i));
			}

			// on initial run, save the polygon after each step
			if (initial) {
				m_stepPolygons[m_rectSide].copy(m_polygons);
			}

			// reset the stepPolygon
			m_stepPolygon.setNumPolygons(0);
			m_stepPolygon.setNumVertices(0);

			if (!initial) {
				drawGrid();
				drawRectangle(-1);
				drawPolygon();
				m_grid.update(m_grid);
				m_polygons.update(m_polygons);
				m_stepPolygon.update(m_stepPolygon);
			}

			if (m_rectSide < 3) {
				m_clipEnd = false;
			} else {
				m_clipEnd = true;
			}
			m_rectSide++;
			m_clipStep = 0;
		}
	}

	// the actual clipping algorithm (alpha-clipping)
	private void computeClipping(PdVector pi, PdVector pi1, PdVector q0, PdVector q1, boolean initial) {
		double a0 = 0;
		double a1 = 1;
		double f0, f1;

		f0 = computeFi(pi, pi1, q0);
		f1 = computeFi(pi, pi1, q1);
		if (f0 < f1) {
			if (f0 < 0) {
				if (f1 < 0) {
					// q0 outside, q1 outside
					computePolyBresenham(q0, false, q1, false, 1, initial, true);
					m_q0inside = false;
					m_q1inside = false;
				} else {
					// q0 outside, q1 inside
					a0 = f0/(f0-f1);
					computePolyBresenham(q0, false, q1, true, a0, initial, true);
					m_q0inside = false;
					m_q1inside = true;
				}
			} else {
				// both inside
				computePolyBresenham(q0, true, q1, true, 1, initial, true);
				m_q0inside = true;
				m_q1inside = true;
			}
		} else {
			if (f1 < 0) {
				if (f0 < 0) {
					// q0 outside, q1 outside
					computePolyBresenham(q0, false, q1, false, 1, initial, true);
					m_q0inside = false;
					m_q1inside = false;
				} else {
					// q0 inside, q1 outside
					a1 = f0/(f0-f1);
					computePolyBresenham(q0, true, q1, false, a1, initial, true);
					m_q0inside = true;
					m_q1inside = false;
				}
			} else {
				// both inside
				computePolyBresenham(q0, true, q1, true, 1, initial, true);
				m_q0inside = true;
				m_q1inside = true;
			}
		}
	}

	// compute the Fi distance function
	private double computeFi(PdVector pi, PdVector pi1, PdVector p) {
		int xi = (int)Math.round(pi.getEntry(0));
		int yi = (int)Math.round(pi.getEntry(1));
		int xi1 = (int)Math.round(pi1.getEntry(0));
		int yi1 = (int)Math.round(pi1.getEntry(1));

		PdVector n = new PdVector(yi - yi1, xi1 - xi);

		return PdVector.dot(n, PdVector.subNew(p, pi1));
	}

	private void setElementColorFromCoords(int x, int y, Color c) {
		m_grid.setElementColor(x+(y*GRIDSIZE), c);
	}

	public void stepStart() {
		reset(0);
	}

	public void stepBack() {
		if (m_clipStep > 0) {
			m_clipStep--;
			startClipping(false);
			m_clipEnd = false;
		} else if (m_clipStep == 0 && m_rectSide > 0) {
			reset(m_rectSide - 1);
			int steps = m_polygons.getNumVertices() - 4;
			for (int i = 0; i < steps; i++) {
				stepForward(false);
			}
		}
	}

	public void stepForward(boolean initial) {
		int numV = m_polygons.getNumVertices();

		if (m_clipStep <= numV - 4 && !m_clipEnd) {
			m_clipStep++;
			startClipping(initial);
		}
	}

	public void stepEnd() {
		reset(4);
	}

	public void zoomIn() {
		PvDisplayIf disp = getDisplay();
		PvCamera cam = (PvCamera)disp.getCamera();
		double dist = cam.getDist();
		if (dist > ZOOM_MIN) {
			cam.setDist(dist - 1);
			disp.update(disp);
		}
	}

	public void zoomOut() {
		PvDisplayIf disp = getDisplay();
		PvCamera cam = (PvCamera)disp.getCamera();
		double dist = cam.getDist();
		if (dist < ZOOM_MAX) {
			cam.setDist(dist + 1);
			disp.update(disp);
		}
	}

	public int getSideFinished() {
		return m_sideFinished;
	}

	public boolean getQ0inside() {
		return m_q0inside;
	}

	public boolean getQ1inside() {
		return m_q1inside;
	}

	// switch between the polygon editing and the clipping visualization modes
	public void switchVisualize(boolean vis) {
		PvDisplayIf disp = getDisplay();
		if (vis) {
			m_polygonsOrig.copy(m_polygons);
			saveGraySides();
			disp.setMajorMode(PvDisplayIf.MODE_INITIAL_PICK);

			// do initial clipping
			// this will save the polygon after each step so that we can
			// directly jump to them using the buttons
			while (!m_clipEnd) {
				stepForward(true);
			}
			reset(0);

			m_polygons.showVertices(false);
			m_polygons.update(m_polygons);
		} else {
			String item = (String)m_pjClippingIP.m_mode.getSelectedItem();
			if (item.equals(PjClipping_IP.MMOVE))
				setMode(ClipMode.MOVE);
			else if (item.equals(PjClipping_IP.MADD))
				setMode(ClipMode.ADD);
			else if (item.equals(PjClipping_IP.MDELETE))
				setMode(ClipMode.DELETE);

			reset(0);

			m_polygons.showVertices(true);
			m_polygons.update(m_polygons);
		}
	}

	// reset everything to the beginning, clearing user-created polygon
	protected void reset() {
		m_polygons.setNumVertices(4);
		m_polygons.showPolygons(false);
		m_polygons.showVertices(true);
		m_polygons.setVertex(0, 15, 15);
		m_polygons.setVertex(1, 28, 15);
		m_polygons.setVertex(2, 28, 26);
		m_polygons.setVertex(3, 15, 26);

		m_pjClippingIP.m_visualize.setEnabled(false);

		reset(-1);
	}

	// reset the polygon to before clipping of the specified side
	private void reset(int side) {
		if (side == 0) {
			m_polygons.copy(m_polygonsOrig);
		} else if (side > 0) {
			m_polygons.copy(m_stepPolygons[side - 1]);
		}

		m_sideFinished = side;

		m_clipStep = 0;
		m_rectSide = Math.max(side, 0);

		m_stepPolygon.setNumPolygons(0);
		m_stepPolygon.setNumVertices(0);

		if (side < 4)
			m_clipEnd = false;
		else
			m_clipEnd = true;

		drawGrid();
		drawRectangle(-1);
		drawPolygon();
		m_grid.update(m_grid);
		m_polygons.update(m_polygons);
	}

	// save the colorization of the grid with a gray side for each rectangle side
	private void saveGraySides() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < GRIDSIZE*GRIDSIZE; j++) {
				m_gridSideColors[i][j] = m_backgroundColor;
			}
		}

		// side 0 (bottom)
		int side0Y = (int)Math.round(m_polygons.getVertex(0).getEntry(1));
		for (int i = 0; i < side0Y * GRIDSIZE; i++) {
			m_gridSideColors[0][i] = Color.GRAY;
		}

		// side 1 (right)
		int side1X = (int)Math.round(m_polygons.getVertex(1).getEntry(0));
		for (int i = 0; i < GRIDSIZE*GRIDSIZE; i++) {
			if (i % GRIDSIZE > side1X)
				m_gridSideColors[1][i] = Color.GRAY;
		}

		// side 2 (top)
		int side2Y = (int)Math.round(m_polygons.getVertex(2).getEntry(1));
		for (int i = (side2Y+1) * GRIDSIZE; i < GRIDSIZE*GRIDSIZE; i++) {
			m_gridSideColors[2][i] = Color.GRAY;
		}

		// side 3 (left)
		int side3X = (int)Math.round(m_polygons.getVertex(3).getEntry(0));
		for (int i = 0; i < GRIDSIZE*GRIDSIZE; i++) {
			if (i % GRIDSIZE < side3X)
				m_gridSideColors[3][i] = Color.GRAY;
		}
	}

	public void setMode(ClipMode mode) {
		PvDisplayIf disp = getDisplay();
		switch (mode) {
		case ADD:
			m_currentMode = ClipMode.ADD;
			disp.setMajorMode(PvDisplayIf.MODE_DISPLAY_PICK);
			break;
		case DELETE:
			m_currentMode = ClipMode.DELETE;
			disp.setMajorMode(PvDisplayIf.MODE_DISPLAY_PICK);
			break;
		case MOVE:
		default:
			m_currentMode = ClipMode.MOVE;
			disp.setMajorMode(PvDisplayIf.MODE_PICK);
		}
	}

	// executed when the user clicks on the display in either 'add'
	// or 'delete' mode
	public void pickDisplay(PvPickEvent pos) {
		PdVector coords = pos.getViewBase();
		int x = (int)Math.round(coords.getEntry(0));
		int y = (int)Math.round(coords.getEntry(1));

		drawGrid();

		if (m_currentMode == ClipMode.ADD) {
			setElementColorFromCoords(x, y, m_polygonColor);
			m_polygons.addVertex(coords);
			if (m_polygons.getNumVertices() > 5)
				m_pjClippingIP.m_visualize.setEnabled(true);
		} else if (m_currentMode == ClipMode.DELETE) {
			int numV = m_polygons.getNumVertices();
			for (int i = 4; i < numV; i++) {
				if (m_polygons.getVertex(i).equals(coords)) {
					m_polygons.removeVertex(i);
					break;
				}
			}
			if (m_polygons.getNumVertices() < 6)
				m_pjClippingIP.m_visualize.setEnabled(false);
		}

		drawRectangle(-1);
		drawPolygon();
		m_grid.update(m_grid);
		m_polygons.update(m_polygons);
	}

	// drag the clicked vertex in 'move' mode
	public void dragVertex(PgGeometryIf geom, int index, PdVector vertex) {
		drawGrid();
		drawRectangle(index);
		drawPolygon();
		m_grid.update(m_grid);
		m_polygons.update(m_polygons);
	}

	public boolean update(Object event) {
		if (event == m_polygons || event == m_stepPolygon) {
			return true;
		}
		// If we do not know about the event then just forward it to the superclass.
		return super.update(event);
	}
}
