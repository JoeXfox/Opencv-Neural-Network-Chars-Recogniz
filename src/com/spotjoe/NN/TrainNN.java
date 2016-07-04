package com.spotjoe.NN;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvANN_MLP;
import org.opencv.ml.CvANN_MLP_TrainParams;
import org.opencv.ml.Ml;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;

public class TrainNN {
    String positivePath = "";
    String negativePath = "";
    String samplesPath = "";

    // NN
    int imgHight = 20;
    int imgWidth = 20;
    
    int outputSize;
    int inputSize;
    int hiddenSize;
    int sampleNum;

    Mat inputMat;
    Mat outputMat;
    Mat predicInput;
    Mat predictOutput;
    
    CvANN_MLP nn;

    ArrayList<SampleImg> sampleList = new ArrayList<SampleImg>();
    ArrayList<SampleMat> sampleMatList = new ArrayList<SampleMat>();
    HashMap<String, Integer> yStringMap = new HashMap<String, Integer>();
    // ---------deprecated at 2016/7/1------------
    public TrainNN(String positive, String negative) {
        positivePath = positive;
        negativePath = negative;
    }

    public TrainNN(String samplesPath) {
        this.samplesPath = samplesPath;
        init();
        loadSamplesMat(samplesPath);
        create();
    }

    void init() {
        //should load in config.ini
        outputSize = 10;
        inputSize = 400;
        hiddenSize = 25;
        sampleNum = 0;
    }

    void loadSamplesMat(String path) {
        File samplesFolder = new File(path);
        if (samplesFolder.exists()) {
            File[] folders = samplesFolder.listFiles();
            // check folder is completed
            if (folders.length != (outputSize)) {
                outputSize = folders.length;
                System.out.println("Alert: The folder may not match the output node size!");
            }

            // read folders
            for (File one : folders) {
                if (one.isDirectory()) {
                    String yString = one.getName();
                    File[] samples = one.listFiles();
                    int y = getY(yString);//output

                    // read sample images
                    for (File oneSample : samples) {
                        Mat img = Highgui.imread(oneSample.getPath());
                        // check img size with input size
                        if (img != null) {
//                            if (inputSize != (img.rows() * img.cols())) {
//                                inputSize = img.rows() * img.cols();
//                                System.out.println("Alert: The img size may not match the input node size!");
//                                System.out.println("floder is " + one.getPath());
//                            }
                            Mat formatImg = new Mat(imgHight,imgWidth,CvType.CV_32FC1);
                            Imgproc.resize(img, formatImg, formatImg.size());
                            Imgproc.cvtColor(formatImg, formatImg, Imgproc.COLOR_RGB2GRAY);
                            sampleMatList.add(new SampleMat(formatImg, y));
                        }
                    }

                }
            }//====after load mat to sample mat list
            
            //save y mapping
            saveOutputMapping();
            
            if(sampleMatList.size() > 0){
                //format mat
                if(sampleMatList.get(0).x.type() != CvType.CV_32FC1){
                    for (SampleMat one : sampleMatList) {
                        one.x.convertTo(one.x, CvType.CV_32FC1);
                    }
                }
                
                
                //load input & output mat
                sampleNum = sampleMatList.size();
                inputMat = new Mat(sampleNum, inputSize, CvType.CV_32FC1);
                outputMat = Mat.zeros(sampleNum, outputSize, CvType.CV_32FC1);
                
                for (int i = 0; i < sampleNum; i++) {
                    
                    Mat matX = sampleMatList.get(i).x;
                    matX = matX.reshape(0, 1);  //convert to vector
                    matX.copyTo(inputMat.row(i)); 
                    // Mat matY = Mat.zeros(1, outputSize, CvType.CV_32FC1);
                    // matY.put(0, sampleList.get(i).y, 1);
                    // - matY.copyTo(outputMat.row(i));

                    if (sampleMatList.get(i).y != -1) {
                        outputMat.put(i, sampleMatList.get(i).y, 1);
                    }
                }
            }           
        }
    }
    
    
    // ---------deprecated at 2016/7/2------------
    void loadSamples(String path) {
        File samplesFolder = new File(path);
        if (samplesFolder.exists()) {
            File[] folders = samplesFolder.listFiles();
            // check folder is completed
            if (folders.length != (outputSize + 1)) {
                System.out
                        .println("Alert: The folder may not match the output node size!");
            }

            // read folders
            for (File one : folders) {
                if (one.isDirectory()) {
                    String yString = one.getName();
                    File[] samples = one.listFiles();
                    int y = getY(yString);

                    // read sample images
                    for (File oneSample : samples) {
                        try {
                            BufferedImage img = ImageIO.read(oneSample);
                            // check img size with input size
                            if (inputSize != (img.getWidth() * img.getHeight())) {
                                inputSize = img.getWidth() * img.getHeight();
                                System.out
                                        .println("Alert: The img size may not match the input node size!");
                                System.out
                                        .println("floder is " + one.getPath());
                            }
                            sampleList.add(new SampleImg(img, y));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        // convert img to Mat
        sampleNum = sampleList.size();
        inputMat = new Mat(sampleNum, inputSize, CvType.CV_32FC1);
        // outputMat = new Mat(sampleNum, outputSize, CvType.CV_32FC1);
        outputMat = Mat.zeros(sampleNum, outputSize, CvType.CV_32FC1);
        for (int i = 0; i < sampleNum; i++) {
            BufferedImage img = sampleList.get(i).x;
            Mat matX = convertImg2Mat(img, img.getType(), CvType.CV_32FC1);
            matX = matX.reshape(0, 1);
            matX.copyTo(inputMat.row(i)); // may wrong
            // Mat matY = Mat.zeros(1, outputSize, CvType.CV_32FC1);
            // matY.put(0, sampleList.get(i).y, 1);
            // - matY.copyTo(outputMat.row(i));

            if (sampleList.get(i).y != -1) {
                outputMat.put(i, sampleList.get(i).y, 1);
            }

        }
    }

    Mat convertImg2Mat(BufferedImage img, int imgType, int matType) {
        if (img.getType() != imgType) {
            BufferedImage newImg = new BufferedImage(img.getWidth(),
                    img.getHeight(), imgType);
            // WritableRaster raster = img.getRaster();
            // newImg.setData(raster);
            Graphics2D graphics2d = newImg.createGraphics();
            graphics2d.setComposite(AlphaComposite.Src);
            graphics2d.drawImage(img, 0, 0, null);
            graphics2d.dispose();
        }

        byte[] pixels = ((DataBufferByte) img.getData().getDataBuffer())
                .getData();
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_32FC1);
        mat.put(0, 0, pixels);
        return mat;
    }

    // mapping yString to y index
    // e.g: "Stop Sign" : 0
    // "Yield" : 1
    // "Negative" : -1
    // return output index
    // if y is 1, vector out be [0,1,0,0,0]
    int getY(String folderName) {
//        return Integer.parseInt(folderName);// temp, it should mapping with some
//                                            // config.ini
        Integer y = yStringMap.get(folderName);
        if(y == null){
            y = yStringMap.size();
           yStringMap.put(folderName, yStringMap.size());
        }
        return y;
    }
    
    void saveOutputMapping(){
        BufferedWriter fWriter;
        try {
            
            fWriter = new BufferedWriter(new FileWriter("./config.ini"));
            for (Entry<String, Integer> one : yStringMap.entrySet()) {
                fWriter.write(String.format("%s:%d\r\n",one.getKey(),one.getValue()));
            }
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    void create(){
        nn = new CvANN_MLP();
        Mat layersize = new Mat(1, 3, CvType.CV_32SC1);
        layersize.put(0, 0, inputSize);
        layersize.put(0, 1, hiddenSize);
        layersize.put(0, 2, outputSize);
        System.out.println("layer size:\n"+layersize.dump());
        nn.create(layersize, CvANN_MLP.SIGMOID_SYM, 0.6, 1);
    }
    
    void train() {

        
        CvANN_MLP_TrainParams params = new CvANN_MLP_TrainParams();
 
        params.set_term_crit(new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,  5000, 0.00001));
        //BP
        params.set_bp_dw_scale(0.1);
        params.set_bp_moment_scale(0.1);
        params.set_train_method(CvANN_MLP_TrainParams.BACKPROP);
       
        //RPROP
//        params.set_rp_dw0(0.1);
//        params.set_rp_dw_plus(1.2);
//        params.set_rp_dw_minus(0.5);
//        params.set_rp_dw_max(50);
//        params.set_train_method(CvANN_MLP_TrainParams.RPROP);
        int times = nn.train(inputMat, outputMat,new Mat(),new Mat(), params, 0);
        System.out.println("Finish train: " + times);
        
    }
    
    
    
    void save(String path){
        nn.save(path);
    }
    
    int predict(String path){
        predictOutput = Mat.zeros(1, outputSize, CvType.CV_32FC1);
        Mat img = Highgui.imread(path);
        if(img != null){
            //format size 
            Mat formatImg = new Mat(imgHight,imgWidth,CvType.CV_32FC1);
            Imgproc.resize(img, formatImg, formatImg.size());
            img = formatImg;
            
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY); // gradscale
            img.convertTo(img, CvType.CV_32FC1);
            img = img.reshape(0, 1);
            nn.predict(img, predictOutput);
            
            
            int outputIndex = 0;
            double[] max = predictOutput.get(0, 0);
            for(int i = 0; i < predictOutput.cols(); i++){
               if(predictOutput.get(0, i)[0] > max[0]){
                   outputIndex = i;
                   max = predictOutput.get(0, i);
               }
            }
            if(max[0] < 0.5){
                return -1;
            }
            return outputIndex;
        }
        return -2;
    }
}

class SampleImg {
    BufferedImage x;
    int y;

    public SampleImg(BufferedImage x, int y) {
        this.x = x;
        this.y = y;
    }
}

class SampleMat {
    Mat x;
    int y;

    public SampleMat(Mat x, int y) {
        this.x = x;
        this.y = y;
    }
}
