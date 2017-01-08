/**
 * 
 */
package com.spotjoe.NN;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.opencv.core.Core;

import com.spotjoe.ImageApplication;



/**
 * @author yun
 *
 */
public class TrainNNWindow extends Frame {
	MenuBar mainMenuBar = new MenuBar();
	Menu mFile = new Menu();
	MenuItem miPositive = new MenuItem();
	MenuItem miNegative = new MenuItem();
	MenuItem miSave = new MenuItem();
	MenuItem miTrain = new MenuItem();
	MenuItem miPredict = new MenuItem();
	JFileChooser fileChooser = new JFileChooser();
	JFileChooser saveFileDialog = new JFileChooser();
	JFileChooser openFileChooser = new JFileChooser();

	String positivePath = "";
	String negativePath = "";
	String savePath = "";
	
	//nn
	TrainNN nn;
	
	public static void main(String[] args){
		(new TrainNNWindow()).setVisible(true);
	}

	public TrainNNWindow(){
		setLayout(null);
		setSize(600,400);
		setVisible(false);
		setTitle("NN Trainer");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		saveFileDialog.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
		openFileChooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
		miPositive.setLabel("Select Samples");
		miNegative.setLabel("Negative Samples");
	        miSave.setLabel("Save");
		miTrain.setLabel("Train");	
		miPredict.setLabel("Predict");
		mFile.setLabel("File");
		mFile.add(miPositive);
		mFile.add(miTrain);
		mFile.add(miSave);
		mFile.add(miPredict);
//		mFile.add(miNegative);
		miSave.setEnabled(false);
		miTrain.setEnabled(false);
		miPredict.setEnabled(false);
		
		mainMenuBar.add(mFile);
		setMenuBar(mainMenuBar);
		
		
		
		//Listeners
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		miPositive.addActionListener(lSymAction);
		miNegative.addActionListener(lSymAction);
		miTrain.addActionListener(lSymAction);
		miSave.addActionListener(lSymAction);
		miPredict.addActionListener(lSymAction);
		
		
		//Activate OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		

	
	}
	
	//System listener
	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == TrainNNWindow.this){
				((TrainNNWindow)object).dispose();
			}
		}
	}
	
	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == miPositive){
				miPositive_action(event);
			}
			else if(object == miTrain){
			    miTrain_action(event);
			}
			else if(object == miSave){
                            miSave_action(event);
                        }
			else if(object == miPredict){
			    miPredict_action(event);
			}
//			else if (object == miNegative){
//				miNegative_action(event);
//			}

		}
	}
	
	//v====v=====v=====Actions=====v=====v=====v=
	void miPositive_action(ActionEvent event){
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			positivePath = fileChooser.getSelectedFile().getPath();
		}
		miTrain.setEnabled(true);
	}
	
	void miNegative_action(ActionEvent event){
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			negativePath = fileChooser.getSelectedFile().getPath();
		}
	}
	
	void miSave_action(ActionEvent event){
	    int result = saveFileDialog.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION){
                    savePath = saveFileDialog.getSelectedFile().getPath();
                    nn.save(savePath);
            }
	}
	
	void miTrain_action(ActionEvent event){
	    nn = new TrainNN(positivePath);
	    nn.train();
	    miPredict.setEnabled(true);
	    miSave.setEnabled(true);
	}
	
	void miPredict_action(ActionEvent event){
	    int result = openFileChooser.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION){
                    String path = openFileChooser.getSelectedFile().getPath();
                    String output = nn.predict(path);
                    JOptionPane.showMessageDialog(this,"Result:"+output);
                    
            }
	    
	}
}


