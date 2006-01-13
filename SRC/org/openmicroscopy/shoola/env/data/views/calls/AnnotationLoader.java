/*
 * org.openmicroscopy.shoola.env.data.views.calls.AnnotationLoader
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

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnnotationLoader
    extends BatchCallTree
{
    
    /** The set of retrieved annotations. */
    private Map         annotations;
    
    /** Loads the specified annotations. */
    private BatchCall   loadCall;
    
    /**
     * Creates {@link BatchCall} if the type is supported.
     * 
     * @param nodeType 	The type of the node. Can only be one out of:
     *                 	{@link DatasetData}, {@link ImageData}.
     * @param nodeIDs  	Collection of node's ids.
     * @param history  	Pass <code>true</code> to retrieve all the annotations
     * 					related to the specified rootNode even if they are
     * 					no longer valid.
     */
    private void validate(Class nodeType, Set nodeIDs, boolean history)
    {
        if (nodeType == null) 
            throw new IllegalArgumentException("No node type.");
        if (nodeIDs == null && nodeIDs.size() == 0)
            throw new IllegalArgumentException("No root node ids.");
        try {
            nodeIDs.toArray(new Integer[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "nodeIDs can only contain Integer.");
        }  
        if (nodeType.equals(DatasetData.class) ||
            nodeType.equals(ImageData.class))
            loadCall = makeAnnotationBatchCall(nodeType, nodeIDs, history);
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the annotation, either 
     * dataset's Annotation or image's Annotation.
     * 
     * @param nodeType 	The type of the node. Can only be one out of:
     *                 	{@link DatasetData}, {@link ImageData}.
     * @param nodeIDs  	Collection of node's ids.
     * @param history  	Pass <code>true</code> to retrieve all the annotations
     * 					related to the specified rootNode even if they are
     * 					no longer valid.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeAnnotationBatchCall(final Class nodeType,
                                  final Set nodeIDs, final boolean history)
    {
        return new BatchCall("Loading annotation") {
            public void doCall() throws Exception
            {
                OmeroPojoService os = context.getOmeroService();
                annotations = os.findAnnotations(nodeType, nodeIDs, history);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return annotations; }

    /**
     * Creates a new instance to load the annotations related to the specified
     * node. We retrieve either DatasetAnnotation or ImageAnnotaion according
     * to the specified class.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param nodeType 	The type of the node. Can only be one out of:
     *                 	{@link DatasetData}, {@link ImageData}.
     * @param nodeID  	The id of the node.
     * @param history 	Pass <code>true</code> to retrieve all the annotations
     * 					related to the specified rootNode even if they are no 
     * 					longer valid.
     */
    public AnnotationLoader(Class nodeType, int nodeID, boolean history)
    {
        HashSet set = new HashSet(1);
        set.add(new Integer(nodeID));
        validate(nodeType, set, history);
    }
    
    /**
     * Creates a new instance to load the annotations related to the specified
     * node. We retrieve either DatasetAnnotation or ImageAnnotaion according
     * to the specified class.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param nodeType 	The type of the node. Can only be one out of:
     *                 	{@link DatasetData}, {@link ImageData}.
     * @param nodeIDs  	Collection of node's ids.
     * @param history 	Pass <code>true</code> to retrieve all the annotations
     * 					related to the specified rootNode even if they are no 
     * 					longer valid.
     */
    public AnnotationLoader(Class nodeType, Set nodeIDs, boolean history)
    {
        validate(nodeType, nodeIDs, history);
    }
    
}
