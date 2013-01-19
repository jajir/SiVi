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
 
import java.awt.Color;
import java.awt.image.*;
import java.util.ArrayList;

public class ColorAxis {

	public ColorMap colorMap;
	
	private int[] data;
	BufferedImage img;
	boolean hover;
	ArrayList<ColorBarPeg>colorBarPegs;
	boolean useHsbLerp;
	
	public ColorAxis()
	{
		data = new int[256];
		img = new BufferedImage(256, 16, 
				BufferedImage.TYPE_INT_ARGB);
		
		// default data will be black to white gradient
//		WritableRaster raster = img.getRaster();
		
		for (int i = 0; i < 256; i++)
		{
			data[i] = calculateARGB(255, i, i, i);
			
			for (int j = 0; j < 16; j++)
			{
				img.setRGB(i, j, data[i]);
			}
		}
		
		colorBarPegs = new ArrayList<ColorBarPeg>();
		colorBarPegs.add(new ColorBarPeg(0, extractRed(data[0]), 
				extractGreen(data[0]), extractBlue(data[0]),
				255));
		colorBarPegs.add(new ColorBarPeg(255, extractRed(data[255]), 
				extractGreen(data[255]), extractBlue(data[255]),
				255));
		
		colorMap = new ColorMap(data);
	}
	
	public static int calculateARGB(int a, int r, int g, int b)
	{
		return b + (g << 8) + (r <<16) + (a << 24);
	}
	
	public static int extractAlpha(int colorARGB)
	{
		return (colorARGB & 0xFF000000) >> 24;
	}

	
	public static int extractRed(int colorARGB)
	{
		return (colorARGB & 0x00FF0000) >> 16;
	}

	public static int extractGreen(int colorARGB)
	{
		return (colorARGB & 0x0000FF00) >> 8;
	}
	
	public static int extractBlue(int colorARGB)
	{
		return (colorARGB & 0x000000FF);
	}
	
	public ColorAxis copyTo(ColorAxis target)
	{
		target.hover = hover;
		target.useHsbLerp = useHsbLerp;
		target.setColorBarPegs(colorBarPegs); // makes a new set
		target.update(); // will derive data and ColorMap and img
		
		return target;
	}
	
	public ArrayList<ColorBarPeg> getColorBarPegs()
	{
		ArrayList<ColorBarPeg> newRowData = new ArrayList<ColorBarPeg>();
		for (ColorBarPeg rd : colorBarPegs)
		{
			newRowData.add(new ColorBarPeg(rd.getLocation(), 
					rd.getrColor(), rd.getgColor(), rd.getbColor(), 255));
		}
		return newRowData;	
	}
	
	public void setColorBarPegs(ArrayList<ColorBarPeg> colorBarPegs)
	{	
		this.colorBarPegs.clear();

		for (ColorBarPeg rd : colorBarPegs)
		{
			this.colorBarPegs.add(
					new ColorBarPeg(rd.getLocation(), 
							rd.getrColor(), rd.getgColor(), 
							rd.getbColor(), rd.getAlpha()));
		}
		update();
	}
	
	public void update()
	{
		int idx, cols;
		float redIncr, greenIncr, blueIncr;
		float redSum, greenSum, blueSum;
		float hueIncr, saturationIncr, brightnessIncr;
		float hueSum, saturationSum, brightnessSum;
		float[] hsbStartVals = new float[3];
		float[] hsbEndVals = new float[3];
		Color color;
		
		int lastRow = colorBarPegs.size() - 1;
		
		for (int i = 0; i < lastRow; i++)
		{
			idx = colorBarPegs.get(i).getLocation();
			redSum = colorBarPegs.get(i).getrColor(); 
			greenSum = colorBarPegs.get(i).getgColor();
			blueSum = colorBarPegs.get(i).getbColor();
			
			data[idx] = calculateARGB(255, (int)redSum, 
					(int)greenSum, (int)blueSum); 
			
			cols = colorBarPegs.get(i + 1).getLocation() - idx;
		
			if (useHsbLerp)
			{
				hsbStartVals = Color.RGBtoHSB(extractRed(data[idx]), 
						extractGreen(data[idx]), extractBlue(data[idx]),
						hsbStartVals);
				
				hsbEndVals = Color.RGBtoHSB(
						(int)colorBarPegs.get(i + 1).getrColor(), 
						(int)colorBarPegs.get(i + 1).getgColor(),
						(int)colorBarPegs.get(i + 1).getbColor(),
						hsbEndVals);
				
				hueSum = hsbStartVals[0];
				saturationSum = hsbStartVals[1];
				brightnessSum = hsbStartVals[2];
		
				if (Math.abs(hsbEndVals[0] - hsbStartVals[0]) > 0.5)
				{
					if (hsbEndVals[0] > hsbStartVals[0])
					{
						hsbStartVals[0] += 1;						
					}
					else
					{
						hsbEndVals[0] += 1;
					}
				}
				hueIncr = (hsbEndVals[0] - hsbStartVals[0])/cols;
				saturationIncr = (hsbEndVals[1] - hsbStartVals[1])/cols;
				brightnessIncr = (hsbEndVals[2] - hsbStartVals[2])/cols;
				
				for (int x = idx, n = idx + cols; x < n; x++)
				{
					hueSum += hueIncr;
					saturationSum += saturationIncr;
					brightnessSum += brightnessIncr;
					
					color = new Color(Color.HSBtoRGB(hueSum, saturationSum, brightnessSum));
					data[x] = calculateARGB(255, 
							color.getRed(), 
							color.getGreen(), 
							color.getBlue());
					
				}
				
				
			}
			else
			{
				redIncr = (colorBarPegs.get(i + 1).getrColor() - redSum)/cols;
				greenIncr = (colorBarPegs.get(i + 1).getgColor() - greenSum)/cols;
				blueIncr = (colorBarPegs.get(i + 1).getbColor() - blueSum)/cols;
				
				for (int x = idx; x < idx + cols; x++)
				{
					redSum += redIncr;
					greenSum += greenIncr;
					blueSum += blueIncr;
					data[x] = calculateARGB(255,
							Math.round(redSum),
							Math.round(greenSum), 
							Math.round(blueSum));
				}
			}
		}
		
		data[255] = calculateARGB(
				255,
				colorBarPegs.get(lastRow).getrColor(),
				colorBarPegs.get(lastRow).getgColor(),
				colorBarPegs.get(lastRow).getbColor());
		
		// update the ColorBar image
		for (int i = 0; i < 256; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				img.setRGB(i, j, data[i]);
			}
		}		
		
		colorMap = new ColorMap(data);
	}
}
