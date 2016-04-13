package org.timebench.util;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.timebench.action.layout.HorizonGraphAction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


/**
 * The main class for storing the color palette information.
 * The class also enables access to the loaded ColorPalettes via static method.
 * The XML configuration file is automatically loaded when the class is accessed for the first time.
 * In the XML file, the different color bands for any number of bands are described.
 * int[] has to be structured like:
 * - negative outlier
 * - negative bands from lowest to highest (eg. -5 to -1)
 * - NULL value
 * - positive bands from highest to lowest (eg. 1 to 5)
 * - positive outlier
 */
public class HorizonColorPalette
{	
	static Map<String, HorizonColorPalette> _schemas;	// key: color schema name; 
	static final String COLOR_PALETTE_SETTINGS_PATH = "HorizonColorPalettes.xml";
	
	/**
	 * Loads the settings from the XML file into the colors and color palettes.
	 */
	public static void loadFromXml()
	{
		Document document;
		try
		{
			document = readXml(COLOR_PALETTE_SETTINGS_PATH);
		}
		catch(Exception e)
		{
			return;
		}
		
		_schemas = new HashMap<String, HorizonColorPalette>();
		
		NodeList schemaNodes = document.getElementsByTagName("schema");
		for(int i = 0; i < schemaNodes.getLength(); i++)
		{
			Node schemaNode = schemaNodes.item(i);
			String schemaName = schemaNode.getAttributes().getNamedItem("name").getTextContent();
		
			NodeList paletteNodes = schemaNode.getChildNodes();
			HorizonColorPalette palette = new HorizonColorPalette();
			
			int l = paletteNodes.getLength();
			
			for(int j = 0; j < paletteNodes.getLength(); j++)
			{
				Node paletteNode = paletteNodes.item(j);
				if(!paletteNode.getNodeName().equals("palette"))
					continue;
				
				String bandsCountStr = paletteNode.getAttributes().getNamedItem("bandsCount").getTextContent();
				
				
				int bandsCount = new Integer(bandsCountStr).intValue();
				
				int[] colors = new int[HorizonGraphAction.MAX_BANDS * 2 + 3];
				NodeList colorNodes = paletteNode.getChildNodes();
				for(int k = 0; k < colorNodes.getLength(); k++)
				{
					Node colorNode = colorNodes.item(k);
					if(!colorNode.getNodeName().equals("color"))
						continue;
					
					int band = new Integer(colorNode.getAttributes().getNamedItem("band").getTextContent()).intValue();
					int alpha = new Integer(colorNode.getAttributes().getNamedItem("alpha").getTextContent()).intValue();
					int red = new Integer(colorNode.getAttributes().getNamedItem("red").getTextContent()).intValue();
					int green = new Integer(colorNode.getAttributes().getNamedItem("green").getTextContent()).intValue();
					int blue = new Integer(colorNode.getAttributes().getNamedItem("blue").getTextContent()).intValue();
					
					colors[band + HorizonGraphAction.MAX_BANDS + 1] = new Color(red, green, blue, alpha).getRGB();
				}
				
				palette._colorMap.put(bandsCount, colors);
			}
			
			_schemas.put(schemaName, palette);
		}
	}	
	
	
	/**
	 * Reads the XML-Code from the file into an XML-Document (after loading from the file).
	 * @param filePath The file path of the configuration (e.g. COLOR_PALETTE_SETTINGS_PATH).
	 * @return The an instance of the XML document.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document readXml(String filePath) throws ParserConfigurationException, SAXException, IOException
	{
		InputStream inputStream = HorizonColorPalette.class.getResourceAsStream(filePath); // new FileInputStream(new File(filePath));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
								
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(inputStream);
		
		return doc;
	}


	/**
	 * The color palette is returned to its named.
	 * @param name The name of the color palette.
	 * @return The named HorizonColorPalette.
	 */
	public static HorizonColorPalette getColorPalette(String name)
	{
		if(_schemas == null)
			loadFromXml();
		
		return _schemas.get(name);
	}	
	
	
	/**
	 * Returns all color palette names.
	 * @return A String[] with all names.
	 */
	public static String[] getColorPaletteNames()
	{
		if(_schemas == null)
			loadFromXml();
		
		String[] names = new String[_schemas.size()];
		
		int i = 0;
		for(String key : _schemas.keySet())
			names[i++] = key;
		
		return names;
	}
		
	
	Map<Integer, int[]> _colorMap;			// key: bandsCount; value: colors
	private HorizonColorPalette()
	{
		_colorMap = new HashMap<Integer, int[]>();
	}
		
	
	/**
	 * The colors are returned using the band number.
	 * @param bandsCount number of bands
	 * @return An int[] filled with the color values.
	 */
	public int[] getColors(int bandsCount)
	{
		return _colorMap.get(bandsCount);
	}
}
