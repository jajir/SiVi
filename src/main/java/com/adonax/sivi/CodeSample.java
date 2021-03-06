package com.adonax.sivi;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class CodeSample 
{
	static private String txt = 
"/* \n"
+" * This example demonstrates how many of the controls \n"
+" * on SiVi can be implemented in code. \n"
+" * Written for illustrative purposes, not efficiency. \n"
+" * There's lots of room for optimizations! \n"
+" */ \n"
+" \n"
+"import java.awt.EventQueue; \n"
+"import java.awt.Graphics; \n"
+"import java.awt.image.BufferedImage; \n"
+"import java.awt.image.WritableRaster; \n"
+" \n"
+"import javax.swing.JComponent; \n"
+"import javax.swing.JFrame; \n"
+" \n"
+"import SimplexNoise; \n"
+" \n"
+"public class SiViCodeSample extends JComponent \n"
+"{ \n"
+"    private static final long serialVersionUID = 1L; \n"
+"    private BufferedImage image; \n"
+" \n"	
+"    public SiViCodeSample(int width, int height) \n"
+"    { \n"
+"        image = new BufferedImage(width, height, \n"
+"                    BufferedImage.TYPE_INT_ARGB); \n"
+"        WritableRaster raster = image.getRaster(); \n"
+"        int[] pixel = new int[4]; \n"
+" \n"
+"        // Parameters from SiVi 'single octave' \n"
+"        float xScale = 1; \n" 
+"        float yScale = 1; \n"
+"        float xTranslation = 0; \n"
+"        float yTranslation = 0; \n"
+"        float noiseMax = 1; \n"
+"        float noiseMin = -1; \n"
+"        // radio buttons \n"
+"        boolean smoothNoise = true;  // (v + 1)/2 \n"
+"        boolean turbulentNoise = false; // |v| \n"
+" \n"        
+"        // for each pixel in the image... \n"
+"        for (int y = 0; y < height; y++) \n"
+"        { \n"
+"            for (int x = 0; x < width; x++) \n"
+"            { \n"
+"                double noiseValue = SimplexNoise.noise( \n"
+"                		x * (xScale/128f) + xTranslation, \n" 
+"                		y * (yScale/128f) + yTranslation); \n"
+" \n"                
+"                // the following is rarely used, can omit \n"
+"                noiseValue = Math.min(noiseMax, \n" 
+"                		Math.max(noiseMin, noiseValue)); \n"
+" \n"                
+"                // If there were additional octaves, they \n"
+"                // could be calculated here, followed by \n"
+"                // the weighted summing of the results of \n" 
+"                // all the octaves. In SiVi, multiple \n" 
+"                // octaves have their weights set via the  \n"
+"                // 'Mix' slider panel. \n"
+" \n"                
+"                // normalization method \n"
+"                if (smoothNoise) \n"
+"                { \n"
+"                	noiseValue = (noiseValue + 1) / 2; \n"
+"                } \n"
+"                else if (turbulentNoise) \n"
+"                { \n"
+"                	noiseValue = Math.abs(noiseValue); \n"
+"                } \n"
+"                // Note: It's possible to skip normalization \n" 
+"                // ('NONE'), but you better make sure your \n"  
+"                // mapping still works!  \n"
+" \n"                
+"                // We map to a value in the color range [0, 255] \n"
+"                noiseValue *= 256; \n"
+" \n" 
+"                // One could also use a pre-built map to \n"
+"                // translate noise values to anything, for \n"
+"                // example, a mapping that designates terrain \n"
+"                // types. \n"
+"                // Also, one could apply the noise value \n"
+"                // to modulate another function (referred to \n"
+"                // as 'gradients' in SiVi. \n"
+" \n"                               
+"                pixel[0] = (int)noiseValue; // RED \n"
+"                pixel[1] = (int)noiseValue; // GREEN \n"
+"                pixel[2] = (int)noiseValue; // BLUE \n"
+"                pixel[3] = 255; // opaque; \n"
+" \n"
+"                raster.setPixel(x, y, pixel); \n"
+"            } \n"
+"        } \n"        
+"    } \n"
+" \n"
+"    public void paintComponent(Graphics g) \n"
+"    { \n"
+"        g.drawImage(image, 0, 0, null); \n"
+"    } \n"    
+" \n"
+"    public static void main(String[] args) { \n"
+" \n"
+"        EventQueue.invokeLater(new Runnable(){ \n"
+" \n"
+"            public void run() \n"
+"            { \n"    
+"                JFrame frame = new JFrame(); \n"
+"                frame.setDefaultCloseOperation( \n"
+"                        JFrame.EXIT_ON_CLOSE); \n"
+"                frame.setSize(408, 194); \n"
+"                frame.setTitle(\"Test Simplex Texture Image\"); \n"
+"                SiViCodeSample panel = new SiViCodeSample(400, 160); \n"
+"                frame.add(panel); \n"
+"                frame.setVisible(true); \n"
+"            } \n"
+"        }); \n"
+"    } \n"	     
+"} \n";

	public static JScrollPane getCodeSamplePane()
	{
		JTextPane tp = new JTextPane();
		tp.setEditable(false);
		tp.setText(txt);
		tp.setFont(new Font("Monospaced", Font.PLAIN, 14));
	
		JScrollPane sp = new JScrollPane(tp);		
		return sp;
	}
	
}
