/*
 * org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
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
public class AnnotationSaver
    extends BatchCallTree
{
    
    /** The save call. */
    private BatchCall       saveCall;
    
    //TODO: remove asap
    private AnnotationData transformPojoAnnotationData(pojos.AnnotationData ad)
    {
        AnnotationData annotation = new AnnotationData(ad.getId(),
                                                    ad.getOwner().getId(),
                                                    ad.getLastModified());
        annotation.setAnnotation(ad.getText());
        return annotation;
    }
    
    /**
     * Creates a {@link BatchCall} to remove the specified annotation.
     * 
     * @param data The annotation to save.
     * @return The {@link BatchCall}.
     */
    private BatchCall removeDatasetAnnotation(final AnnotationData data)
    {
        return new BatchCall("Remove dataset annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.removeDatasetAnnotation(data);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to remove the specified annotation.
     * 
     * @param data The annotation to remove.
     * @return The {@link BatchCall}.
     */
    private BatchCall removeImageAnnotation(final AnnotationData data)
    {
        return new BatchCall("Remove image annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.removeImageAnnotation(data);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified annotation.
     * 
     * @param nodeID 	The id to the <code>DataObject</code>.
     * @param data 		The updated annotation.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateImageAnnotation(final int nodeID, 
                                            final AnnotationData data)
    {
        return new BatchCall("Update image annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.updateImageAnnotation(data, nodeID);
            }     
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified annotation.
     * 
     * @param nodeID 	The id to the <code>DataObject</code>.
     * @param data 		The updated annotation.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateDatasetAnnotation(final int nodeID, 
                                            final AnnotationData data)
    {
        return new BatchCall("Update dataset annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.updateDatasetAnnotation(data, nodeID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified annotation.
     * 
     * @param id 	The id to the <code>DataObject</code>.
     * @param data 		The updated annotation.
     * @return The {@link BatchCall}.
     */
    private BatchCall createImageAnnotation(final int id, final String data)
    {
        return new BatchCall("Create image annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.createImageAnnotation(id, data, -1, -1);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to create a dataset annotation.
     * 
     * @param id 	The id to the <code>DataObject</code>.
     * @param data 		The updated annotation.
     * @return The {@link BatchCall}.
     */
    private BatchCall createDatasetAnnotation(final int id, final String data)
    {
        return new BatchCall("Create dataset annotation.") {
            public void doCall() throws Exception
            {
                SemanticTypesService sts = context.getSemanticTypesService();
                sts.createDatasetAnnotation(id, data);
            }
        };
    }
    
    /**
     * Adds the {@link #saveCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

    /**
     * Returns <code>null</code>, as the return type of the underlying call
     * <code>void</code>.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return Boolean.TRUE; }

    /**
     * Creates a new instance.
     * Constructor invokes to update an annotation.
     * 
     * @param nodeType The type of the node. Can either be
     *                  <code>DatasetData or ImageData</code>. 
     * @param nodeID The id of the node.
     * @param data The Annotation object.
     */
    public AnnotationSaver(Class nodeType, int nodeID,
                            pojos.AnnotationData data)
    {
        if (data == null) throw new IllegalArgumentException("no annotation.");
        if (nodeType.equals(DatasetData.class))
            saveCall = updateDatasetAnnotation(nodeID,
                    transformPojoAnnotationData(data));
        else if (nodeType.equals(ImageData.class))
            saveCall = updateImageAnnotation(nodeID,
                    transformPojoAnnotationData(data));
        else throw new IllegalArgumentException("Unsupported type: "+nodeType);
    }
    
    /**
     * Creates a new instance.
     * Constructor invokes to create an annotation.
     * 
     * @param nodeType The type of the node. Can either be
     *                  <code>DatasetData or ImageData</code>.
     * @param nodeID The id of the node.
     * @param data The textual annotation.
     */
    public AnnotationSaver(Class nodeType, int nodeID, String data)
    {
        if (data == null) throw new IllegalArgumentException("no annotation."); 
        if (nodeID < 0) throw new IllegalArgumentException("ID not valid.");
        if (nodeType.equals(DatasetData.class))
            saveCall = createDatasetAnnotation(nodeID, data);
        else if (nodeType.equals(ImageData.class))
            saveCall = createImageAnnotation(nodeID, data);
        else throw new IllegalArgumentException("Unsupported type: "+nodeType);
    }
    
    /**
     * Creates a new instance.
     * Constructor invokes to delete an annotation.
     * 
     * @param nodeType The type of the node. Can either be
     *                  <code>DatasetData or ImageData</code>.
     * @param data The Annotation object.
     */
    public AnnotationSaver(Class nodeType, pojos.AnnotationData data)
    {
        if (data == null) throw new IllegalArgumentException("no annotation."); 
        if (nodeType.equals(DatasetData.class))
            saveCall = removeDatasetAnnotation(
                    transformPojoAnnotationData(data));
        else if (nodeType.equals(ImageData.class))
            saveCall = removeImageAnnotation(transformPojoAnnotationData(data));
        else throw new IllegalArgumentException("Unsupported type: "+ nodeType);
    }
    
}
