/*
 * GraphicsApplets/PjTransform_IP.java - Part of the Transform Applet
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

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import jv.object.PsUpdateIf;
import jv.project.PjProject_IP;
import jv.vecmath.PdMatrix;
import jvx.numeric.PnMatrix;

public class PjTransform_IP extends PjProject_IP
	implements ActionListener, ItemListener, AdjustmentListener {

	protected PjTransform m_pjTransform;

	protected static final String NCUBE = "Würfel";
	protected static final String NSPHERE = "Kugel";
	protected static final String NTETRA = "Tetraeder";
	protected static final String NCONE = "Kegel";
	protected static final String NCYLINDER = "Zylinder";

	protected static final String[] SOLIDS = {NCUBE, NSPHERE, NTETRA, NCONE, NCYLINDER};

	protected static final String MTRANSF = "Transformation";
	protected static final String MTRANSL = "Translation";
	protected static final String MSKAL = "Skalierung";
	protected static final String MPERS = "Perspektive";
	protected static final String MROTX = "Rotation um X-Achse";
	protected static final String MROTY = "Rotation um Y-Achse";
	protected static final String MROTZ = "Rotation um Z-Achse";

	protected static final String[] MODES = {MTRANSF, MTRANSL, MSKAL, MPERS, MROTX, MROTY, MROTZ};

	protected static final int DIMS = 4;

	protected JComboBox solidCB;
	protected JCheckBox filled;
	protected JComboBox mode;
	protected JButton resetButton, transposeButton, inverseButton;
	protected JLabel det;
	protected JPanel matrix;
	protected JLabel angleL;
	protected JTextField angleTf;
	protected JScrollBar angleSb;
	protected JButton stackPush, stackClear;

	public PjTransform_IP() {
		super();

		if (getClass() == PjTransform_IP.class)
			init();
	}

	public void init() {
		super.init();
		Font titleFont = new Font(null, Font.BOLD, 14);

		JLabel solidTitle = new JLabel("Körper");
		solidTitle.setFont(titleFont);
		add(solidTitle);

		solidCB = new JComboBox(SOLIDS);
		solidCB.setLightWeightPopupEnabled(false);
		solidCB.addActionListener(this);
		add(solidCB);

		filled = new JCheckBox("Ausgefüllt", false);
		filled.addItemListener(this);
		add(filled);

		addLine(1);

		JLabel matrixTitle = new JLabel("Matrix");
		matrixTitle.setFont(titleFont);
		add(matrixTitle);
		JPanel matrixType = new JPanel(new GridLayout(2,0));
		add(matrixType);

		resetButton = new JButton("Einheit");
		resetButton.addActionListener(this);
		matrixType.add(resetButton);
		transposeButton = new JButton("Transponierte");
		transposeButton.addActionListener(this);
		matrixType.add(transposeButton);
		inverseButton = new JButton("Inverse");
		inverseButton.addActionListener(this);
		matrixType.add(inverseButton);
		det = new JLabel("Determinante: 1.0");
		det.setHorizontalAlignment(SwingConstants.CENTER);
		matrixType.add(det);

		addLine(1);

		Box panel = new Box(BoxLayout.Y_AXIS);
		add(panel);

		mode = new JComboBox(MODES);
		mode.setLightWeightPopupEnabled(false);
		mode.addActionListener(this);
		panel.add(mode);

		matrix = new JPanel();
		matrix.setLayout(new GridLayout(DIMS, DIMS));
		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				JTextField entry = new JTextField();
				if (i == j)
					entry.setText("1.0");
				else
					entry.setText("0.0");
				entry.setActionCommand("matrix");
				entry.addActionListener(this);
				entry.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
						JTextField field = (JTextField)e.getComponent();
						field.selectAll();
					}
					public void focusLost(FocusEvent e) {
					}
				});
				matrix.add(entry);
			}
		}
		panel.add(matrix);

		JPanel angle = new JPanel(new GridLayout(0, 2));
		JPanel angleTextPanel = new JPanel(new GridLayout(0, 2));
		angleL = new JLabel("Winkel: ");
		angleL.setHorizontalAlignment(SwingConstants.CENTER);
		angleL.setEnabled(false);
		angleTextPanel.add(angleL);
		angleTf = new JTextField("0.0");
		angleTf.setColumns(4);
		angleTf.addActionListener(this);
		angleTf.setEnabled(false);
		angleTf.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				JTextField field = (JTextField)e.getComponent();
				field.selectAll();
			}
			public void focusLost(FocusEvent e) {
			}
		});
		angleTextPanel.add(angleTf);
		angle.add(angleTextPanel);
		angleSb = new JScrollBar(Adjustable.HORIZONTAL, 0, 10, 0, 359);
		angleSb.addAdjustmentListener(this);
		angleSb.setEnabled(false);
		angle.add(angleSb);
		panel.add(angle);

		addLine(1);
		JLabel stackTitle = new JLabel("Stack");
		stackTitle.setFont(titleFont);
		add(stackTitle);
		JPanel stack = new JPanel(new GridLayout(0, 2));
		add(stack);
		stackPush = new JButton("Push");
		stackPush.addActionListener(this);
		stack.add(stackPush);
		stackClear = new JButton("Leeren");
		stackClear.addActionListener(this);
		stack.add(stackClear);
	}

	public void setParent(PsUpdateIf parent) {
		super.setParent(parent);
		m_pjTransform = (PjTransform) parent;
	}

	public boolean update(Object event) {
		if (m_pjTransform == event) {
			return true;
		}
		return super.update(event);
	}

	private void reset(boolean resetStack) {
		if (resetStack)
			m_pjTransform.stackSetUnit();
		PdMatrix m = new PdMatrix(DIMS);
		m.setIdentity();
		setMatrix(m);
		angleTf.setText("0.0");
		angleSb.setValue(0);
		m_pjTransform.transformSolid(m);
	}

	// get the entries from the matrix in the panel and save them in a PdMatrix
	private PdMatrix getMatrix() {
		PdMatrix m = new PdMatrix(DIMS);
		boolean exHappened = false;
		String modS;

		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
				try {
					float d = Float.parseFloat(entry.getText());
					modS = (String)mode.getSelectedItem();
					if (!modS.equals(MROTX) && !modS.equals(MROTY) && !modS.equals(MROTZ))
						entry.setBackground((Color)UIManager.get("TextField.background"));
					m.setEntry(i, j, d);
				} catch (NumberFormatException ex) {
					entry.setBackground(Color.RED);
					exHappened = true;
				}
			}
		}

		if (!exHappened)
			return m;
		else
			return null;
	}

	// set the matrix shown in the panel to the given PdMatrix
	private void setMatrix(PdMatrix m) {
		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
				entry.setText(toText(m.getEntry(i, j)));
			}
		}
		det.setText("Determinante: " + toText(m.det()));
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == solidCB) {
			String item = (String)solidCB.getSelectedItem();
			if (item.equals(NCUBE)) {
				m_pjTransform.showSolid(m_pjTransform.m_cube);
			} else if (item.equals(NSPHERE)) {
				m_pjTransform.showSolid(m_pjTransform.m_sphere);
			} else if (item.equals(NTETRA)) {
				m_pjTransform.showSolid(m_pjTransform.m_tetra);
			} else if (item.equals(NCONE)) {
				m_pjTransform.showSolid(m_pjTransform.m_cone);
			} else if (item.equals(NCYLINDER)) {
				m_pjTransform.showSolid(m_pjTransform.m_cylinder);
			}
		} else if (source == resetButton) {
			reset(true);
		} else if (source == transposeButton) {
			PdMatrix m = getMatrix();
			if (m != null) {
				m.transpose();
				m_pjTransform.transformSolid(m);
				setMatrix(m);
			}
		} else if (source == inverseButton) {
			PdMatrix m = getMatrix();
			if (m != null) {
				double[][] inv = new double[DIMS][DIMS];
				double[][] md = m.getEntries();
				PnMatrix.invert(inv, md, DIMS);
				m.set(inv);
				m_pjTransform.transformSolid(m);
				setMatrix(m);
			}
		} else if (source == mode) {
			changeMode();
		} else if (e.getActionCommand().equals("matrix")) {
			PdMatrix m = getMatrix();
			if (m != null) {
				m_pjTransform.transformSolid(m);
				det.setText("Determinante: " + toText(m.det()));
			}
		} else if (source == angleTf) {
			try {
				float a = Float.parseFloat(angleTf.getText());
				angleSb.setValue(Math.round(a));
				angleTf.setBackground((Color)UIManager.get("TextField.background"));
				String item = (String)mode.getSelectedItem();
				if (item.equals(MROTX))
					rotateX(a);
				else if (item.equals(MROTY))
					rotateY(a);
				else if (item.equals(MROTZ))
					rotateZ(a);
			} catch (NumberFormatException ex) {
				angleTf.setBackground(Color.RED);
			}
		} else if (source == stackPush) {
			m_pjTransform.stackPush();
			PdMatrix m = new PdMatrix(DIMS);
			m.setIdentity();
			setMatrix(m);
		} else if (source == stackClear) {
			m_pjTransform.stackSetUnit();
			PdMatrix m = getMatrix();
			if (m != null) {
				m_pjTransform.transformSolid(m);
				det.setText("Determinante: " + toText(m.det()));
			}
		}
	}

	private void changeMode() {
		reset(false);
		String item = (String)mode.getSelectedItem();
		if (item.equals(MTRANSF)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					entry.setEnabled(true);
					entry.setFocusable(true);
				}
			}
			angleL.setEnabled(false);
			angleTf.setEnabled(false);
			angleSb.setEnabled(false);
		} else if (item.equals(MTRANSL)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (j == 3 && (i == 0 || i == 1 || i == 2)) {
						entry.setEnabled(true);
						entry.setFocusable(true);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(false);
			angleTf.setEnabled(false);
			angleSb.setEnabled(false);
		} else if (item.equals(MSKAL)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (i == j) {
						entry.setEnabled(true);
						entry.setFocusable(true);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(false);
			angleTf.setEnabled(false);
			angleSb.setEnabled(false);
		} else if (item.equals(MPERS)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (i == 3 && (j == 0 || j == 1 || j == 2)) {
						entry.setEnabled(true);
						entry.setFocusable(true);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(false);
			angleTf.setEnabled(false);
			angleSb.setEnabled(false);
		} else if (item.equals(MROTX)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (i == 1 && j == 1 || i == 1 && j == 2
						|| i == 2 && j == 1 || i == 2 && j == 2) {
						entry.setEnabled(true);
						entry.setFocusable(false);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(true);
			angleTf.setEnabled(true);
			angleSb.setEnabled(true);
		} else if (item.equals(MROTY)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (i == 0 && j == 0 || i == 2 && j == 0
						|| i == 0 && j == 2 || i == 2 & j == 2) {
						entry.setEnabled(true);
						entry.setFocusable(false);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(true);
			angleTf.setEnabled(true);
			angleSb.setEnabled(true);
		} else if (item.equals(MROTZ)) {
			for (int i = 0; i < DIMS; i++) {
				for (int j = 0; j < DIMS; j++) {
					JTextField entry = (JTextField)matrix.getComponent((i * DIMS) + j);
					if (i == 0 && j == 0 || i == 0 && j == 1
						|| i == 1 && j == 0 || i == 1 && j == 1) {
						entry.setEnabled(true);
						entry.setFocusable(false);
					} else {
						entry.setEnabled(false);
						entry.setFocusable(false);
					}
				}
			}
			angleL.setEnabled(true);
			angleTf.setEnabled(true);
			angleSb.setEnabled(true);
		}
	}

	private void rotateX(double a) {
		double[][] d = new double[DIMS][DIMS];
		double rad = Math.toRadians(a);

		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				if (i == 1 && j == 1)
					d[i][j] = Math.cos(rad);
				else if (i == 2 && j == 1)
					d[i][j] = -Math.sin(rad);
				else if (i == 1 && j == 2)
					d[i][j] = Math.sin(rad);
				else if (i == 2 && j == 2)
					d[i][j] = Math.cos(rad);
				else if (i == j)
					d[i][j] = 1;
				else
					d[i][j] = 0;
			}
		}

		PdMatrix m = new PdMatrix(d);
		m_pjTransform.transformSolid(m);
		setMatrix(m);
	}

	private void rotateY(double a) {
		double[][] d = new double[DIMS][DIMS];
		double rad = Math.toRadians(a);

		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				if (i == 0 && j == 0)
					d[i][j] = Math.cos(rad);
				else if (i == 0 && j == 2)
					d[i][j] = Math.sin(rad);
				else if (i == 2 && j == 0)
					d[i][j] = -Math.sin(rad);
				else if (i == 2 && j == 2)
					d[i][j] = Math.cos(rad);
				else if (i == j)
					d[i][j] = 1;
				else
					d[i][j] = 0;
			}
		}

		PdMatrix m = new PdMatrix(d);
		m_pjTransform.transformSolid(m);
		setMatrix(m);
	}

	private void rotateZ(double a) {
		double[][] d = new double[DIMS][DIMS];
		double rad = Math.toRadians(a);

		for (int i = 0; i < DIMS; i++) {
			for (int j = 0; j < DIMS; j++) {
				if (i == 0 && j == 0)
					d[i][j] = Math.cos(rad);
				else if (i == 0 && j == 1)
					d[i][j] = -Math.sin(rad);
				else if (i == 1 && j == 0)
					d[i][j] = Math.sin(rad);
				else if (i == 1 && j == 1)
					d[i][j] = Math.cos(rad);
				else if (i == j)
					d[i][j] = 1;
				else
					d[i][j] = 0;
			}
		}

		PdMatrix m = new PdMatrix(d);
		m_pjTransform.transformSolid(m);
		setMatrix(m);
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		angleTf.setText(toText(angleSb.getValue()));
		angleTf.postActionEvent();
	}

	// round a double to a String of reasonable length
	private String toText(double d) {
		double rd = Math.rint(d * 10000)/10000;
		return String.valueOf(rd);
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == filled) {
			JCheckBox box = (JCheckBox)source;
			m_pjTransform.showElements(box.isSelected());
		}
	}
}
