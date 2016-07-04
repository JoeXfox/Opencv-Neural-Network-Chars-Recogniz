package com.spotjoe;
import java.awt.*;
import java.io.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.awt.image.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;


/**

Class ImageData is a base class which
respresents image data and the methods for
producing the corresponding wavelet image,
as well as methods to access both of these
datas. </p>

@author L. Grewe
@student Xinyun Zheng	
@version 13 April. 2016
*/

//Note: extends Component to inherit its createImage() method
class ImageData extends Component
{    boolean verbose = false;

     //File where data stored and format
     String filename = ""; 
     String format   = "";

 
     // Num Rows, columns
     public int rows=0, cols=0;
     public int mWidth = 0;
     public int mHeight = 0;
     

     //image data
     public int data[];    //TIP: MAYBE CHANGING THIS TO USE java.awt.Image WOULD MAKE YOUR CODING EASIER
     public int[] edgeData;
     public int[] M;
     public int[] N;
     public double[] P;
     public double[] Q;
     public double[] Theta;
     public double[] filteredData;
     public int[] sobelData;
     public double sigma = 0;
     public int[] mHist;   
	 public int mEdgeNum;             
	 public int mMaxG;         
	 public int mHighCount; 
     
	 public double  dRatHigh;  
	 public double  dThrHigh;  
	 public double  dThrLow;  
	 public double  dRatLow;  
	 public double graphThreshold;

     
     public float minDataRange = Float.MAX_VALUE;
     public float maxDataRange = Float.MIN_VALUE;
     
//     public double contrast = 1;

     private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
     private static final ColorModel RGB_OPAQUE =
    		    new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);



    //**METHODS: for image data*/
     int getData(int row, int col)
      { if (row < rows && col <cols )
            return data[(row*cols)+col];
        else
            return 0;
      }

     int[] getData(){
    	 return data.clone();
     }
      int getDataForDisplay(int row, int col)
      {   if (row < rows && col <cols )
            return data[(row*cols)+col];
        else
            return 0;
      }


      void setData(int row, int col, int value)
      {  data[(row*cols)+col] = (int) value;

      }

      void setData(int[] data)
      {  
    	  this.data = data;

      }


     

  /**
   * Constructs a ImageData object using the
   * specified by an instance of java.awt.Image,
   * format, and size indicated by numberRows and
   * numberColumns.
   * @param img an Image object containing the data.
   * @param DataFormat the format of the data
   * @param numberRows the number of rows of data
   * @param numberColumns the number of columns of data
   * @exception IOException if there is an error during
   *  reading of the rangeDataFile.
   */
   public ImageData(Image img, String DataFormat,String filePath,
                    int numberRows, int numberColumns) throws IOException
     {
      int pixel, red, green, blue, r,c;
      format = DataFormat;
      rows = numberRows;
      cols = numberColumns;
      filename = filePath;
      PixelGrabber pg;

      //From the image passed retrieve the pixels by
      //creating a pixelgrabber and dump pixels
      //into the data[] array.
      data = new int[rows*cols];
      pg = new PixelGrabber(img, 0, 0, cols, rows, data, 0, cols);   //SPECIAL NOTE: you could change so stores in java.awt.Image instead
      try {
          pg.grabPixels();   //this actually gets the pixel data and puts it into the array.
      } catch (InterruptedException e) {
          System.err.println("interrupted waiting for pixels!");
          return;
      }


      //Convert the PixelGrabber pixels to greyscale
      // from the {Alpha, Red, Green, Blue} format 
      // PixelGrabber uses.
      for(r=0; r<rows; r++)
      for(c=0; c<cols; c++)
        {   pixel = data[r*cols + c];
	        red   = (pixel >> 16) & 0xff;
            green = (pixel >>  8) & 0xff;
            blue  = (pixel      ) & 0xff;
            if(verbose)
                System.out.println("RGB: " + red + "," + green +"," +blue);
            data[r*cols+c] = (int)((red+green+blue)/3);  //SPECIAL NOTE: This sample code converts RGB image to a greyscale one
			                                              
            if(verbose)
                System.out.println("Pixel: " + (int)((red+green+blue)/3));
            minDataRange = Math.min(minDataRange, data[r*cols+c]);
            maxDataRange = Math.max(maxDataRange, data[r*cols+c]);
        }                
	        
            
            
     
		//{{INIT_CONTROLS
		setBackground(java.awt.Color.white);
		setSize(0,0);
		//}}
	}



  /**
   * Constructs a ImageData object using the
   * specified  size indicated by
   * numberRows and numberColumns that is EMPTY.
   * @param numberRows the number of rows of data
   * @param numberColumns the number of columns of data
   */
   public ImageData(int numberRows, int numberColumns){

      rows = numberRows;
      cols = numberColumns;
      
     

   }
   
   
   
   /**
   * Constructs a ImageData object using the
   * specified  size indicated by
   * numberRows and numberColumns.  Fill the data[]
   * array with the information stored in
   * the ImageData instance ID, from the 2D
   * neighborhood starting at the upper-left coordinate
   * (rStart,cStart) 
   * @param numberRows the number of rows of data
   * @param numberColumns the number of columns of data
   * @param ID image data to copy data from
   * @param rStart,cStart  Start of Neighborhood copy
   */
   public ImageData(int numberRows, int numberColumns, ImageData ID,
                    int rStart,int cStart){


      //saftey check: Retrieval in ID outside of boundaries
      if(ID.rows<(rStart+numberRows) || ID.cols<(cStart+numberColumns))
      {  rows = 0;
         cols = 0;
         return;
      }   
      
      
      rows = numberRows;
      cols = numberColumns;
      
      //create data[] array.
      data = new int[rows*cols];
      
      //Copy data from ID.
      for(int i=0; i<rows; i++)
      for(int j=0; j<cols; j++)
        {   data[i*cols+j] = ID.data[(rStart+i)*ID.cols + j + cStart];
            minDataRange = Math.min(minDataRange, data[i*cols+j]);
            maxDataRange = Math.max(maxDataRange, data[i*cols+j]);
        }    
      
      
   }   



//METHODS
 

 
   


  /**
   * creates a java.awt.Image from the pixels stored 
   * in the array data using 
   * java.awt.image.MemoryImageSource
   */
  public Image createImage(int[] data)
   {
        int pixels[], t;
        pixels = new int[rows*cols];
    
        //translate the data in data[] to format needed
        for(int r=0;r<rows; r++)
        for(int c=0;c<cols; c++)
        {  t = data[r*cols + c];
           if(t == 999) //due to reg. transformation boundaries produced
            { t = 0; }  // see Transform.ApplyToImage() method
           if(t<0) //due to processing
            { t = -t; }
           else if(t>255) //due to processing
            { t = 255; }
           
           pixels[r*cols+c] = (255 << 24) | (t << 16) | (t << 8) | t;
//           pixels[r*cols+c] =  (t << 16) | (t << 8) | t;
           //note data is greyscale so red=green=blue above (alpha first)
        }
    
        //Now create Image using new MemoryImageSource
//        return ( super.createImage(new MemoryImageSource(cols, rows, pixels, 0, cols)));
        
//	  byte[] byteData = new byte[rows * cols];
//	  for (int i = 0; i < data.length; i++) {
//		byteData[i] = (byte)data[i];
//	}
//        try {
//			BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteData));
//			return img;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
        
        
//        DataBuffer buffer = new DataBufferInt((int[]) data, cols*rows);
//        WritableRaster raster = Raster.createPackedRaster(buffer, cols, rows, cols, RGB_MASKS, null);
//        BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);
//        return bi;
        
        
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_3BYTE_BGR);
        img.setRGB(0, 0, cols, rows, pixels, 0, cols);
        return img;
	
   } 
   
   
  public Image createImage(){
	  return createImage(data);
  }
   
 
   
   
   /**
	 *Stores the data image to a 
	 * a file as COLOR raw image data format
	 */
	public void storeImage(String filename)throws IOException
	{ 
	   
	    int  pixel, alpha, red, green,blue;
	    
	    
	        
        //Open up file	
        FileOutputStream file_output = new FileOutputStream(filename);
        DataOutputStream DO = new DataOutputStream(file_output);
 
 
        //Write out each pixel as integers
        
	
         
        for(int r=0; r<rows; r++)
	    for(int c=0; c<cols; c++) {
            pixel = data[r*cols + c];
	        red = pixel;
            green = pixel;
            blue = pixel;
            if(verbose)//verbose
    	        {System.out.println("value: " + (int)((red+green+blue)/3));
    	         System.out.println(" R,G,B: " + red +"," + green +"," + blue); }
	   
 	        DO.writeByte(red);
 	        DO.writeByte(green);
 	        DO.writeByte(blue);
        }	

        //flush Stream
        DO.flush();
        //close Stream
        DO.close();

    }
   
   
	public void brighten(int x){
		for(int i = 0; i < rows; ++i){
			for(int j = 0; j < cols; ++j){
				int tmp = getData(i, j);
				tmp = (tmp + x) > 255 ? 255 : (tmp + x);
				setData(i, j, tmp);
			}
		}
	}
     
	
	public boolean saveToFile() {
		try {
			//storeImage(filename);
			File outputfile = new File(filename);
			BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster wr = image.getRaster();
			wr.setPixels(0, 0,cols,rows, data);
			ImageIO.write(image, format, outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	//threshold for grayscale
	public void threshold(int t){
		
		//boundary 
		if(t > 255){
			t = 255;
		}else if(t < 0){
			t = 0;
		}
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				if(data[i*cols + j] > t){
					data[i*cols + j] = 255;
				}
				else{
					data[i*cols + j] = 0;
				}
			}
		}
		
	}
	
	public void negate(){
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
					data[i*cols + j] = 255 - data[i*cols + j];
			}
		}
	}
	
	public void contrast(double y, int c){
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
					data[i * cols + j] =(int)(c * Math.pow(data[i*cols + j] / 255.0, y));
			}
		}
	}
	
	public void contrast(double y){
		contrast(y,255);
	}
	
	
	public void edge(){
		gaussFilter();
		gradientAndDirectPrewitt();
		suppressNotMax();
		verifyDoubleThreshold();
		edgeDetect();
	}
	public void gaussFilter(){
		
		short[] sGauss = new short[rows*cols];
		filteredData = new double[rows*cols];
		Mat tmp = new Mat(rows,cols,CvType.CV_32S);
		tmp.put(0, 0, data);
		tmp.convertTo(tmp, CvType.CV_16S);
		Mat dst = new Mat(rows,cols,CvType.CV_16S);
		Size gaussWindowSize = new Size(1+2*Math.ceil(3*sigma),1+2*Math.ceil(3*sigma));
		
		Imgproc.GaussianBlur(tmp, dst, gaussWindowSize, sigma);

		dst.get(0, 0, sGauss);
		for(int i = 0; i < sGauss.length; ++i){
			filteredData[i] = sGauss[i];
		}
	}
	
	public void gradientAndDirectPrewitt(){
		mWidth = cols;
		mHeight = rows;
		P = new double[mWidth*mHeight];         
		Q = new double[mWidth*mHeight];        
		M = new int[mWidth*mHeight];				
		Theta = new double[mWidth*mHeight];  
		
		
		for (int i = 0; i < (mHeight - 1); i++)
		{
			for(int j = 0; j < (mWidth - 1); j++)
			{
				
				//P[i * mWidth + j] = (double)(filteredData[i * mWidth+ j] - filteredData[min((i + 1), mHeight - 1) * mWidth+ min(j + 1, mWidth - 1)]);
				P[i * mWidth + j] =(double)(filteredData[Math.max(i - 1, 0) * mWidth + Math.min(j+1, mWidth - 1)] 
												+ 2 * filteredData[i * mWidth +  Math.min(j + 1, mWidth - 1)] 
												+ filteredData[Math.min(i + 1, mHeight - 1) * mWidth  +Math.min(j + 1, mWidth - 1)] 
												- filteredData[Math.max(i - 1, 0) * mWidth + Math.max(j - 1, 0)] 
												- 2 * filteredData[i * mWidth+Math.max(j - 1, 0)] 
												- filteredData[Math.min(i + 1, mHeight - 1) * mWidth +Math.max(j - 1, 0)]);
				//Q[i * mWidth + j] = (double)( filteredData[Math.min((i + 1), mHeight - 1) * mWidth + j] - filteredData[i  * mWidth + Math.min(j + 1, mWidth-1)]);
				Q[i * mWidth + j] = (double)(filteredData[Math.max(i - 1, 0) * mWidth +Math.max(j - 1, 0)]
												+ 2 * filteredData[Math.max(i - 1, 0) * mWidth + j]
												+ filteredData[Math.max(i - 1, 0) * mWidth +Math.min(j+1, mWidth - 1)]
												- filteredData[Math.min(i + 1, mHeight - 1) * mWidth + Math.max(j - 1, 0)]
												- 2* filteredData[Math.min(i + 1, mHeight - 1) * mWidth + j]
												- filteredData[Math.min(i + 1, mHeight - 1) * mWidth + Math.min(j+1, mWidth - 1)]);
			}
		}


		//gradient direction
		for (int i = 0; i < mHeight; i ++)
		{
			for (int j = 0; j < mWidth; j++)
			{
				M[i*mWidth+j] = (int)(Math.sqrt(P[i*mWidth+j]*P[i*mWidth+j] + Q[i*mWidth+j]*Q[i*mWidth+j])+0.5);  
				Theta[i*mWidth+j] = Math.atan2(Q[i*mWidth+j], P[i*mWidth+j]) * 57.3;  
				if(Theta[i*mWidth+j] < 0)  
				{
					Theta[i*mWidth+j] += 360;         
				} 
			}
		}
		sobelData = M.clone();
	}
	
	public void suppressNotMax(){
		N = new int[mWidth*mHeight];        //result of suppress not Max
		int g1=0, g2=0, g3=0, g4=0;                            //for insert value
		double dTmp1=0.0, dTmp2=0.0;                       //insert values
		double dWeight=0.0;                                        //weight of insert value 


		for(int i=0; i<mWidth; i++)  
		{  
			N[i] = 0;  
			N[(mHeight-1)*mWidth+i] = 0;  
		}  
		for(int j=0; j<mHeight; j++)  
		{  
			N[j*mWidth] = 0;  
			N[j*mWidth+(mWidth-1)] = 0;  
		}  


		for(int i=1; i<(mWidth-1); i++)  
		{  
			for(int j=1; j<(mHeight-1); j++)  
			{  
				int nPointIdx = i+j*mWidth;       
				if(M[nPointIdx] == 0)  
					N[nPointIdx] = 0;        
				else  
				{  
					if( ((Theta[nPointIdx]>=90)&&(Theta[nPointIdx]<135)) ||   
						((Theta[nPointIdx]>=270)&&(Theta[nPointIdx]<315)))  
					{  
						
						g1 = M[nPointIdx-mWidth-1];  
						g2 = M[nPointIdx-mWidth];  
						g3 = M[nPointIdx+mWidth];  
						g4 = M[nPointIdx+mWidth+1];  				
						dWeight = Math.abs(P[nPointIdx])/Math.abs(Q[nPointIdx]);   
						dTmp1 = g1*dWeight+g2*(1-dWeight);  
						dTmp2 = g4*dWeight+g3*(1-dWeight);  
					}  

					else if( ((Theta[nPointIdx]>=135)&&(Theta[nPointIdx]<180)) ||   
						((Theta[nPointIdx]>=315)&&(Theta[nPointIdx]<360)))  
					{  
						g1 = M[nPointIdx-mWidth-1];  
						g2 = M[nPointIdx-1];  
						g3 = M[nPointIdx+1];  
						g4 = M[nPointIdx+mWidth+1];  
						dWeight = Math.abs(Q[nPointIdx])/Math.abs(P[nPointIdx]);     
						dTmp1 = g2*dWeight+g1*(1-dWeight);  
						dTmp2 = g4*dWeight+g3*(1-dWeight);  
					}  

					else if( ((Theta[nPointIdx]>=45)&&(Theta[nPointIdx]<90)) ||   
						((Theta[nPointIdx]>=225)&&(Theta[nPointIdx]<270)))  
					{  
						g1 = M[nPointIdx-mWidth];  
						g2 = M[nPointIdx-mWidth+1];  
						g3 = M[nPointIdx+mWidth];  
						g4 = M[nPointIdx+mWidth-1];  
						dWeight = Math.abs(P[nPointIdx])/Math.abs(Q[nPointIdx]);  
						dTmp1 = g2*dWeight+g1*(1-dWeight);  
						dTmp2 = g3*dWeight+g4*(1-dWeight);  
					}  

					else if( ((Theta[nPointIdx]>=0)&&(Theta[nPointIdx]<45)) ||   
						((Theta[nPointIdx]>=180)&&(Theta[nPointIdx]<225)))  
					{  
						g1 = M[nPointIdx-mWidth+1];  
						g2 = M[nPointIdx+1];  
						g3 = M[nPointIdx+mWidth-1];  
						g4 = M[nPointIdx-1];  
						dWeight = Math.abs(Q[nPointIdx])/Math.abs(P[nPointIdx]);   //ÕýÇÐ  
						dTmp1 = g1*dWeight+g2*(1-dWeight);  
						dTmp2 = g3*dWeight+g4*(1-dWeight);  
					}  
				}         
				//=============Max value===========
				if((M[nPointIdx]>=dTmp1) && (M[nPointIdx]>=dTmp2))  
					N[nPointIdx] = 128;  
				else  
					N[nPointIdx] = 0;  
			}  
		}  //==========End of insert value===========//
		
//		data = N.clone();
	}
	
	public void verifyDoubleThreshold(){
		//init
		mHist = new int[2048];
		mMaxG = 0;
		
		for(int i=0; i<mHeight; i++)  
		{  
			for(int j=0; j<mWidth; j++)  
			{  
				if(N[i*mWidth+j]==128)  
					mHist[M[i*mWidth+j]]++;  
			}  
		}  
		
		mEdgeNum = mHist[0]; 
		
		for(int i=1; i<mHist.length; i++)          
		{  
			if(mHist[i] != 0)     
			{  
				mMaxG = i;  
			}     
			mEdgeNum += mHist[i];   
		}  


		dRatHigh = 0.79;  
		dRatLow = 0.5;  
		mHighCount = (int)(dRatHigh * mEdgeNum + 0.5);  
		int j=1;  
		mEdgeNum = mHist[1];  
		while((j<(mMaxG-1)) && (mEdgeNum < mHighCount))  
		{  
			j++;  
			mEdgeNum += mHist[j];  
		}  
		dThrHigh = j;                                  
		dThrLow = (int)((dThrHigh) * dRatLow + 0.5);    
	}
	
	public void edgeDetect(){
		Size sz = new Size(mWidth,mHeight);   
		for(int i=0; i<mHeight; i++)  
		{  
			for(int j=0; j<mWidth; j++)  
			{  
				if((N[i*mWidth+j]==128) && (M[i*mWidth+j] >= dThrHigh))  
				{  
					N[i*mWidth+j] = 255;  
					TraceEdge(i, j, dThrLow, N, M, sz);  
				}  
			}  
		}  


		  
		for(int i=0; i<mHeight; i++)  
		{  
			for(int j=0; j<mWidth; j++)  
			{  
				if(N[i*mWidth+j] != 255)  
				{  
					N[i*mWidth+j]  = 0 ;   
				}  
			}  
		}  
		
		edgeData = N.clone();
	}
	
	
	void TraceEdge(int y, int x, double nThrLow, int[]n, int[] m, Size sz)  
	{  
		//check surround
		int[] xNum = {1,1,0,-1,-1,-1,0,1};  
		int[] yNum = {0,1,1,1,0,-1,-1,-1};  
		int yy,xx,k;  
		for(k=0;k<8;k++)  
		{  
			yy = y+yNum[k];  
			xx = x+xNum[k];  
			int index = (int) (yy*sz.width+xx);
			if(n[index]==128 && m[index]>=nThrLow )  
			{  
				
				n[index] = 255;  
				
				TraceEdge(yy,xx,nThrLow,n,m,sz);  
			}  
		}  
	}  
	
	public Image getSobelImage(){
		return createImage(sobelData);
	}
	
	public Image getEdgeImage(){
		return createImage(edgeData);
	}
	public void edgeByLib(){
//		byte[] dataDouble = new byte[cols * rows];
//		for(int i = 0; i < dataDouble.length; ++i){
//			dataDouble[i] = (byte)data[i];
//		}
		Mat mat = new Mat(rows, cols, CvType.CV_32S);// CVTYPE is important
		mat.put(0, 0, data);
		int scale = 1;
		int delta = 0;
		int gamma = 0;
		byte sdata[] = new byte[cols*rows];
//		ArrayList<Integer> tmp = new ArrayList<>();
//		for (int i : data) {
//			tmp.add(i);
//		}
		
		
//		mat = Converters.vector_int_to_Mat(tmp);
		mat.convertTo(mat, CvType.CV_16S);
		Mat dstMat = new Mat(rows,cols,CvType.CV_16S);
		Mat dstMat_x = new Mat(rows,cols,mat.type());
		Mat dstMat_y = new Mat(rows,cols,mat.type());
		
//		Imgproc.Sobel(mat, dstMat, -1, 1, 1, 3, scale, delta);
		Imgproc.Sobel(mat, dstMat_x, -1, 1, 0);
		Imgproc.Sobel(mat, dstMat_y, -1, 0, 1);
		
	
		Core.convertScaleAbs(dstMat_x, dstMat_x);
		Core.convertScaleAbs(dstMat_y, dstMat_y);
		Core.addWeighted(dstMat_x, 0.5, dstMat_y, 0.5, gamma, dstMat);
		System.out.println(dstMat.type());
		dstMat.get(0, 0, sdata);
		for(int i = 0; i < sdata.length; ++i){
			data[i] = sdata[i];  
		}
//		System.out.println("mat = " + mat.dump());
//		MatOfDouble matd = new MatOfDouble();
//		Mat mat = new Mat();
//		matd.fromArray(dataDouble);
//		Highgui.imwrite("D:/test.jpg", mat);
	}
	//{{DECLARE_CONTROLS
	//}}
}//End ImageData
