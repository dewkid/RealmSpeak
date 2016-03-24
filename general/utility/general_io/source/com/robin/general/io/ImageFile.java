/*
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2016 Robin Warren
 * E-mail: robin@dewkid.com
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;

public class ImageFile {
    /**
     * Saves the given image using JPEG compression, to the specified file.
     * Compression quality ranges between 0 and 1, where 1 specifies
     * minimum compression and maximum quality.
     *
     * @param bi                 the image to save
     * @param outfile            the file path to save to
     * @param compressionQuality ranges between 0 and 1, where 1 specifies
     *                           minimum compression and maximum quality
     * @return true on success
     */
    public static boolean saveJpeg(BufferedImage bi, File outfile,
                                   float compressionQuality) {
        try {
            // Retrieve jpg image to be compressed
            RenderedImage rendImage = bi;

            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
                writer = (ImageWriter) iter.next();
            }

            // instantiate an ImageWriteParam object with default compression options
            ImageWriteParam iwp = writer.getDefaultWriteParam();

            //Now, we can set the compression quality:
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(compressionQuality);

            // Prepare output file
            ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
            writer.setOutput(ios);

            // Write the image
            writer.write(null, new IIOImage(rendImage, null, null), iwp);

            // Cleanup
            ios.flush();
            writer.dispose();
            ios.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}