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



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Node extends JComponent implements Comparable<Node> {
	private static final long 		serialVersionUID 	= 5542054625603014824L;
	String 							key;
	String 							author;
	int								date;
	String 							fullMessage;
	String 							shortMessage;
	ArrayList<Node> 				children;
	ArrayList<Node> 				parents;
	static HashMap<String, Node> 	nodeMap 			= new HashMap<String, Node>();
	static JLabel 					renderer 			= new JLabel();
	int 							rank 				= 0;
	static int 						gap 				= 6;

	public static void createNode(RevWalk walk, Ref ref, boolean recur) throws IOException {
		Node.createNode(walk, ref.getObjectId(), recur);
	}

	public static Node createNode(RevWalk walk, ObjectId id, boolean recur) throws IOException {
		String 		key = id.getName();
		RevCommit 	commit;
		Node 		node;

		if (nodeMap.containsKey(key)) {
			// commit node was already mapped
			node = nodeMap.get(key);
		} else {
			// create new commit node
			commit 	= walk.parseCommit(id);
			node 	= new Node(key, commit.getAuthorIdent().getEmailAddress(), commit.getCommitTime(), commit.getFullMessage(), commit.getShortMessage());
			node.setBackground(new Color(240, 240, 255));
			node.setForeground(new Color(0, 0, 127));

			if (recur) {
				// add parent nodes
				for (ObjectId parentCommit : commit.getParents())
					node.addParent(Node.createNode(walk, parentCommit, recur));
			}
		}

		return node;
	}

	Node() {
		this.children 		= new ArrayList<Node>();
		this.parents 		= new ArrayList<Node>();
	}

	public Node(String key, String author, Integer date, String fullMessage, String shortMessage) {
		// assign fields
		this.key			= key;
		this.author 		= author;
		this.date 			= date;
		this.fullMessage 	= fullMessage;
		this.shortMessage 	= shortMessage;
		this.children 		= new ArrayList<Node>();
		this.parents 		= new ArrayList<Node>();

		// put node in map
		nodeMap.put(key, this);

		// set tooltip
		setToolTipText(fullMessage);
	}

	public void addParent(Node parent) {
		// add parent node and set this node as child of parent
		if (parent != null && !parents.contains(parent)) {
			parents.add(parent);
			parent.children.add(this);
		}
	};

	@Override
	public int compareTo(Node other) {
		return this.date - other.date;
	}

	@Override
	@Transient
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	@Transient
	public Dimension getPreferredSize() {
		Dimension dim = new Dimension(2 * gap, 2 * gap);

		renderer.setText(key);
		Dimension pref 	= renderer.getPreferredSize();
		dim.height += pref.height + 3;

		renderer.setText(author);
		pref 		= renderer.getPreferredSize();
		dim.width 	= Math.max(pref.width + 2 * gap, dim.width);
		dim.height += pref.height + 2;

		Date dat 	= new Date(date * 1000L);
		renderer.setText(dat.toString());
		pref 		= renderer.getPreferredSize();
		dim.width 	= Math.max(pref.width + 2 * gap, dim.width);
		dim.height += pref.height + 3;

		renderer.setText(shortMessage);
		pref 		= renderer.getPreferredSize();
		dim.height += pref.height;

		return dim;
	}

	@Override
	@Transient
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, gap, gap);
		g2.setColor(getForeground());
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, gap, gap);
		g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, gap - 2, gap - 2);

		int w = getPreferredSize().width - 2 * gap;

		g2.translate(gap - 1, gap - 1);
		renderer.setText(key);
		renderer.setSize(w, renderer.getPreferredSize().height);
		renderer.paint(g2);

		g2.translate(0, renderer.getHeight() + 2);
		g2.drawLine(0, 0, getWidth() - 2 * gap, 0);

		g2.translate(0, 2);
		renderer.setText(author);
		renderer.setSize(w, renderer.getPreferredSize().height);
		renderer.paint(g2);

		g2.translate(0, renderer.getHeight() + 2);

		if (date > 0) {
			Date dat = new Date(date * 1000L);
			renderer.setText(dat.toString());
		} else {
			renderer.setText(null);
		}

		renderer.setSize(w, renderer.getPreferredSize().height);
		renderer.paint(g2);

		g2.translate(0, renderer.getHeight() + 2);
		g2.drawLine(0, 0, getWidth() - 2 * gap, 0);

		g2.translate(0, 2);
		renderer.setText(shortMessage);
		renderer.setSize(w, renderer.getPreferredSize().height);
		renderer.paint(g2);
		g2.dispose();
	}
}
