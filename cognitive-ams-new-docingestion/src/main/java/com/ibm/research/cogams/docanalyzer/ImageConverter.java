/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* Add this to the README file
 *
 *
 * Instructions to configure JMagick and Imagemagick in Ubuntu
 * Source: http://dissdoc.blogspot.in/2012/12/install-jmagick.html
 * 
 * Step1: Install imagemagick: sudo apt-get install imagemagick --fix-missing
 * Step2: Install jmagick: sudo apt-get install jmagick --fix-missing
 * Step3: Make sure that jmagick binary files (jmagick*.so or jmagick*.dll for windows os) are accessible for jvm. This files mast be             present in /usr/lib for linux os or in java_home/bin folders. If this files are absent simple copy it from jmagick                      installation package
 * Step4: If you start with Tomcat. Set variables
            -Djava.library.path=/usr/local/lib
            -Djmagick.systemclassloader=no 
*/

package com.ibm.research.cogams.docanalyzer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

import org.apache.commons.compress.utils.IOUtils;
/**
 *
 * @author Anush
 */
public class ImageConverter {
    
    public class ImageBundle {
        ImageInfo image;
        MagickImage magic_converter;
        
        public ImageBundle(){ }        
        public void setImage(ImageInfo image){ this.image = image;}    
        public void setConverter(MagickImage magic_converter){ this.magic_converter = magic_converter;}         
        public ImageInfo getImage(){ return this.image;}        
        public MagickImage getConverter(){ return this.magic_converter;}
    }
    
    private ImageConverter(){ }
    
    public ImageBundle readImageFromFile(String filePath) throws MagickException{
        ImageBundle image = new ImageBundle();
        ImageInfo imageInfo = new ImageInfo(filePath);
        image.setImage(imageInfo);
        image.setConverter(new MagickImage(imageInfo));
        return image;
    }
    
    public void writeImageToFile(ImageBundle image, String filePath) throws MagickException{
        ImageInfo imageInfo = image.getImage();
        MagickImage magic_converter = image.getConverter();
        magic_converter.setFileName(filePath);     
        magic_converter.writeImage(imageInfo);     
    }
    
    public ByteArrayInputStream imageToByteStream(ImageBundle image) throws MagickException{  
        ImageInfo imageInfo = image.getImage();
        MagickImage magic_converter = image.getConverter();
        byte[] imageBytes = magic_converter.imageToBlob(imageInfo);
        return new ByteArrayInputStream(imageBytes);
    }
    
    public ImageBundle byteStreamToImage(ByteArrayInputStream imageByteStream) throws MagickException, IOException{ 
        ImageBundle image = new ImageBundle();
        ImageInfo imageInfo = new ImageInfo();
        MagickImage magic_converter = new MagickImage();
        
        byte[] imageBytes = IOUtils.toByteArray(imageByteStream);
        //byte[] imageBytes = ByteStreams.toByteArray(imageByteStream);
        magic_converter.blobToImage(imageInfo, imageBytes);
        image.setImage(imageInfo);
        image.setConverter(magic_converter);       
        
        return image;
    }
    
    public static void main(String args[]) throws MagickException, IOException{
        String imagePath = "C:\\Users\\IBM_ADMIN\\Downloads\\GotEthicsNew.jpg";
        ImageConverter imgObject = new ImageConverter();
        
        // read image and convert to byte stream
        ImageBundle image = imgObject.readImageFromFile(imagePath);        
        ByteArrayInputStream imageByteStream = imgObject.imageToByteStream(image);
        
        // test my byte stream
        ImageBundle image_recon = imgObject.byteStreamToImage(imageByteStream);
        String imagePathDest = "C:\\Users\\IBM_ADMIN\\Downloads\\GotEthicsNew.png";
        imgObject.writeImageToFile(image_recon, imagePathDest);
    }    
}


