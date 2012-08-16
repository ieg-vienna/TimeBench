package timeBench.data.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader.Tokens;
import timeBench.data.Edge;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
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
 * @author Alexander Rind, Sascha Plessberger
 */
public class GraphMLTemporalDatasetReader extends AbstractTemporalDatasetReader {	
	/**
	 * TemporalDataset to be filled and accessible via the readData method.
	 */
	private static TemporalDataset tds = new TemporalDataset();
	
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
    	String nextDataType = null;
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
				if(!(orderCheckDone))
					orderCheckDone = orderCheck(reader);
				
				lastID = reader.getAttributeValue(null, Tokens.ID);
				
				if("root".equals(lastID) || "root".equals(reader.getAttributeValue(null, Tokens.TARGET))) {
					if(Tokens.EDGE.equals(reader.getLocalName())){
						long rootID = Long.parseLong(reader.getAttributeValue(0).substring(1,2), 10);
						rootList.add(rootID);
					}
				}					
				else if(Tokens.KEY.equals(reader.getLocalName()))
					elementAttributeCount += configReader(reader);
				else if(Tokens.NODE.equals(reader.getLocalName()))
					dataMap.put("ID", lastID);
				else if(Tokens.DATA.equals(reader.getLocalName()))
					nextDataType = reader.getAttributeValue(null, Tokens.KEY);
				else if(Tokens.EDGE.equals(reader.getLocalName()))
					edgeList.add(new Edge(reader.getAttributeValue(null, Tokens.SOURCE), reader.getAttributeValue(null, Tokens.TARGET)));
				else if(Tokens.GRAPH.equals(reader.getLocalName())) {
					if("directed".equals(reader.getAttributeValue(null, Tokens.EDGEDEF)))
						edgedefault = true;
				}
				break;
				
			//Value saved to the HashMap to later add it as a bulk	
			case XMLEvent.CHARACTERS:
				if(nextDataType != null){	
					if(nextDataType.startsWith("_"))
						dataMap.put(nextDataType.split("_")[1], reader.getText());
					else
						dataMap.put(nextDataType, reader.getText());
					nextDataType = null;
				}
				break;
				
			//When the element ends, call the appropriate method to add it to the TemporalDataset
			case XMLEvent.END_ELEMENT:
				if(Tokens.NODE.equals(reader.getLocalName()) && !("root".equals(lastID))){
					if(dataMap.size() >= elementAttributeCount) {
						createTemporalElement(dataMap);
						dataMap.clear();
					}
					else {
						createTemporalObject(dataMap);
						dataMap.clear();
					}
				}
				break;
			//When the file ends, the edges are created and the latest compatibility checks are being made
			case XMLEvent.END_DOCUMENT:
				createEdges(edgeList, rootList);
				checkTemporalObjects();
				checkDirected(edgedefault);
				break;

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
     */
    private int configReader(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError, IOException
    {
    	if(reader.getAttributeValue(null, Tokens.ID).startsWith(prefuse.util.PrefuseConfig.getConfig().getProperty("data.visual.fieldPrefix")))
    		return 1;
		else {
			Class type = null;
			String typeString = reader.getAttributeValue(null, Tokens.ATTRTYPE);
		
			if("string".equals(typeString))
				type = String.class;
			else if("int".equals(typeString))
				type = Integer.class;
			else if("long".equals(typeString))
				type = Long.class;
			else if("double".equals(typeString))
				type = Double.class;
			else if("float".equals(typeString))
				type = Float.class;
			else if("boolean".equals(typeString))
				type = Boolean.class;
			
			tds.addColumn(reader.getAttributeValue(null, Tokens.ATTRNAME), type);
			return 0;
    	}
    }
    
    /**
     * Creates a TemporalElement with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalElement(HashMap<String, String> dataMap)
    {
    	int id = Integer.parseInt(dataMap.get("ID").split("t")[1]);
		long inf = Long.parseLong(dataMap.get("inf"), 10);
		long sup = Long.parseLong(dataMap.get("sup"), 10);
		int granularityID = Integer.parseInt(dataMap.get("granularityID"));
		int granularityContextID = Integer.parseInt(dataMap.get("granularityContextID"));
		int kind = Integer.parseInt(dataMap.get("kind"));
		
		tds.addTemporalElement(id, inf, sup, granularityID, granularityContextID, kind);
    }
    
    /**
     * Creates a TemporalObject with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalObject(HashMap<String, String> dataMap)
    {
    	String currentCol = null;
		Class currentType = null;
		
    	Node node = tds.addNode();
		node.set(TemporalObject.ID, Integer.parseInt(dataMap.get("ID").substring(1, 2)));
		
		for (int j = 0; j < tds.getDataColumnSchema().getColumnCount(); j++) {
			currentCol = tds.getDataColumnSchema().getColumnName(j);
			currentType = tds.getDataColumnSchema().getColumnType(j);
			
			if(String.class.equals(currentType))
				node.set(currentCol, dataMap.get(currentCol));
			else if(Integer.class.equals(currentType))
				node.set(currentCol, Integer.parseInt(dataMap.get(currentCol)));
			else if(Double.class.equals(currentType))
				node.set(currentCol, Double.parseDouble(dataMap.get(currentCol)));
			else if(Long.class.equals(currentType))
				node.set(currentCol, Long.parseLong(dataMap.get(currentCol)));
			else if(Float.class.equals(currentType))
				node.set(currentCol, Float.parseFloat(dataMap.get(currentCol)));
			else if(Boolean.class.equals(currentType))
			{
				if("true".equals(dataMap.get(currentCol)))
					node.set(currentCol, true);
				else
					node.set(currentCol, false);
			}				
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
    			if("element".equals(e.GetSourceType()))
    				tds.getTemporalElements().addEdge(e.getSourceIdAsInt(), e.getTargetIdAsInt());    				
    			else
    				tds.addEdge(tds.getTemporalObject(e.getSourceIdAsLong()), tds.getTemporalObject(e.getTargetIdAsLong()));
    		}
    		else {
    			Node node = tds.getTemporalObject(e.getSourceIdAsLong());
    			node.set(TemporalObject.TEMPORAL_ELEMENT_ID, e.getTargetIdAsLong());
    		}
    	}
    	
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
    
    /**
     * Checks if the edgedefault and the tds.isDirected() are the same, if not, the edgedefault in the graphML is deemed as incorrect and throws a TemporalDataException.
     * @param edgedefault
     * @throws TemporalDataException
     */
    private void checkDirected(boolean edgefault) throws TemporalDataException
    {
    	if(edgefault != tds.isDirected())
    		throw new TemporalDataException("edgedefault incorrect!");
    }
}
