/*
 * org.openmicroscopy.shoola.agents.hiviewer.AnnotationEditor
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

package org.openmicroscopy.shoola.agents.hiviewer;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.AnnotationData;
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
public class AnnotationEditor
    extends CBDataLoader
{

    /** Identifies the <code>CREATE</code> annotation action. */
    public static final int         CREATE = 0;
    
    /** Identifies the <code>UPDATE</code> annotation action. */
    public static final int         UPDATE = 1;
    
    /** Identifies the <code>DELETE</code> annotation action. */
    public static final int         DELETE = 2;
    
    /** Indicates that we manipulate image annotations. */
    public static final int         IMAGE_ANNOTATION = 101;
    
    /** Indicates that we manipulate dataset annotations. */
    public static final int         DATASET_ANNOTATION = 104;
    
    /** 
     * The Annotation index, one of the following constants:
     * {@link #IMAGE_ANNOTATION} or {@link #DATASET_ANNOTATION}.
     */ 
    private int             annotationIndex;
    
    /** One of the constants defined by this class. */
    private int             actionIndex;
    
    /** The ID of the object to annotate. */
    private int             objectID;
    
    /** The annotation data object to update or delete. */
    private AnnotationData  data;
    
    /** The text of the annotation to create. */
    private String          text;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Checks if the action index is valid.
     * 
     * @param i The passed index.
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    private boolean checkActionIndex(int i)
    {
        switch (i) {
            case CREATE:
            case UPDATE:
            case DELETE:    
                return true;
        }
        return false;
    }
    
    /**
     * Checks if the annotation index is valid.
     * 
     * @param i The passed index
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    private boolean checkAnnotationIndex(int i)
    {
        switch (i) {
            case IMAGE_ANNOTATION:
            case DATASET_ANNOTATION:  
                return true;
        }
        return false;
    }
    
    /**
     * Returns the Class corresponding to the annotation index.
     * 
     * @return See above.
     */
    private Class getAnnotationClass()
    {
        switch (annotationIndex) {
            case IMAGE_ANNOTATION:
                return ImageData.class;
            case DATASET_ANNOTATION:
                return DatasetData.class;
            default:
                return null;
        }
    }
    
    /**
     * 
     * @param clipBoard
     * @param actionIndex
     * @param annotationIndex
     * @param data
     */
    public AnnotationEditor(ClipBoard clipBoard, int actionIndex, 
                            int annotationIndex, AnnotationData data)
    {
        super(clipBoard);
        if (!checkActionIndex(actionIndex))
            throw new IllegalArgumentException("Action not supported: "
                    +actionIndex);
        if (!checkAnnotationIndex(annotationIndex))
            throw new IllegalArgumentException("Annotation not supported: "
                    +annotationIndex);
        if (data == null)
            throw new IllegalArgumentException("Annotation not valid");
        this.annotationIndex = annotationIndex;
        this.actionIndex = actionIndex;
        this.data = data;
    }

    /**
     * 
     * @param clipBoard
     * @param actionIndex
     * @param annotationIndex
     * @param objectID
     * @param data
     */
    public AnnotationEditor(ClipBoard clipBoard, int actionIndex, 
                            int annotationIndex, int objectID,
                            AnnotationData data)
    {
        super(clipBoard);
        if (!checkActionIndex(actionIndex))
            throw new IllegalArgumentException("Action not supported: "
                    +actionIndex);
        if (!checkAnnotationIndex(annotationIndex))
            throw new IllegalArgumentException("Annotation not supported: "
                    +annotationIndex);
        if (data == null)
            throw new IllegalArgumentException("Annotation not valid");
        if (objectID < 0)
            throw new IllegalArgumentException("ObjectID not valid: "+objectID);
        this.annotationIndex = annotationIndex;
        this.actionIndex = actionIndex;
        this.data = data;
        this.objectID = objectID;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard
     * @param actionIndex
     * @param annotationIndex
     * @param objectID
     * @param text
     */
    public AnnotationEditor(ClipBoard clipBoard, int actionIndex, 
                            int annotationIndex, int objectID, String text)
    {
        super(clipBoard);
        if (!checkActionIndex(actionIndex))
            throw new IllegalArgumentException("Action not supported: "
                    +actionIndex);
        if (!checkAnnotationIndex(annotationIndex))
            throw new IllegalArgumentException("Annotation not supported: "
                    +annotationIndex);
        if (text == null)
            throw new IllegalArgumentException("Annotation not valid");
        if (objectID < 0)
            throw new IllegalArgumentException("ObjectID not valid: "+objectID);
        this.annotationIndex = annotationIndex;
        this.actionIndex = actionIndex;
        this.text = text;
        this.objectID = objectID;
    }
    
    /**
     * Creates or updates the annotation.
     * 
     * @see CBDataLoader#load()
     */
    public void load()
    {
        switch (actionIndex) {
            case CREATE:
                handle = hiBrwView.createAnnotation(getAnnotationClass(),
                                                    objectID, text, this);
                break;
            case UPDATE:
                handle = hiBrwView.updateAnnotation(getAnnotationClass(),
                                                    objectID, data, this);
                break;
            case DELETE:
                handle = hiBrwView.deleteAnnotation(getAnnotationClass(), data,
                                                    this);
                break;
        }
    }

    /** Cancels the data saving. */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see CBDataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        clipBoard.manageAnnotationEditing(((Boolean) result).booleanValue());
    }

}
