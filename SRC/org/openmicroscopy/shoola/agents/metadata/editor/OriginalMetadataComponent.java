/*
 * org.openmicroscopy.shoola.agents.metadata.editor.OriginalMetadataComponent
 *
�*------------------------------------------------------------------------------
 * �Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * ����This program is free software; you can redistribute it and/or modify
 * �it under the terms of the GNU General Public License as published by
 * �the Free Software Foundation; either version 2 of the License, or
 * �(at your option) any later version.
 * �This program is distributed in the hope that it will be useful,
 * �but WITHOUT ANY WARRANTY; without even the implied warranty of
 * �MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. �See the
 * �GNU General Public License for more details.
 *
 * �You should have received a copy of the GNU General Public License along
 * �with this program; if not, write to the Free Software Foundation, Inc.,
 * �51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;


//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/**
 * Displays the original metadata.
 *
 * @author �Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * ����<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author ���Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * ����<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class OriginalMetadataComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** The columns of the table. */
	private static final String[] COLUMNS;

	static {
		COLUMNS = new String[2];
		COLUMNS[0] = "Tag";
		COLUMNS[1] = "Value";
	}
	
	/** Reference to the model. */
	private EditorModel	model;
	
	/** 
	 * Brings up a dialog so that the user can select where to 
	 * download the file.
	 */
	private void download()
	{
		JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.FOLDER_CHOOSER, 
				"Download", "Select where to download the file.");
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}
	
	/** Flag indicating if the metadata have been loaded or not. */
	private boolean metadataLoaded;
	
	/** Button to download the file. */
	private JButton		downloadButton;
	
	/** Builds the tool bar displaying the controls. */
	private JComponent	toolBar;
	
	/** The bar displaying the status. */
	private JComponent	statusBar;
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		downloadButton = new JButton();
		Icon icon = icons.getIcon(IconManager.DOWNLOAD);
		downloadButton = new JButton(icon);
		downloadButton.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		downloadButton.setToolTipText("Download the metadata file.");
		downloadButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) { download(); }
		});
		toolBar = buildToolBar();
		
		JXBusyLabel label = new JXBusyLabel(new Dimension(icon.getIconWidth(), 
				icon.getIconHeight()));
		label.setBusy(true);
		label.setText("Loading metadata");
		//label.setHorizontalTextPosition(JXBusyLabel.RIGHT);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(label);
		p.add(Box.createHorizontalStrut(5));
		p.add(new JLabel("Loading metadata"));
		statusBar = UIUtilities.buildComponentPanel(p);
		
		setLayout(new BorderLayout(0, 0));
		add(statusBar, BorderLayout.NORTH);
	}
	
    /** 
     * Builds the tool bar.
     * 
     * @return See above.
     */
    private JComponent buildToolBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(downloadButton);
    	return bar;
    }
    
	/** 
	 * Builds and lays out the UI.
	 * 
	 * @param components The components to lay out.
	 */
	private void buildGUI(Map<String, List<String>> components)
	{
		//Now lay out the elements
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.weightx = 1.0;
		String key;
		List l;
		Entry entry;
		Iterator i = components.entrySet().iterator();
		p.add(new JSeparator(), constraints);
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			l = (List) entry.getValue();
			if (l != null && l.size() > 0) {
				constraints.gridy++;
				p.add(UIUtilities.buildComponentPanel(
						UIUtilities.setTextFont(key)), constraints);
				constraints.gridy++;
				p.add(createTable(l), constraints);
			}
		}
		removeAll();
		add(toolBar, BorderLayout.NORTH);
		add(p, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new table.
	 * 
	 * @param list The list of elements to add to the table.
	 * @return See above
	 */
	private JScrollPane createTable(List<String> list)
	{
		Iterator<String> i = list.iterator();
		String[] values;
		Object[][] data = new Object[list.size()][2];
		int index = 0;
		while (i.hasNext()) {
			values =  i.next().split("=");
			data[index][0] = values[0];
			data[index][1] = values[1];
			index++;
		}
		JXTable table = new JXTable(new DefaultTableModel(data, COLUMNS));
		Highlighter h = HighlighterFactory.createAlternateStriping(
				UIUtilities.BACKGROUND_COLOUR_EVEN, 
				UIUtilities.BACKGROUND_COLOUR_ODD);
		table.addHighlighter(h);
		return new JScrollPane(table);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model.
	 */
	OriginalMetadataComponent(EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
	}
	
	/**
	 * Returns <code>true</code> if the metadata have been loaded.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMetadataLoaded() { return metadataLoaded; }

	/** Removes all the components when a new node is selected. */
	void clear()
	{ 
		metadataLoaded = false; 
		removeAll();
		add(statusBar, BorderLayout.NORTH);
	}
	
	/**
	 * Reads the file and displays the result in a table.
	 * 
	 * @param file The file to read.
	 */
	void setOriginalFile(File file)
		throws IOException
	{
		metadataLoaded = true;
		BufferedReader input = new BufferedReader(new FileReader(file));
		Map<String, List<String>> components = 
			new LinkedHashMap<String, List<String>>();
		try {
			String line = null;
			List<String> l;
			String key = null;
			while ((line = input.readLine()) != null) {
				if (line.contains("=")) {
					if (key != null) {
						l = components.get(key);
						if (l != null) l.add(line);
					}
				} else {
					key = line.substring(1, line.length()-1);
					components.put(key, new ArrayList<String>());
				}
			}
			buildGUI(components);
		} finally {
			input.close();
		}
	}

	/**
	 * Downloads the original file.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File folder = (File) evt.getNewValue();
			if (folder == null)
				folder = UIUtilities.getDefaultFolder();
			UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
			un.notifyDownload(model.getOriginalMetadata(), folder);
		}
	}
	
}
