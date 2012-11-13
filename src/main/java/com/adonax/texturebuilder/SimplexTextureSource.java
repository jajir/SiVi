/*
 *  This file is part of SiVi.
 *
 *  SiVi is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published
 *  by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  SiVi is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public 
 *  License along with SiVi.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package com.adonax.texturebuilder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import com.adonax.utils.SimplexNoise;

public class SimplexTextureSource extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private BufferedImage image;
	float[][] noiseArray;
	
	private int width, height;
	private int cols, rows;
//	private int[] pixel;

	JLabel xScaleLbl, yScaleLbl;
	JLabel xTranslateLbl, yTranslateLbl;
	JLabel minLbl, maxLbl;
	
	float xScale;
	float yScale;
	float xTranslate;
	float yTranslate;
	float minClamp;
	float maxClamp;
	
	JCheckBox scaleLock;
	boolean scaleLocked;
	float scaleRatio;
	final JSlider xScaleSlider, yScaleSlider;
	final JSlider xTranslateSlider, yTranslateSlider;
	final JSlider minClampSlider, maxClampSlider;
	final float PRECISION = 8;
	
	JTextField xScaleVal;
	JTextField yScaleVal;
	JTextField xTranslationVal;
	JTextField yTranslationVal;
	JTextField minVal;
	JTextField maxVal;
	
	// I/O, allows external source to set values
	// assumption: the ActionListener for each will update
	// the model.
	public void setXScaleVal(float val)
	{
		xScale = val;
		xScaleVal.setText(String.valueOf(val));
		xScaleSlider.setValue((int)Math.round(val)*8);
	}
	public void setYScaleVal(float val)
	{
		yScale = val;
		yScaleVal.setText(String.valueOf(val));
		yScaleSlider.setValue((int)Math.round(val)*8);
	}
	public void setXTranslationVal(float val)
	{
		xTranslate = val;
		xTranslationVal.setText(String.valueOf(val));
		xTranslateSlider.setValue((int)Math.round(val)*4);
	}
	public void setYTranslationVal(float val)
	{
		yTranslate = val;
		yTranslationVal.setText(String.valueOf(val));
		yTranslateSlider.setValue((int)Math.round(val)*4);
	}
	public void setMinVal(float val)
	{
		minClamp = val;
		minVal.setText(String.valueOf(val));
		minClampSlider.setValue((int)(val * 1024));
	}
	public void setMaxVal(float val)
	{
		maxClamp = val;
		maxVal.setText(String.valueOf(val));
		maxClampSlider.setValue((int)(val * 1024));
	}
	
	
	JTextField functionText;
	
	ButtonGroup mappingOptions;
	JRadioButton noMap, absMap, compress01Map;
//	JCheckBox useColorMapCheckBox;
//	boolean useColorMapSelected;
	
	public void setMap(int i)
	{
		switch (i)
		{
		case 0:
			compress01Map.setSelected(true);
			break;
		case 1:
			absMap.setSelected(true);
			break;
		case 2:
			noMap.setSelected(true);
		}
	}
	
	
	ColorAxis colorAxis;
	public void setColorAxis(ColorAxis colorAxis)
	{
		this.colorAxis = colorAxis;
	}
	
	
	final STBPanel host;
	
	SimplexTextureSource(final int left, final int top, int width, int height, 
			ColorAxis colorAxis, final STBPanel host)
	{
		this.width = width;
		this.height = height;
		this.host = host;
		this.colorAxis = colorAxis;
		
		setLayout(null);
		
		final Rectangle colorBarArea = new Rectangle(0, 0, width, 24);
		
		addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if (colorBarArea.contains(e.getPoint()))
				{
					host.cbSelector.setBounds(left, top, 272, 
							STBPanel.BARS * 32 
							+ 34 );
					host.cbSelector.setCallback(getSelf());
					host.cbSelector.setVisible(true);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub	
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub	
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub	
			}});
		
		xScaleLbl = new JLabel("X Scale");
		yScaleLbl = new JLabel("Y Scale");
		xTranslateLbl = new JLabel("X Trans");
		yTranslateLbl = new JLabel("Y Trans");
		minLbl = new JLabel("Min");
		maxLbl = new JLabel("Max");
		
		xScaleLbl.setBounds(0, 32, 64, 24);
		yScaleLbl.setBounds(0, 64, 64, 24);
		xTranslateLbl.setBounds(0, 96, 64, 24);
		yTranslateLbl.setBounds(0, 128, 64, 24);
		minLbl.setBounds(0, 160, 64, 24);
		maxLbl.setBounds(0, 192, 64, 24);
		
		add(xScaleLbl);
		add(yScaleLbl);
		add(xTranslateLbl);
		add(yTranslateLbl);
		add(minLbl);
		add(maxLbl);
		
		scaleLock = new JCheckBox();
		scaleLock.setBounds(42, 52, 18, 16);
		scaleLock.setSelected(false);
		scaleLocked = false;
		add(scaleLock);
		scaleLock.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				scaleRatio = xScale / yScale;				
				scaleLocked = scaleLock.getModel().isSelected();
//				System.out.println("scaleRatio:" + scaleRatio
//						+ " locked:" + scaleLocked);
			}});
		
		xScaleSlider = new JSlider(1, 1024, 8);
		yScaleSlider = new JSlider(1, 1024, 8);
		xTranslateSlider = new JSlider(-1024, 1024, 0);
		yTranslateSlider = new JSlider(-1024, 1024, 0);
		minClampSlider = new JSlider(-1024, 1024, -1024);
		maxClampSlider = new JSlider(-1024, 1024, 1024);
		
		add(xScaleSlider);
		add(yScaleSlider);
		add(xTranslateSlider);
		add(yTranslateSlider);
		add(minClampSlider);
		add(maxClampSlider);
		
		xScaleSlider.setBounds(64, 32, 128, 24);
		yScaleSlider.setBounds(64, 64, 128, 24);
		xTranslateSlider.setBounds(64, 96, 128, 24);
		yTranslateSlider.setBounds(64, 128, 128, 24);		
		minClampSlider.setBounds(64, 160, 128, 24);
		maxClampSlider.setBounds(64, 192, 128, 24);
		
		xScaleSlider.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) 
			{
				xScale = xScaleSlider.getValue() / PRECISION;
				xScaleVal.setText(String.valueOf(xScale));
				if (scaleLocked)
				{
					scaleLocked = false;
					yScale = xScale / scaleRatio;
					yScaleVal.setText(String.valueOf(yScale));
					yScaleSlider.setValue((int)Math.round(yScale * PRECISION));
					scaleLocked = true;
				}
				update();
			}
		});
		
		yScaleSlider.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) 
			{
//				System.out.println("ySlider, locked?:" + scaleLocked);
				yScale = yScaleSlider.getValue() / PRECISION;
				yScaleVal.setText(String.valueOf(yScale));
				if (scaleLocked)
				{
					scaleLocked = false;
					xScale = yScale * scaleRatio;
					xScaleVal.setText(String.valueOf(xScale));
					xScaleSlider.setValue((int)Math.round(xScale * PRECISION));
					scaleLocked = true;
				}

				update();
			}
		});
		
		xTranslateSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) {
				xTranslate = xTranslateSlider.getValue() / 4f;
				xTranslationVal.setText(String.valueOf(xTranslate));
				update();
			}
		});

		yTranslateSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				yTranslate = yTranslateSlider.getValue() / 4f;
				yTranslationVal.setText(String.valueOf(yTranslate));
				update();
			}
		});

		minClampSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				minClamp = minClampSlider.getValue()/1024f;
				minVal.setText(String.valueOf(minClamp));
				update();
			}
		});

		maxClampSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				maxClamp = maxClampSlider.getValue()/1024f;
				maxVal.setText(String.valueOf(maxClamp));
				update();
			}
		});
		
		xScale = 1;
		xScaleVal = new JTextField(String.valueOf(xScale));
		yScale = 1;
		yScaleVal = new JTextField(String.valueOf(yScale));
		xTranslate = 0;
		xTranslationVal = new JTextField(String.valueOf(xTranslate));
		yTranslate = 0;
		yTranslationVal = new JTextField(String.valueOf(yTranslate));
		minClamp = -1;
		minVal = new JTextField(String.valueOf(minClamp));
		maxClamp = 1;
		maxVal = new JTextField(String.valueOf(maxClamp));
		add(xScaleVal);
		add(yScaleVal);
		add(xTranslationVal);
		add(yTranslationVal);
		add(minVal);
		add(maxVal);
		
		xScaleVal.setBounds(208, 32, 48, 24);
		yScaleVal.setBounds(208, 64, 48, 24);
		xTranslationVal.setBounds(208, 96, 48, 24);
		yTranslationVal.setBounds(208, 128, 48, 24);
		minVal.setBounds(208, 160, 48, 24);
		maxVal.setBounds(208, 192, 48, 24);
		
		xScaleVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				xScale = Float.valueOf(xScaleVal.getText());
				xScaleSlider.setValue((int)(xScale * PRECISION));
				update();
			}
		});
		
		yScaleVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
//				System.out.println("yScaleVal:" + yScaleVal.getText()
//						+ " yScale:" + yScale
//						+ " yScaleSlider:" + yScaleSlider.getValue()
//						+ " newScaleVal:" + (int)(yScale * PRECISION));
				yScale = Float.valueOf(yScaleVal.getText());
				yScaleSlider.setValue((int)(yScale * PRECISION));
				update();
			}
		});		
		
		xTranslationVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				xTranslate = Math.round(Double.valueOf(
						xTranslationVal.getText()) * 4);
				xTranslateSlider.setValue((int)xTranslate);
				update();
			}
		});
		
		yTranslationVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				yTranslate = Math.round(Double.valueOf(yTranslationVal.getText()) * 4);
				yTranslateSlider.setValue((int)yTranslate);
				update();
			}
		});

		minVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				minClamp = Math.round(Double.valueOf(minVal.getText()) * 1024.00);
				minClampSlider.setValue((int)minClamp);
				update();
			}
		});

		maxVal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				maxClamp = Math.round(Double.valueOf(maxVal.getText()) * 1024.00);
				maxClampSlider.setValue((int)maxClamp);
				update();
			}
		});
		
		mappingOptions = new ButtonGroup();
		noMap = new JRadioButton("none");
		absMap = new JRadioButton("|v|");
		compress01Map = new JRadioButton("(v+1)/2");
		compress01Map.setSelected(true); // default
		mappingOptions.add(noMap);
		mappingOptions.add(absMap);
		mappingOptions.add(compress01Map);
		compress01Map.setBounds(2, 224, 74, 24);
		absMap.setBounds(78, 224, 54, 24);
		noMap.setBounds(134, 224, 54, 24);

		add(noMap);
		add(absMap);
		add(compress01Map);

		absMap.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
		});
		
		compress01Map.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
		});
		
		noMap.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE: noMap data range undefined for ColorMap
				update();
			}
		});

		
		// image building variables, initializations
		cols = 256;
		rows = 256;
		
		image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
		noiseArray = new float[cols][rows];
		
	}
	
	private SimplexTextureSource getSelf() {return this;}
	
	public void update()
	{
		
		int mapChoice = 0;
		if (mappingOptions.getSelection().equals(compress01Map.getModel()))
		{
			mapChoice = 0;
		}
		if (mappingOptions.getSelection().equals(absMap.getModel()))
		{
			mapChoice = 1;
		}
		if (mappingOptions.getSelection().equals(noMap.getModel())) 
		{
			mapChoice = 2;
		}

		int pixel = 0;
		for (int j = 0; j < rows; j++)
		{
			double y = (j * yScale) / rows + yTranslate; 
			
			for (int i = 0; i < cols; i++)
			{
				float x = (i * xScale) / cols + xTranslate;  
				
				float noiseVal = (float)SimplexNoise.noise(x, y);
				noiseVal = Math.min(Math.max(noiseVal, minClamp),
						maxClamp);
				
				switch (mapChoice)
				{
					case 0:
						noiseVal = (noiseVal + 1) / 2;
						break;
					case 1:
						noiseVal = Math.abs(noiseVal);
						break;
					case 2:
						// use raw noiseVal
						break;
					case 3:
						// use ColorMap locally
				}
				int idx = (int)(noiseVal * 255);
				if (mapChoice == 0 || mapChoice ==1)
				{	
					int argb = colorAxis.data[idx];
					pixel = ColorAxis.calculateARGB(
							255, 
							ColorAxis.getRed(argb),
							ColorAxis.getGreen(argb), 
							ColorAxis.getBlue(argb));
				}
				else if (mapChoice == 2)
				{
					pixel = ColorAxis.calculateARGB(
							255, idx, idx, idx); 
				}
					
				image.setRGB(i, j, pixel);
				
				noiseArray[i][j] = noiseVal;
				
//				if (i == 0 && j == 0)
//				{
//					System.out.println("noiseVal:" + noiseVal
//							+ " xScale:" + xScale
//							+ " yScale:" + yScale
//							+ " xTranslate:" + xTranslate
//							+ " yTranslate:" + yTranslate
//							+ " Min:" + minClamp
//							+ " Max:" + maxClamp
//							+ " idx:" + idx);
//				}
			}
		}	
		
//		functionText.setText("noise((x / width) * " 
//			+ xScaleVal.getText() + " + " 
//			+ xTranslationVal.getText()
//			+ ", (y / height) * "
//			+ yScaleVal.getText() + " + " 
//			+ yTranslationVal.getText() + ");");
		repaint();
		host.remix();
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		// refresh screen
		g2.setBackground(new Color(255, 255, 255, 255));  //230,250,255
		g2.clearRect(0, 0, width, height);

		g2.drawImage(colorAxis.img, 0, 4, null);
		g2.drawImage(image, 0, 256, null);
	}
	
}