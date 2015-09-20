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



import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GraphLayout implements LayoutManager {
	HashMap<Node, Integer> 		ranking = new HashMap<Node, Integer>();
	ArrayList<ArrayList<Node>> 	rows 	= new ArrayList<ArrayList<Node>>();
	ArrayList<Integer> 			colW 	= new ArrayList<Integer>();
	boolean 					valid 	= false;
	Dimension 					size 	= new Dimension();
	static int 					gap 	= 10;

	@Override
	public void addLayoutComponent(String arg0, Component arg1) {
	}

	@Override
	public void layoutContainer(Container panel) {
		computeSize(panel);

		int y = gap;

		// rows contains all nodes: use it to position the nodes
		for (int iRow = rows.size() - 1; iRow >= 0; iRow--) {
			ArrayList<Node> row = rows.get(iRow);
			int 			h 	= 0;
			int 			x 	= gap;

			for (int iCol = 0; iCol < row.size(); iCol++) {
				Node node = row.get(iCol);

				if (node != null) {
					Dimension p = node.getPreferredSize();
					node.setBounds(x, y, p.width, p.height);
					h = Math.max(h, p.height);
				}

				x += colW.get(iCol) + gap;
			}

			y += h + gap;
		}

		// edgeList contains all edges: use it to position all edges
		for (Edge edge : Edge.edgeList) {
			edge.setBounds(0, 0, size.width, size.height);
		}
	}

	private void computeSize(Container panel) {

		if (!valid) {
			// recompute size
			rankNodes(Node.nodeMap);
			orderNodes(Node.nodeMap);
			collapseNodes(Node.nodeMap);

			int totalHeight = gap;
			int totalWidth 	= gap;
			colW.clear();

			for (ArrayList<Node> row : rows) {
				int maxHeight = 0;

				for (int iCol = 0; iCol < row.size(); iCol++) {
					Node node = row.get(iCol);

					while (colW.size() <= iCol) {
						colW.add(0);
					}

					if (node != null) {
						Dimension pref 	= node.getPreferredSize();
						maxHeight 		= Math.max(maxHeight, pref.height);
						colW.set(iCol, Math.max(colW.get(iCol), pref.width));
					}
				}

				totalHeight += maxHeight + gap;
			}

			for (int w : colW)
				totalWidth += w + gap;

			size.setSize(totalWidth, totalHeight);
			valid = true;
		}
	}

	private void collapseNodes(HashMap<String, Node> nodeMap) {
		boolean changed = true;

		while (changed) {
			changed = false;

			for (Node node : nodeMap.values()) {
				// check if minimum rank difference > 1
				int parentRank = 0;

				for (Node parentNode : node.parents)
					parentRank = Math.max(parentRank, parentNode.rank);

				if ((node.rank - parentRank) > 1) {
					// move node straight up (if space is not occupied)
					ArrayList<Node> row 	= rows.get(node.rank); 
					int 			col 	= row.indexOf(node);
					int 			newRank = parentRank + 1;
					ArrayList<Node> newRow 	= rows.get(newRank);

					if (newRow.size() <= col || newRow.get(col) == null) {
						row.set(col, null);
						node.rank = newRank;

						while (newRow.size() <= col)
							newRow.add(null);

						newRow.set(col, node);
						changed = true;
					}
				}
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container panel) {
		computeSize(panel);
		return size;
	}

	@Override
	public Dimension preferredLayoutSize(Container panel) {
		computeSize(panel);
		return size;
	}

	@Override
	public void removeLayoutComponent(Component panel) {
	}

	void rankNodes(HashMap<String, Node> nodeMap) {
		// rank all nodes in map: start at oldest node
		ranking.clear();
		ArrayList<Node> nodes = new ArrayList<Node>(nodeMap.values());
		Collections.sort(nodes);

		for (int iNode = 0; iNode < nodes.size(); iNode++)
			nodes.get(iNode).rank = iNode;
	}

	void orderNodes(HashMap<String, Node> nodeMap) {
		// order all nodes in map: start at first node
		rows.clear();
		ArrayList<Node> nodes = new ArrayList<Node>(nodeMap.values());
		Collections.sort(nodes);

		for (Node node : nodes)
			orderNode(node);
	}

	private void orderNode(Node node) {
		// order node (if it was not ordered before)
		int rowIdx = node.rank;

		// create new rows if necessary
		while (rows.size() <= rowIdx)
			rows.add(new ArrayList<Node>());

		ArrayList<Node> row = rows.get(rowIdx);

		if (!row.contains(node)) {
			// node was not ordered yet: order it and recur over children
			row.add(node);

			ArrayList<Node> children = new ArrayList<Node>(node.children);
			Collections.sort(children, Collections.reverseOrder());

			for (Node child : children) {
				// create new rows if necessary
				while (rows.size() <= child.rank)
					rows.add(new ArrayList<Node>());

				// fill the gap with dummies
				for (int dummy = node.rank + 1; dummy < child.rank; dummy++)
					rows.get(dummy).add(null);

				// add the child
				orderNode(child);
			}
		}
	}
}
