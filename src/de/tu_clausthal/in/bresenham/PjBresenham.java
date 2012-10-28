/*
 * GraphicsApplets/PjBresenham.java - Part of the Bresenham Applet
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

package de.tu_clausthal.in.bresenham;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygon;
import jv.project.PjProject;
import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.project.PvGeometryIf;
import jv.project.PvLightIf;
import jv.vecmath.PdVector;
import jv.viewer.PvCamera;

public class PjBresenham extends PjProject {

	// width/height of the grid. don't set it too large since
	// it has to be redrawn quite often.
	private static final int GRIDSIZE = 50;
	// ensure that the grid always fills the display. if you enlarge the grid,
	// also increase ZOOM_MAX accordingly.
	private int ZOOM_MIN = 5;
	private int ZOOM_MAX = 45;

	private Color[] m_gridColors;
	private Color m_backgroundColor;
	private Color m_selectedGridColor, m_stepColor;

	protected PjBresenham_IP m_pjBresenhamIP;

	// whether we're in antialias mode
	private boolean m_antialias;

	protected PgElementSet	m_grid;
	protected PgPolygon		m_line;
	protected PgPointSet	m_middlePoint, m_pixelO, m_pixelU;

	protected static final int MPOINT_ABOVE = 1;
	protected static final int MPOINT_BELOW = -1;
	protected static final int MPOINT_LEFT = 2;
	protected static final int MPOINT_RIGHT = -2;
	protected static final int MPOINT_EQUAL = 0;
	protected static final int MPOINT_FINISHED = 3;
	private int m_middlePointPosition;

	// the name of the point that was chosen for the next step
	// so we can display it in the panel
	private String m_chosenPoint;

	private int m_bresenhamStep;
	private int m_bresenhamdx;

	public PjBresenham() {
		super("Bresenham");

		m_grid = new PgElementSet(2);
		m_grid.setName("Grid");

		m_line = new PgPolygon(2);
		m_line.setName("Line");

		m_middlePoint = new PgPointSet(2);
		m_middlePoint.setName("M");

		m_pixelO = new PgPointSet(2);
		m_pixelO.setName("Po");

		m_pixelU = new PgPointSet(2);
		m_pixelU.setName("Pu");

		if (getClass() == PjBresenham.class)
			init();
	}

	public void init() {
		super.init();

		m_antialias = false;
		m_backgroundColor = new Color(238, 238, 238);

		initGrid();

		m_line.setNumVertices(2);
		m_line.setGlobalEdgeColor(Color.RED);
		m_line.setGlobalEdgeSize(1.);
		m_line.showPolygonEndArrow(false);
		m_line.showVertexSizes(true);
		m_line.setVertex(0, 20, 20);
		m_line.setVertexSize(0, 2);
		m_line.setVertex(1, 28, 26);
		m_line.setVertexSize(1, 2);
		m_line.setParent(this);

		m_middlePoint.setNumVertices(1);
		m_middlePoint.setLabelColor(PvGeometryIf.GEOM_ITEM_NAME, Color.BLUE);
		m_middlePoint.setGlobalVertexColor(Color.BLUE);
		m_middlePoint.setGlobalVertexSize(3.0);
		m_middlePoint.showVertices(true);

		m_pixelO.setNumVertices(1);
		m_pixelO.setLabelColor(PvGeometryIf.GEOM_ITEM_NAME, Color.RED);
		m_pixelO.setGlobalVertexColor(Color.RED);
		m_pixelO.setGlobalVertexSize(3.0);
		m_pixelO.showVertices(true);

		m_pixelU.setNumVertices(1);
		m_pixelU.setLabelColor(PvGeometryIf.GEOM_ITEM_NAME, Color.RED);
		m_pixelU.setGlobalVertexColor(Color.RED);
		m_pixelU.setGlobalVertexSize(3.0);
		m_pixelU.showVertices(true);

		m_bresenhamStep = 0;
		m_bresenhamdx = 0;
		m_chosenPoint = m_pixelO.getName();
	}

	private void initGrid() {
		m_selectedGridColor = Color.BLACK;
		m_stepColor = new Color(150, 150, 150);

		m_gridColors = new Color[GRIDSIZE*GRIDSIZE];
		for (int i = 0; i < GRIDSIZE*GRIDSIZE; i++) {
			m_gridColors[i] = m_backgroundColor;
		}

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
		m_grid.setElementColors(m_gridColors);
	}

	public void start() {
		m_pjBresenhamIP = (PjBresenham_IP)getInfoPanel();

		addGeometry(m_grid);
		addGeometry(m_line);
		addGeometry(m_middlePoint);
		addGeometry(m_pixelO);
		addGeometry(m_pixelU);
		selectGeometry(m_line);
		computeBresenham(false);
		computeBresenham(true);
		m_pjBresenhamIP.update(this);

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
			disp.setEnabledIntegerPick(true);
			disp.selectCamera(PvCameraIf.CAMERA_ORTHO_XY);
			disp.setMajorMode(PvDisplayIf.MODE_PICK);
			disp.setBackgroundColor(m_backgroundColor);
			disp.setLightingModel(PvLightIf.MODEL_SURFACE);
			disp.setEnabledZBuffer(true);
		}

		super.start();

		// for some reason this gets ignored sometimes ...
		PvCamera m_camera = (PvCamera)disp.getCamera();
		m_camera.setDist(13.);
	}

	// compute the rasterization of the line using the bresenham algorithm.
	// 'step' indicates whether we're drawing the whole line or just the step
	// we're currently at.
	private void computeBresenham(boolean step) {
		if (!step)
			m_grid.setElementColors(m_gridColors);

		m_middlePoint.showVertices(true);
		m_middlePoint.showName(true);
		m_pixelO.showVertices(true);
		m_pixelO.showName(true);
		m_pixelU.showVertices(true);
		m_pixelU.showName(true);

		PdVector start = m_line.getVertex(0);
		int startX = (int) Math.round(start.getEntry(0));
		int startY = (int) Math.round(start.getEntry(1));
		PdVector end = m_line.getVertex(1);
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

		// does the line run from right to left?
		if (startX > endX) {
			tmp = startX;
			startX = endX;
			endX = tmp;
			tmp = startY;
			startY = endY;
			endY = tmp;
		}

		int ystep = (startY < endY) ? 1 : -1;

		int x = startX;
		int y = startY;
		int dx = endX - startX;
		m_bresenhamdx = dx;

		int stepEnd = step ? m_bresenhamStep : dx;

		// the actual algorithm, with slightly different versions for antialias
		// and non-antialias mode
		if (m_antialias) {
			float I = 1.0f;
			float m = ((float)(endY - startY)) / ((float)(endX - startX));

			for (int i = 0; i <= stepEnd; i++) {
				if (!step) {
					if (steep) {
						setElementColorFromCoords(y, x, I);
						setElementColorFromCoords(y+ystep, x, 1.0f - I);
					} else {
						setElementColorFromCoords(x, y, I);
						setElementColorFromCoords(x, y+ystep, 1.0f - I);
					}
				}
				x++;
				if (Math.abs(m) >= I) {
					y += ystep;
					I += 1.0;
				}
				I -= Math.abs(m);
				if (step) {
					if (i < dx && i == stepEnd) {
						updateStepPointsAA(steep, ystep, x, y, I);
					} else if (i >= dx) {
						hideStepPoints();
					}
				}
			}
		} else {
			int dx2 = dx + dx;
			int dy = Math.abs(endY - startY);
			int dy2 = dy + dy;
			int f = dy2 - dx;

			for (int i = 0; i <= stepEnd; i++) {
				if (step) {
					if (steep && i == stepEnd) {
						setElementColorFromCoords(y, x, m_stepColor);
					} else if (i == stepEnd) {
						setElementColorFromCoords(x, y, m_stepColor);
					}
				} else {
					if (steep) {
						setElementColorFromCoords(y, x, m_selectedGridColor);
					} else {
						setElementColorFromCoords(x, y, m_selectedGridColor);
					}
				}
				x++;
				if (step) {
					if (i < dx && i == stepEnd) {
						updateStepPoints(steep, ystep, x, y, f);
					} else if (i >= dx) {
						hideStepPoints();
					}
				}
				if (f >= 0) {
					y += ystep;
					f -= dx2;
				}
				if (step) {
					if (steep)
						m_chosenPoint = getPointNameAtCoords(y, x);
					else
						m_chosenPoint = getPointNameAtCoords(x, y);
				}
				f += dy2;
			}
		}

		m_middlePoint.update(m_middlePoint);
		m_pixelO.update(m_pixelO);
		m_pixelU.update(m_pixelU);
		m_grid.update(m_grid);
	}

	// update the points M, Po and Pu to illustrate the next step
	// (non-antialias version)
	private void updateStepPoints(boolean steep, int ystep, int x, int y, int f) {
		if (steep) {
			m_middlePoint.setVertex(0, y+(0.5*ystep), x);
			if (ystep > 0) {
				m_pixelO.setVertex(0, y+ystep, x);
				m_pixelU.setVertex(0, y, x);
			} else {
				m_pixelU.setVertex(0, y+ystep, x);
				m_pixelO.setVertex(0, y, x);
			}
			if (f > 0)
				m_middlePointPosition = MPOINT_LEFT*ystep;
			else if (f == 0)
				m_middlePointPosition = MPOINT_EQUAL;
			else
				m_middlePointPosition = MPOINT_RIGHT*ystep;
		} else {
			m_middlePoint.setVertex(0, x, y+(0.5*ystep));
			if (ystep > 0) {
				m_pixelO.setVertex(0, x, y+ystep);
				m_pixelU.setVertex(0, x, y);
			} else {
				m_pixelU.setVertex(0, x, y+ystep);
				m_pixelO.setVertex(0, x, y);
			}
			if (f > 0)
				m_middlePointPosition = MPOINT_BELOW*ystep;
			else if (f == 0)
				m_middlePointPosition = MPOINT_EQUAL;
			else
				m_middlePointPosition = MPOINT_ABOVE*ystep;
		}
	}

	// update the points M, Po and Pu to illustrate the next step
	// (antialias version)
	private void updateStepPointsAA(boolean steep, int ystep, int x, int y, float I) {
		m_middlePointPosition = MPOINT_EQUAL; // to clear the "finished" setting
		if (steep) {
			m_middlePoint.setVertex(0, y+(0.5*ystep), x);
			if (ystep > 0) {
				m_pixelO.setVertex(0, y+ystep, x);
				m_pixelO.setTransparency(I); // hack to send 'I' to the panel
				m_pixelU.setVertex(0, y, x);
				m_pixelU.setTransparency(1.0 - I);
			} else {
				m_pixelU.setVertex(0, y+ystep, x);
				m_pixelU.setTransparency(-I); // negative to indicate switched pixels
				m_pixelO.setVertex(0, y, x);
				m_pixelO.setTransparency(-(1.0 - I));
			}
		} else {
			m_middlePoint.setVertex(0, x, y+(0.5*ystep));
			if (ystep > 0) {
				m_pixelO.setVertex(0, x, y+ystep);
				m_pixelO.setTransparency(I);
				m_pixelU.setVertex(0, x, y);
				m_pixelU.setTransparency(1.0 - I);
			} else {
				m_pixelU.setVertex(0, x, y+ystep);
				m_pixelU.setTransparency(-I);
				m_pixelO.setVertex(0, x, y);
				m_pixelO.setTransparency(-(1.0 - I));
			}
		}
	}

	// hide the step points when we have reached the end of the line
	private void hideStepPoints() {
		m_middlePoint.showVertices(false);
		m_middlePoint.showName(false);
		m_pixelO.showVertices(false);
		m_pixelO.showName(false);
		m_pixelU.showVertices(false);
		m_pixelU.showName(false);
		m_middlePointPosition = MPOINT_FINISHED;
	}

	private void setElementColorFromCoords(int x, int y, Color c) {
		m_grid.setElementColor(x+(y*GRIDSIZE), c);
	}

	private void setElementColorFromCoords(int x, int y, float I) {
		int grey = 255 - Math.round(I * 255);
		m_grid.setElementColor(x+(y*GRIDSIZE), new Color(grey, grey, grey));
	}

	protected void setAntialias(boolean enabled) {
		m_antialias = enabled;
		computeBresenham(false);
		computeBresenham(true);
		m_pjBresenhamIP.update(this);
	}

	private String getPointNameAtCoords(int x, int y) {
		int ox = (int)Math.round(m_pixelO.getVertex(0).getEntry(0));
		int oy = (int)Math.round(m_pixelO.getVertex(0).getEntry(1));
		int ux = (int)Math.round(m_pixelU.getVertex(0).getEntry(0));
		int uy = (int)Math.round(m_pixelU.getVertex(0).getEntry(1));

		if (x == ox && y == oy)
			return m_pixelO.getName();
		else if (x == ux && y == uy)
			return m_pixelU.getName();
		else
			return "Kein Pixel";
	}

	public void bresenStepStart() {
		m_bresenhamStep = 0;
		computeBresenham(false);
		computeBresenham(true);
	}

	public void bresenStepBack() {
		m_bresenhamStep--;
		if (m_bresenhamStep < 0)
			m_bresenhamStep = m_bresenhamdx + 1 + m_bresenhamStep;
		computeBresenham(false);
		computeBresenham(true);
	}

	public void bresenStepForward() {
		m_bresenhamStep = (m_bresenhamStep + 1) % (m_bresenhamdx + 1);
		computeBresenham(false);
		computeBresenham(true);
	}

	public void bresenStepEnd() {
		m_bresenhamStep = m_bresenhamdx;
		computeBresenham(false);
		computeBresenham(true);
	}

	public int getBresenhamStep() {
		return m_bresenhamStep;
	}

	public int setBresenhamStep(int step) {
		m_bresenhamStep = step % (m_bresenhamdx + 1);
		computeBresenham(false);
		computeBresenham(true);
		return m_bresenhamStep;
	}

	public int getMiddlePointPosition() {
		return m_middlePointPosition;
	}

	public String getChosenPoint() {
		return m_chosenPoint;
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

	public boolean update(Object event) {
		if (event == m_line) {
			computeBresenham(false);
			bresenStepStart();
			computeBresenham(true);
			m_pjBresenhamIP.update(this);
			return true;
		}
		// If we do not know about the event then just forward it to the superclass.
		return super.update(event);
	}
}
