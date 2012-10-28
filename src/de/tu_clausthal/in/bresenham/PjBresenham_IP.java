/*
 * GraphicsApplets/PjBresenham_IP.java - Part of the Bresenham Applet
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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import jv.object.PsUpdateIf;
import jv.project.PjProject_IP;

public class PjBresenham_IP extends PjProject_IP
	implements ActionListener, ItemListener {

	protected PjBresenham m_pjBresenham;

	protected JCheckBox antialias;
	protected JButton startButton, backButton, forwardButton, endButton;
	protected JTextField stepField;
	protected JButton zoomIn, zoomOut;

	protected JLabel m_infoText;

	public PjBresenham_IP() {
		super();

		if (getClass() == PjBresenham_IP.class)
			init();
	}

	public void init() {
		super.init();
		Font titleFont = new Font(null, Font.BOLD, 14);
		Font buttonFont = new Font(null, Font.BOLD, 12);
		Font infoFont = new Font("Serif", Font.BOLD, 14);

		JLabel solidTitle = new JLabel("Bresenham");
		solidTitle.setFont(titleFont);
		add(solidTitle);

		antialias = new JCheckBox("Antialias", false);
		antialias.addItemListener(this);
		add(antialias);

		JLabel zoomTitle = new JLabel("Zoom");
		zoomTitle.setFont(titleFont);
		add(zoomTitle);
		JPanel zoomPanel = new JPanel(new GridLayout(0, 4));
		zoomPanel.setBackground(UIManager.getColor(this));
		add(zoomPanel);
		zoomIn = new JButton("+");
		zoomIn.setFont(buttonFont);
		zoomIn.addActionListener(this);
		zoomPanel.add(zoomIn);
		zoomOut = new JButton("-");
		zoomOut.setFont(buttonFont);
		zoomOut.addActionListener(this);
		zoomPanel.add(zoomOut);

		JLabel stepTitle = new JLabel("Schritt");
		stepTitle.setFont(titleFont);
		add(stepTitle);

		JPanel stepPanel = new JPanel(new GridLayout(0, 5));
		stepPanel.setBackground(UIManager.getColor(this));
		add(stepPanel);
		startButton = new JButton("|<");
		startButton.setFont(buttonFont);
		startButton.addActionListener(this);
		stepPanel.add(startButton);
		backButton = new JButton("<");
		backButton.setFont(buttonFont);
		backButton.addActionListener(this);
		stepPanel.add(backButton);
		stepField = new JTextField("0");
		stepField.addActionListener(this);
		stepPanel.add(stepField);
		forwardButton = new JButton(">");
		forwardButton.setFont(buttonFont);
		forwardButton.addActionListener(this);
		stepPanel.add(forwardButton);
		endButton = new JButton(">|");
		endButton.setFont(buttonFont);
		endButton.addActionListener(this);
		stepPanel.add(endButton);

		m_infoText = new JLabel("");
		m_infoText.setFont(infoFont);
		m_infoText.setBackground(UIManager.getColor(this));
		m_infoText.setOpaque(true);
		add(m_infoText);
	}

	public void setParent(PsUpdateIf parent) {
		super.setParent(parent);
		m_pjBresenham = (PjBresenham) parent;
	}

	public boolean update(Object event) {
		if (m_pjBresenham == event) {
			stepField.setText("" + m_pjBresenham.getBresenhamStep());
			setMiddlePointText();
			return true;
		}
		return super.update(event);
	}

	private void setMiddlePointText() {
		int position = m_pjBresenham.getMiddlePointPosition();
		if (antialias.isSelected()) {
			if (position == PjBresenham.MPOINT_FINISHED) {
				m_infoText.setText("Fertig.");
			} else {
				double pOI = m_pjBresenham.m_pixelO.getTransparency();
				double pUI = m_pjBresenham.m_pixelU.getTransparency();
				pOI = Math.rint(pOI * 100)/100;
				pUI = Math.rint(pUI * 100)/100;
				if (pOI < 0 || pUI < 0) {
					m_infoText.setText("I (Pu): "+Math.abs(pUI)+", 1-I (Po): "+Math.abs(pOI));
				} else {
					m_infoText.setText("I (Po): " + pOI + ", 1-I (Pu): " + pUI);
				}
			}
		} else {
			String pointName = m_pjBresenham.getChosenPoint();
			if (position == PjBresenham.MPOINT_ABOVE) {
				m_infoText.setText("M über Linie => wähle " + pointName);
			} else if (position == PjBresenham.MPOINT_BELOW) {
				m_infoText.setText("M unter Linie => wähle " + pointName);
			} else if (position == PjBresenham.MPOINT_LEFT) {
				m_infoText.setText("M links von Linie => wähle " + pointName);
			} else if (position == PjBresenham.MPOINT_RIGHT) {
				m_infoText.setText("M rechts von Linie => wähle " + pointName);
			} else if (position == PjBresenham.MPOINT_EQUAL) {
				m_infoText.setText("M auf Linie => wähle " + pointName);
			} else if (position == PjBresenham.MPOINT_FINISHED) {
				m_infoText.setText("Fertig.");
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == startButton) {
			m_pjBresenham.bresenStepStart();
			stepField.setText("" + m_pjBresenham.getBresenhamStep());
			stepField.setBackground((Color)UIManager.get("TextField.background"));
			setMiddlePointText();
		} else if (source == backButton) {
			m_pjBresenham.bresenStepBack();
			stepField.setText("" + m_pjBresenham.getBresenhamStep());
			stepField.setBackground((Color)UIManager.get("TextField.background"));
			setMiddlePointText();
		} else if (source == forwardButton) {
			m_pjBresenham.bresenStepForward();
			stepField.setText("" + m_pjBresenham.getBresenhamStep());
			stepField.setBackground((Color)UIManager.get("TextField.background"));
			setMiddlePointText();
		} else if (source == endButton) {
			m_pjBresenham.bresenStepEnd();
			stepField.setText("" + m_pjBresenham.getBresenhamStep());
			stepField.setBackground((Color)UIManager.get("TextField.background"));
			setMiddlePointText();
		} else if (source == stepField) {
			try {
				int step = Integer.parseInt(stepField.getText());
				stepField.setBackground((Color)UIManager.get("TextField.background"));
				int stepModulo = m_pjBresenham.setBresenhamStep(step);
				stepField.setText("" + stepModulo);
				setMiddlePointText();
			} catch (NumberFormatException ex) {
				stepField.setBackground(Color.RED);
			}
		} else if (source == zoomIn) {
			m_pjBresenham.zoomIn();
		} else if (source == zoomOut) {
			m_pjBresenham.zoomOut();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == antialias) {
			JCheckBox box = (JCheckBox)source;
			m_pjBresenham.setAntialias(box.isSelected());
		}
	}
}
