package com.spotjoe.practice;
import java.awt.AlphaComposite;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;


public class OpenCVTest {
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		Mat mat = Mat.eye(3,  3, CvType.CV_8UC1);
		Mat vec1 = Mat.eye(4, 5, CvType.CV_32FC1);
		Mat vec2 = Mat.ones(4, 5, CvType.CV_32FC1);
		Mat mat2 = Mat.zeros(2,20,CvType.CV_32FC1);
//		vec1.copyTo(mat2.row(0));
		//vec2.copyTo(mat2);
		vec1 = vec1.reshape(1,1);
		vec1.copyTo(mat2.row(0));
		System.out.println(vec1.dump());
		
		try {
			File file = new File("F:\\IMG_0821.JPG");
			if(file.exists()){
			 Mat img = Highgui.imread(file.getPath());
			 if(img == null) return;
			 Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
			 img.convertTo(img, CvType.CV_32FC1);
			 MatOfByte matOfByte = new MatOfByte();  
			 Highgui.imencode(".jpg", img, matOfByte);  
			 byte[] byteArray = matOfByte.toArray();  
			 BufferedImage newImg = ImageIO.read(new ByteArrayInputStream(byteArray));
//			BufferedImage img = ImageIO.read(file);
//			BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//			Graphics2D graphics2d = newImg.createGraphics();
//			graphics2d.setComposite(AlphaComposite.Src);
//			graphics2d.drawImage(img, 0, 0, null);
//			graphics2d.dispose();
			
			
			 ImageIcon icon=new ImageIcon(newImg);
			    JFrame frame=new JFrame();
			    frame.setLayout(new FlowLayout());        
			    frame.setSize(newImg.getWidth(null)+50, newImg.getHeight(null)+50);     
			    JLabel lbl=new JLabel();
			    lbl.setIcon(icon);
			    frame.add(lbl);
			    frame.setVisible(true);
			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
