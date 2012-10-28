/*
 * GraphicsApplets/PjClipping_IP.java - Part of the Clipping Applet
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

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.tu_clausthal.in.clipping.PjClipping.ClipMode;

import jv.object.PsUpdateIf;
import jv.project.PjProject_IP;

public class PjClipping_IP extends PjProject_IP
	implements ActionListener {

	private PjClipping m_pjClipping;
	
	protected static final String MMOVE = "Verschieben";
	protected static final String MADD = "Hinzufügen";
	protected static final String MDELETE = "Löschen";
	protected static final String[] MODES = { MADD, MMOVE, MDELETE };
	protected JComboBox m_mode;
	protected JCheckBox m_visualize;
	protected JButton m_startClip, m_reset;
	protected JButton zoomIn, zoomOut;

	protected JLabel m_infoText;

	private JButton startButton, backButton, forwardButton, endButton;

	public PjClipping_IP() {
		super();

		if (getClass() == PjClipping_IP.class)
			init();
	}

	public void init() {
		super.init();
		Font titleFont = new Font(null, Font.BOLD, 14);
		Font buttonFont = new Font(null, Font.BOLD, 12);
		Font infoFont = new Font("Serif", Font.BOLD, 14);

		JLabel clippingTitle = new JLabel("Clipping");
		clippingTitle.setFont(titleFont);
		add(clippingTitle);

		JPanel modePanel = new JPanel(new GridLayout(0, 2));
		modePanel.setBackground(UIManager.getColor(this));
		add(modePanel);
		m_mode = new JComboBox(MODES);
		m_mode.setLightWeightPopupEnabled(false);
		m_mode.addActionListener(this);
		modePanel.add(m_mode);
		m_reset = new JButton("Zurücksetzen");
		m_reset.addActionListener(this);
		modePanel.add(m_reset);

		m_visualize = new JCheckBox("Visualisieren");
		m_visualize.addActionListener(this);
		m_visualize.setEnabled(false);
		add(m_visualize);

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

		JPanel stepPanel = new JPanel(new GridLayout(0, 4));
		stepPanel.setBackground(UIManager.getColor(this));
		add(stepPanel);
		startButton = new JButton("|<");
		startButton.setFont(buttonFont);
		startButton.addActionListener(this);
		startButton.setEnabled(false);
		stepPanel.add(startButton);
		backButton = new JButton("<");
		backButton.setFont(buttonFont);
		backButton.addActionListener(this);
		backButton.setEnabled(false);
		stepPanel.add(backButton);
		forwardButton = new JButton(">");
		forwardButton.setFont(buttonFont);
		forwardButton.addActionListener(this);
		forwardButton.setEnabled(false);
		stepPanel.add(forwardButton);
		endButton = new JButton(">|");
		endButton.setFont(buttonFont);
		endButton.addActionListener(this);
		endButton.setEnabled(false);
		stepPanel.add(endButton);

		m_infoText = new JLabel("");
		m_infoText.setFont(infoFont);
		m_infoText.setBackground(UIManager.getColor(this));
		m_infoText.setOpaque(true);
		add(m_infoText);
	}

	public void setParent(PsUpdateIf parent) {
		super.setParent(parent);
		m_pjClipping = (PjClipping) parent;
	}

	private void setInfoText() {
		int sideFinished = m_pjClipping.getSideFinished();

		if (sideFinished == 0) {
			m_infoText.setText("");
		} else if (sideFinished == -1) {
			boolean q0inside = m_pjClipping.getQ0inside();
			boolean q1inside = m_pjClipping.getQ1inside();
			String text = "";

			text += "Anfangspunkt: " + (q0inside ? "innen" : "außen");
			text += ", ";
			text += "Endpunkt: " + (q1inside ? "innen" : "außen");

			m_infoText.setText(text);
		} else {
			m_infoText.setText("Seite " + sideFinished + " fertig geclippt");
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == m_mode) {
			String item = (String)m_mode.getSelectedItem();
			if (item.equals(MMOVE))
				m_pjClipping.setMode(ClipMode.MOVE);
			else if (item.equals(MADD))
				m_pjClipping.setMode(ClipMode.ADD);
			else if (item.equals(MDELETE))
				m_pjClipping.setMode(ClipMode.DELETE);
		} else if (source == m_reset) {
			m_pjClipping.reset();
		} else if (source == m_visualize) {
			if (m_visualize.isSelected()) {
				m_mode.setEnabled(false);
				m_reset.setEnabled(false);
				startButton.setEnabled(true);
				backButton.setEnabled(true);
				forwardButton.setEnabled(true);
				endButton.setEnabled(true);
				m_pjClipping.switchVisualize(true);
			} else {
				m_mode.setEnabled(true);
				m_reset.setEnabled(true);
				startButton.setEnabled(false);
				backButton.setEnabled(false);
				forwardButton.setEnabled(false);
				endButton.setEnabled(false);
				m_pjClipping.switchVisualize(false);
			}
		} else if (source == startButton) {
			m_pjClipping.stepStart();
			setInfoText();
		} else if (source == backButton) {
			m_pjClipping.stepBack();
			setInfoText();
		} else if (source == forwardButton) {
			m_pjClipping.stepForward(false);
			setInfoText();
		} else if (source == endButton) {
			m_pjClipping.stepEnd();
			setInfoText();
		} else if (source == zoomIn) {
			m_pjClipping.zoomIn();
		} else if (source == zoomOut) {
			m_pjClipping.zoomOut();
		}
	}

	public boolean update(Object event) {
		if (m_pjClipping == event) {
			return true;
		}
		return super.update(event);
	}
}
