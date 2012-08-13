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
    	String nextType = null;
    	String lastAttribute = null;
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
				
				lastAttribute = reader.getAttributeValue(0);
				
				if("root".equals(reader.getAttributeValue(0)) || "root".equals(reader.getAttributeValue(1))) {
					if(Tokens.EDGE.equals(reader.getLocalName())){
						long rootID = Long.parseLong(reader.getAttributeValue(0).substring(1,2), 10);
						rootList.add(rootID);
					}
				}					
				else if(Tokens.KEY.equals(reader.getLocalName())) 
					configReader(reader);
				else if(Tokens.NODE.equals(reader.getLocalName()))
					dataMap.put("ID", reader.getAttributeValue(0));
				else if(Tokens.DATA.equals(reader.getLocalName()))
					nextType = reader.getAttributeValue(0);
				else if(Tokens.EDGE.equals(reader.getLocalName()))
					edgeList.add(new Edge(reader.getAttributeValue(0), reader.getAttributeValue(1)));
				
				break;
				
			//Value saved to the HashMap to later add it as a bulk	
			case XMLEvent.CHARACTERS:
				if(nextType != null){				
					dataMap.put(nextType, reader.getText());
					nextType = null;
				}
				break;
				
			//When the element ends, call the appropriate method to add it to the TemporalDataset
			case XMLEvent.END_ELEMENT:
				if(Tokens.NODE.equals(reader.getLocalName()) && !("root".equals(lastAttribute))){
					if(dataMap.size() == 6) {
						createTemporalElement(dataMap);
						dataMap.clear();
					}
					else {
						createTemporalObject(dataMap);
						dataMap.clear();
					}
				}
				break;
			case XMLEvent.END_DOCUMENT:
				createEdges(edgeList, rootList);
				checkTemporalObjects();
				break;

			default:
				break;
			}
		}
    }
    
    /**
     * Determines the content of a key element and configures the TemporalDataset accordingly.
     * 
     * @param reader
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    private void configReader(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError, IOException
    {    
    	if(!("_".equals(reader.getAttributeValue(0).substring(0,1)))) {
			Class type = null;
			if("string".equals(reader.getAttributeValue(2)))
				type = String.class;
			if("int".equals(reader.getAttributeValue(2)))
				type = Integer.class;
			if("long".equals(reader.getAttributeValue(2)))
				type = Long.class;
			if("double".equals(reader.getAttributeValue(2)))
				type = Double.class;
			
			tds.addColumn(reader.getAttributeValue(1), type);
    	}
    }
    
    /**
     * Creates a TemporalElement with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalElement(HashMap<String, String> dataMap)
    {
    	int id = Integer.parseInt(dataMap.get("ID").substring(1, 2));
		long inf = Long.parseLong(dataMap.get("_inf"), 10);
		long sup = Long.parseLong(dataMap.get("_sup"), 10);
		int granularityID = Integer.parseInt(dataMap.get("_granularityID"));
		int granularityContextID = Integer.parseInt(dataMap.get("_granularityContextID"));
		int kind = Integer.parseInt(dataMap.get("_kind"));
		
		tds.addTemporalElement(id, inf, sup, granularityID, granularityContextID, kind);
    }
    
    /**
     * Creates a TemporalObject with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalObject(HashMap<String, String> dataMap)
    {
    	Node node = tds.addNode();
		node.set(TemporalObject.ID, Integer.parseInt(dataMap.get("ID").substring(1, 2)));
		node.set("name", dataMap.get("name"));
		node.set("value", Double.parseDouble(dataMap.get("value")));
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
			if(to.getTemporalElement() == null)
			{
				throw new TemporalDataException(this.toString());
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
			throw new DataIOException(this.toString());
		}
    	else 
    		return false;
    }
}
