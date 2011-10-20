"""
 components/tools/OmeroPy/scripts/omero/util_scripts/Rotate.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2011 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------

This script creates new images from existing images, rotating them by a 
specified amount OR to align a line vertically or horizontally.
Requires PIL or scipy.

@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3
 
"""

import omero
from omero.gateway import BlitzGateway
import omero.scripts as scripts
import omero_api_IRoi_ice
from omero.rtypes import *
import omero.util.script_utils as script_utils

import os
from numpy import zeros, hstack, vstack

import time


def newImageWithRotate(conn, scriptParams, imageId):
    """
    Process a single image here: creating a new image and passing planes from
    original image to new image - applying rotation to each channel as we go.
    
    @param imageId:     Original image
    """
    
    oldImage = conn.getObject("Image", imageId)
    if oldImage is None:
        print "Image not found for ID:", imageId
        return


    # these dimensions don't change
    sizeZ = oldImage.getSizeZ()
    sizeC = oldImage.getSizeC()
    sizeT = oldImage.getSizeT()
    sizeX = oldImage.getSizeX()
    sizeY = oldImage.getSizeY()
    colors = [c.getColor().getRGB() for c in oldImage.getChannels()]

    # check we're not dealing with Big image.
    rps = oldImage.getPrimaryPixels()._prepareRawPixelsStore()
    bigImage = rps.requiresPixelsPyramid()
    rps.close()
    if bigImage:
        print "This script does not support 'BIG' images such as Image ID: %s X: %d Y: %d" % (imageId, sizeX, sizeY)
        return

    def planeGen():
        pixels = oldImage.getPrimaryPixels()
        dt = None
        # get the planes one at a time - exceptions on getPlane() don't affect subsequent calls (new RawPixelsStore)
        for i in range(len(zctList)):
            z,c,t = zctList[i]
            offsets = offsetMap[c]
            if z < 0 or z >= sizeZ:
                print "Black plane for zct:", zctList[i]
                if dt is None:
                    # if we are on our first plane, we don't know datatype yet...
                    dt = pixels.getPlane(0,0,0).dtype  # hack! TODO: add method to pixels to supply dtype
                plane = zeros((sizeY, sizeX), dt)
            else:
                print "getPlane for zct:", zctList[i], "applying offsets:", offsets
                try:
                    plane = pixels.getPlane(*zctList[i])
                    dt = plane.dtype
                except:
                    # E.g. the Z-index is out of range - Simply supply an array of zeros.
                    if dt is None:
                        # if we are on our first plane, we don't know datatype yet...
                        dt = pixels.getPlane(0,0,0).dtype  # hack! TODO: add method to pixels to supply dtype
                    plane = zeros((sizeY, sizeX), dt)
            yield offsetPlane(plane, offsets['x'], offsets['y'])
    
    # create a new image with our generator of numpy planes.
    newImageName = "%s_offsets" % oldImage.getName()
    descLines = [" Channel %s: Offsets x: %s y: %s z: %s" % (c['index'], c['x'], c['y'], c['z']) for c in channel_offsets]
    desc = "Image created from Image ID: %s by applying Channel Offsets:\n" % imageId
    desc += "\n".join(descLines)
    serviceFactory = conn.c.sf  # make sure that script_utils creates a NEW rawPixelsStore
    i = conn.createImageFromNumpySeq(offsetPlaneGen(), newImageName,
        sizeZ=sizeZ, sizeC=len(offsetMap.items()), sizeT=sizeT, description=desc, dataset=dataset)

    # apply colors from the original image to the new one
    i._prepareRenderingEngine()
    print "Applying colors..."
    for c, color in enumerate(newImageColors):
        r,g,b = color
        print "Index %d: r,g,b: %d, %d, %d" % (c, r, g, b)
        i._re.setRGBA(c, r, g, b, 255)
    i._re.saveCurrentSettings()
    i._re.close()
    return i

def processImages(conn, scriptParams):
    """ For each image, call newImageWithRotate() """

    # need to handle Datasets eventually - Just do images for now
    newImgIds = []
    for iId in scriptParams['IDs']:
        newImg = newImageWithRotate(conn, scriptParams, iId)
        if newImg is not None:
            newImgIds.append(newImg.getId())
    return newImgIds
    
def runAsScript():

    dataTypes = [rstring('Image')]

    client = scripts.client('Channel_Offsets.py', """This script creates new images from existing images, rotating them by a 
    specified amount OR rotating to align an ROI line vertically or horizontally. """,

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Pick Images by 'Image' ID or by the ID of their 'Dataset'", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs to process.").ofType(rlong(0)),

    scripts.List("Channels", grouping="3", min=1,
        description="Indices of channels to rotate (Rotate ALL channels by default)").ofType(rint(0)),

    scripts.Float("Degrees", grouping="4", min=-360, max=360,
        description="Rotate clockwise by the specified number of degrees. Negative is counter-clockwise."),

    scripts.String("Align_Line", grouping="5", default=True,
        description="Choose to include this channel in the output image"),

    scripts.Int("Line_Label", grouping="5.1", default='rotate',
        description="If multiple lines on this image, only use line with the specified text label"),

    version = "4.2.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )
    
    try:
        session = client.getSession()

        # process the list of args above.
        scriptParams = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = client.getInput(key, unwrap=True)

        print scriptParams
        
        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)
        
        result = processImages(conn, scriptParams)
        if result is None:
            message = "Script failed. See 'Info' or 'Error' for more details"
        else:
            newImgIds, dataset = result
            if len(newImgIds) == 1:
                newImg = conn.getObject("Image", newImgIds[0])
                message = "New Image created: %s" % newImg.getName()
                client.setOutput("Image", robject(newImg._obj))
            elif len(newImgIds) > 1:
                message = "%s new Images created" % len(newImgIds)
            else:
                message = "No images created. See 'Info' or 'Error' for more details"
            if dataset is not None:
                client.setOutput("New Dataset", robject(dataset._obj))

        client.setOutput("Message", rstring(message))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()