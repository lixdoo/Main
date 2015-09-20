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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// use system look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// build frame to display tree
		final JFrame 		frame 		= new JFrame("GitViz");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar 			menubar 	= new JMenuBar();
		JMenu 				menu 		= new JMenu("File");
		JMenuItem 			openRepo 	= new JMenuItem("Open repository ...");
		JScrollPane 		scroll 		= new JScrollPane();
		final JPanel 		panel 		= new GraphPanel();
		final GraphLayout 	layout 		= new GraphLayout();
		panel.setLayout(layout);
		panel.setBackground(Color.white);

		// scroll using keys
		JScrollBar vertical = scroll.getVerticalScrollBar();
		InputMap im = vertical.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "positiveBlockIncrement");
		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "negativeBlockIncrement");
		im.put(KeyStroke.getKeyStroke("HOME"), "minScroll");
		im.put(KeyStroke.getKeyStroke("END"), "maxScroll");

		// add action to openRepo menu item
		openRepo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				// 1. select repository
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "Git repository";
					}

					@Override
					public boolean accept(File file) {
						return file.isDirectory() && (!file.getName().startsWith(".") || file.getName().endsWith(".git"));
					}
				});

				int returnVal = fileChooser.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();

					// get access to repository
					try {
						Repository 			repository 	= FileRepositoryBuilder.create(file);
						Map<String, Ref> 	refs 		= repository.getAllRefs();
						RevWalk 			walk 		= new RevWalk(repository);

						// 2. clear the gui
						Edge.edgeList.clear();
						layout.valid = false;
						Node.nodeMap.clear();
						panel.removeAll();

						// loop over references and create nodes (recursively)
						for (Ref ref : refs.values())
							RefNode.createNode(walk, ref, true);

						// add all commit nodes to the panel; create edges as we go
						for (Node node : Node.nodeMap.values()) {
							panel.add(node);

							for (Node parent : node.parents) {
								Edge.edgeList.add(new Edge(node, parent));
							}
						}

						// add all edges to the panel
						for (Edge edge : Edge.edgeList)
							panel.add(edge);

						frame.revalidate();

					} catch (IOException e) {
						// failed to open repository
						System.out.println(e.getStackTrace());
						return;
					}
				}
			}
		});

		scroll.setViewportView(panel);
		frame.setContentPane(scroll);
		menu.add(openRepo);
		menubar.add(menu);
		frame.setJMenuBar(menubar);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}
