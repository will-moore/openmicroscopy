/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.Component;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Determines and sets the icon corresponding to a data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeCellRenderer
    extends DefaultTreeCellRenderer
{
    
    /** Reference to the {@link IconManager}. */
    private IconManager         icons;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * If an icon is passed, the passed icon is set
     * 
     * @param usrObject The user's object.
     */
    private void setIcon(Object usrObject)
    {
        Icon icon = null;
        if (usrObject instanceof ProjectData)
            icon = icons.getIcon(IconManager.PROJECT);
        else if (usrObject instanceof DatasetData) {
            Set annotations = ((DatasetData) usrObject).getAnnotations();
            if (annotations == null || annotations.size() == 0)
                icon = icons.getIcon(IconManager.DATASET);
            else icon = icons.getIcon(IconManager.ANNOTATED_DATASET);
        } else if (usrObject instanceof ImageData) {
            Set annotations = ((ImageData) usrObject).getAnnotations();
            if (annotations == null || annotations.size() == 0)
                icon = icons.getIcon(IconManager.IMAGE);
            else icon = icons.getIcon(IconManager.ANNOTATED_IMAGE);
            
        } else if (usrObject instanceof CategoryGroupData)
            icon = icons.getIcon(IconManager.CATEGORY_GROUP);
        else if (usrObject instanceof CategoryData)
            icon = icons.getIcon(IconManager.CATEGORY);
        setIcon(icon);
    }

    /** Creates a new instance. */
    public TreeCellRenderer()
    {
        icons = IconManager.getInstance();
    }
    
    /**
     * Overriden to set the icon and the text.
     * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     * 								boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        if (!(value instanceof TreeImageDisplay)) return this;
        TreeImageDisplay  node = (TreeImageDisplay) value;
        
        if (node.getLevel() == 0) {
            setIcon(icons.getIcon(IconManager.ROOT));
            return this;
        }
        setText(node.getNodeText());
        setToolTipText(node.getToolTip());
        setIcon(node.getUserObject());
        return this;
    }
    
}
