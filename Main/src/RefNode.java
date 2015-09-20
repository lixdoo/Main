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
import java.io.IOException;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class RefNode extends Node {
	private static final long serialVersionUID = 3266833654120783259L;

	public static void createNode(RevWalk walk, Ref ref, boolean recur) throws IOException {
		String 		key = ref.getName();
		ObjectId 	id 	= ref.getObjectId();
		Node 		node;

		if (!nodeMap.containsKey(key)) {
			// create new ref node
			node = new RefNode(key);

			// determine color
			if (key.contains("/heads/")) {
				node.setBackground(new Color(240, 255, 240));
				node.setForeground(new Color(0, 127, 0));
			} else if (key.contains("/tags/")) {
				node.setBackground(new Color(255, 255, 240));
				node.setForeground(new Color(127, 127, 0));
			} else if (key.contains("/remotes/")) {
				node.setBackground(new Color(240, 240, 240));
				node.setForeground(new Color(0, 0, 0));
			} else {
				node.setBackground(new Color(255, 240, 240));
				node.setForeground(new Color(127, 0, 0));
			}

			if (recur) {
				// add parent node
				RevCommit commit = walk.parseCommit(id);
				node.addParent(Node.createNode(walk, commit, recur));
			}
		}
	}

	public RefNode(String key) {
		// assign fields
		this.key = key;
		this.date = (int) (new Date().getTime() / 1000);;

		// put node in map
		nodeMap.put(key, this);

		// set tooltip
		setToolTipText(key);
	}
}
