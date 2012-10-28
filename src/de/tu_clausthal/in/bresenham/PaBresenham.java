/*
 * GraphicsApplets/PaBresenham.java - Part of the Bresenham Applet
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JApplet;
import javax.swing.UIManager;

import jv.object.PsViewerIf;
import jv.viewer.PvViewer;

public class PaBresenham extends JApplet {
	public Frame m_frame = null;
	protected PvViewer m_viewer;
	
	public String getAppletInfo() {
		return "Bresenham Applet";
	}

	public void init() {
		// try to set native look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// if it doesn't work, just ignore it and use the normal one
		}
		m_viewer = new PvViewer(this, m_frame);
		
		PjBresenham bresenham = new PjBresenham();
		m_viewer.addProject(bresenham);
		m_viewer.selectProject(bresenham);

		setLayout(new BorderLayout());
		add((Component)m_viewer.getDisplay(), BorderLayout.CENTER);
		add(m_viewer.getPanel(PsViewerIf.PROJECT), BorderLayout.EAST);
		validate();
	}

	public static void main(String args[]) {
		PaBresenham va	= new PaBresenham();
		Frame frame	= new jv.object.PsMainFrame(va, args);
		frame.setBounds(new Rectangle(420, 5, 850, 550));
		frame.setVisible(true);
		va.m_frame = frame;
		va.init();
		va.start();
	}

	public void destroy()	{ m_viewer.destroy(); }

	public void start()		{ m_viewer.start();	}

	public void stop()		{ m_viewer.stop(); }
}
