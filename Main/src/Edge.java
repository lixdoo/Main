/* My attempt to visualize a git-repository. This program uses JGit to interact with the repository.
 * Copyright (C) 2014  grasserik
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import javax.swing.JComponent;

public class Edge extends JComponent {
	private static final long 	serialVersionUID 	= -8963483498361343090L;
	static ArrayList<Edge> 		edgeList 			= new ArrayList<Edge>();
	private Node 				from;
	private Node 				to;
	
	public Edge(Node from, Node to) {
		this.from 	= from;
		this.to 	= to;
	}
	
	@Override
	public boolean isOpaque() {
		return false;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int xFromCenter = from.getX() + from.getWidth() / 2;
		int yFromCenter = from.getY() + from.getHeight() / 2;
		int xToCenter 	= to.getX() + to.getWidth() / 2;
		int yToCenter 	= to.getY() + to.getHeight() / 2;
		int xMiddle 	= (xFromCenter + xToCenter) / 2;
		int yMiddle 	= (yFromCenter + yToCenter) / 2;
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo(xFromCenter, yFromCenter);
		path.curveTo(xFromCenter, yMiddle, xMiddle, yToCenter, xToCenter, yToCenter);
		g2.draw(path);
		g2.dispose();
	}
}
