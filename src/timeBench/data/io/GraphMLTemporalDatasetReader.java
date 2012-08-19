package timeBench.data.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader.Tokens;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;


/**
 * TemporalDatasetReader implementation that reads in temporal data formatted
 * using the GraphML file format. GraphML is an XML format supporting graph
 * structure and typed data schemas for both nodes and edges. For more
 * information about the format, please see the <a
 * href="http://graphml.graphdrawing.org/">GraphML home page</a>.
 * 
 * Based on {@link prefuse.data.io.GraphMLReader}.
 * 
 * @author Sascha Plessberger, Alexander Rind
 */
public class GraphMLTemporalDatasetReader extends AbstractTemporalDatasetReader {	
	/**
	 * TemporalDataset to be filled and accessible via the readData method.
	 */
    // TODO why static? consider the class is used twice, or the same object is used twice -> member variable and initialize in readData()
	private static TemporalDataset tds 				= new TemporalDataset();
	
	
	//Constants for the use in class
	//TODO: maybe some already existent and unnecessary defined here?
	private static String TEMP_ELEMENT_ATTR_PREFIX  = prefuse.util.PrefuseConfig.getConfig().getProperty("data.visual.fieldPrefix");
	private static String TEMP_ELEMENT_ID_PREFIX 	= 	     "t";
	
	private static String CLASS_STRING				=   "string";
	private static String CLASS_INT					=      "int";
	private static String CLASS_DOUBLE				=   "double";
	private static String CLASS_FLOAT				=    "float";
	private static String CLASS_LONG				=     "long";
	private static String CLASS_BOOLEAN				=  "boolean";
	
	private static String ROOT 						= 	  "root";
	
	private static String GRAPH_DIRECTED			= "directed";
	
	/**
	 * Returns the TemporalDataset read from a GraphML file.
	 * @see timeBench.data.io.AbstractTemporalDatasetReader#readData(java.io.InputStream)
	 */
    @Override
    public TemporalDataset readData(InputStream is) throws DataIOException {
		try {
			mainReader(is);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemporalDataException e) {
			e.printStackTrace();
		}
		finally{
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return tds;
    }
    
    /**
    * Reads the GraphML-file and checks it's content.
    * 
    * @param is
    * @throws XMLStreamException
    * @throws FactoryConfigurationError
    * @throws IOException
    * @throws TemporalDataException 
    * @throws DataIOException 
    */
    private void mainReader(InputStream is) throws XMLStreamException, FactoryConfigurationError, IOException, TemporalDataException, DataIOException
    {    	
    	XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
    	
    	//Objects needed for operation
    	int event;
    	int elementAttributeCount = 0;
    	int graphCounter = 0;
    	String nextFieldName = null;
    	String lastID = null;
    	boolean edgedefault = false;
    	boolean orderCheckDone = false;
    	HashMap<String, String> dataMap = new HashMap<String, String>();
    	ArrayList<Long> rootList = new ArrayList<Long>();
    	ArrayList<Edge> edgeList = new ArrayList<Edge>();
    	
		//Read the GraphML line for line
    	while (reader.hasNext()) {
			event = reader.next();
			
			switch (event) {
			//Determines if an element starts and after that the type of the element			
			case XMLEvent.START_ELEMENT:
			    // TODO only checks once, but then the flag is true; how about key -> graph -> key
				if(!(orderCheckDone))
					orderCheckDone = orderCheck(reader);
				
				lastID = reader.getAttributeValue(null, Tokens.ID);
				
				if(ROOT.equals(lastID) || ROOT.equals(reader.getAttributeValue(null, Tokens.TARGET))) {
					if(Tokens.EDGE.equals(reader.getLocalName())){
						long rootID = Long.parseLong(reader.getAttributeValue(0).substring(1,2), 10);
						rootList.add(rootID);
					}
				}					
				else if(Tokens.KEY.equals(reader.getLocalName()))
					elementAttributeCount += configReader(reader);
				else if(Tokens.NODE.equals(reader.getLocalName()))
					dataMap.put(TemporalElement.ID, lastID);
				else if(Tokens.DATA.equals(reader.getLocalName()))
					nextFieldName = reader.getAttributeValue(null, Tokens.KEY);
				else if(Tokens.EDGE.equals(reader.getLocalName()))
				    // TODO check edge directed || edgedefault -- each edge must be directed either by default or separately --> not needed now 
					edgeList.add(new Edge(reader.getAttributeValue(null, Tokens.SOURCE), reader.getAttributeValue(null, Tokens.TARGET)));
				else if(Tokens.GRAPH.equals(reader.getLocalName())) {
					if(GRAPH_DIRECTED.equals(reader.getAttributeValue(null, Tokens.EDGEDEF)))
						edgedefault = true;
					if(graphCounter > 0) {
						throw new DataIOException("Unexpected graph element detected.");
					}
					graphCounter++;
				}
				break; // START_ELEMENT
				
			//Value saved to the HashMap to later add it as a bulk	
			case XMLEvent.CHARACTERS:
			    // TODO check the StAX API if it is possible that an element content is more than one character events -> concatenate, no overwrite  
				if(nextFieldName != null){	
					if(nextFieldName.startsWith(TEMP_ELEMENT_ATTR_PREFIX))
						dataMap.put(nextFieldName.split(TEMP_ELEMENT_ATTR_PREFIX)[1], reader.getText());
					else
						dataMap.put(nextFieldName, reader.getText());
					nextFieldName = null;
				}
				break; // CHARACTERS
				
			//When the element ends, call the appropriate method to add it to the TemporalDataset
			case XMLEvent.END_ELEMENT:
				if(Tokens.NODE.equals(reader.getLocalName()) && !(ROOT.equals(lastID))){
				    // TODO bug! temporal object can have 5 or more data fields 
					if(dataMap.size() >= elementAttributeCount) {
						createTemporalElement(dataMap);
						dataMap.clear();
					}
					else {
						createTemporalObject(dataMap);
						dataMap.clear();
					}
				}
				break; // END_ELEMENT
				
			//When the file ends, the edges are created and the latest compatibility checks are being made
			case XMLEvent.END_DOCUMENT:
				createEdges(edgeList, rootList);
				checkTemporalObjects();
				break; // END_DOCUMENT

			default:
				break;
			}
		}
    }
    
    /**
     * Determines the content of a key element and configures the TemporalDataset accordingly.
     * Returns the Number of elements defined for a TemporalElement.
     * 
     * @param reader
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws TemporalDataException 
     */
    private int configReader(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError, IOException, TemporalDataException
    {
    	if(reader.getAttributeValue(null, Tokens.ID).startsWith(TEMP_ELEMENT_ATTR_PREFIX))
    		return 1;
		else {
			@SuppressWarnings("rawtypes")
            Class type = null;
			String typeString = reader.getAttributeValue(null, Tokens.ATTRTYPE);
		
			if(CLASS_STRING.equals(typeString))
				type = String.class;
			else if(CLASS_INT.equals(typeString))
				type = Integer.class;
			else if(CLASS_LONG.equals(typeString))
				type = Long.class;
			else if(CLASS_DOUBLE.equals(typeString))
				type = Double.class;
			else if(CLASS_FLOAT.equals(typeString))
				type = Float.class;
			else if(CLASS_BOOLEAN.equals(typeString))
				type = Boolean.class;
			
			tds.addDataColumn(reader.getAttributeValue(null, Tokens.ATTRNAME), type, null);
			return 0;
    	}
    }
    
    /**
     * Creates a TemporalElement with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalElement(HashMap<String, String> dataMap)
    {
    	int id = Integer.parseInt(dataMap.get(TemporalElement.ID).split(TEMP_ELEMENT_ID_PREFIX)[1]);
		long inf = Long.parseLong(dataMap.get(TemporalElement.INF), 10);
		long sup = Long.parseLong(dataMap.get(TemporalElement.SUP), 10);
		int granularityID = Integer.parseInt(dataMap.get(TemporalElement.GRANULARITY_ID));
		int granularityContextID = Integer.parseInt(dataMap.get(TemporalElement.GRANULARITY_CONTEXT_ID));
		int kind = Integer.parseInt(dataMap.get(TemporalElement.KIND));
		
		tds.addTemporalElement(id, inf, sup, granularityID, granularityContextID, kind);
    }
    
    /**
     * Creates a TemporalObject with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalObject(HashMap<String, String> dataMap)
    {
    	String currentCol = null;
		@SuppressWarnings("rawtypes")
        Class currentType = null;
		
    	Node node = tds.addNode();
    	// TODO bug: fails on "o1100"! ->  substring(1)
		node.set(TemporalObject.ID, Integer.parseInt(dataMap.get(TemporalElement.ID).substring(1, 2)));
		
		for (int j = 0; j < tds.getDataColumnSchema().getColumnCount(); j++) {
			currentCol = tds.getDataColumnSchema().getColumnName(j);
			currentType = tds.getDataColumnSchema().getColumnType(j);
			
			String value = dataMap.get(currentCol);
			// TODO check if missing -> future work/not necessary now
			
			if(String.class.equals(currentType))
				node.set(currentCol, value);
			else if(Integer.class.equals(currentType))
				node.set(currentCol, Integer.parseInt(value));
			else if(Double.class.equals(currentType))
				node.set(currentCol, Double.parseDouble(value));
			else if(Long.class.equals(currentType))
				node.set(currentCol, Long.parseLong(value));
			else if(Float.class.equals(currentType))
				node.set(currentCol, Float.parseFloat(value));
			else if(Boolean.class.equals(currentType)) 
			    node.set(currentCol, Boolean.valueOf(value));
		}
    }
    
    /**
     * Adds the Edges from the edgeList to the TemporalDataset. (Use only after adding nodes!)
     * @param edgeList
     */
    private void createEdges(ArrayList<Edge> edgeList, ArrayList<Long> rootList)
    {
    	for(Edge e : edgeList)
    	{
    		if(e.GetSourceType().equals(e.GetTagetType())) {
    			if(Edge.Tokens.TEMP_ELEMENT.equals(e.GetSourceType()))
    			    // TODO bug addEdge() uses row number not id
    				tds.getTemporalElements().addEdge(e.getSourceIdAsInt(), e.getTargetIdAsInt());    				
    			else
                    // TODO bug addEdge() uses row number not id
    				tds.addEdge(tds.getTemporalObject(e.getSourceIdAsLong()), tds.getTemporalObject(e.getTargetIdAsLong()));
    		}
    		else {
    			Node node = tds.getTemporalObject(e.getSourceIdAsLong());
    			node.set(TemporalObject.TEMPORAL_ELEMENT_ID, e.getTargetIdAsLong());
    		}
    	}
    	
    	// TODO consider different data structure in TemporalDataset -> AR
    	long[] roots = new long[rootList.size()];
		for(int i = 0; i < rootList.size(); i++)
			roots[i] = rootList.get(i);
		tds.setRoots(roots);
    }
    
    /**
     * Checks if all the TemporalObject of an TemporalDataset are anchored to a TemporalElement. If not, it throws the TemporalDataException.
     * @return
     * @throws TemporalDataException 
     */
    private void checkTemporalObjects() throws TemporalDataException
    {
    	for (TemporalObject to : tds.temporalObjects()) {
    		// TODO anchored has a different meaning 
			if(!to.getTemporalElement().isAnchored())
			{
				throw new TemporalDataException("Unanchored TemporalObject!");
			}

		}
    }
    
    /**
     * Checks if the key elements are defined before the graph. If not, it throws the DataIOException.
     * @param reader
     * @return
     * @throws DataIOException 
     */
    private boolean orderCheck(XMLStreamReader reader) throws DataIOException
    {
    	if(Tokens.KEY.equals(reader.getLocalName()))
    		return true;
    	else if (Tokens.GRAPH.equals(reader.getLocalName())) {
			throw new DataIOException("Expected key-element first!");
		}
    	else 
    		return false;
    }
}
