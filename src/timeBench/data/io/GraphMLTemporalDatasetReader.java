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
	private TemporalDataset tds 					= new TemporalDataset();
	
	
	//Constants for the use in class
	private static String TEMP_ELEMENT_ATTR_PREFIX  = prefuse.util.PrefuseConfig.getConfig().getProperty("data.visual.fieldPrefix");
	private static String ROOT 						= 	  "root";
	private static String GRAPH_DIRECTED			= "directed";
	
	/**
	 * Returns the TemporalDataset read from a GraphML file. Overrides method of the superclass.
	 * @throws TemporalDataException 
	 * @see timeBench.data.io.AbstractTemporalDatasetReader#readData(java.io.InputStream)
	 */
    @Override
    public TemporalDataset readData(InputStream is) throws DataIOException, TemporalDataException {
		//Try catch for all the various exception that can occur in the mainReader and it's subcalls.
    	try {
			//mainReader is called upon the start the reading
			mainReader(is);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new DataIOException(e);
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
            throw new DataIOException(e);
		} catch (IOException e) {
			e.printStackTrace();
            throw new DataIOException(e);
		}
		finally{
			try {
				//close the InputStream when done reading
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
    	//return the TemporalDataset in which all the data of the GraphML has been stored in
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
    	//create an instance of the XMLStreamReader via the InputStream
    	XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
    	
    	//Objects needed for operation
    	int event;						//Takes the type of the occurring XMLEvent
    	int graphCounter = 0;			//Used to count graph occurrences, only 1 allowed
    	String nextFieldName = null;	//Used to save the name of the current data element and to add it's value to the hashMap in the CHARACTERS event
    	String lastID = null;			//Used to block out root elements as TE/TO and to add the current ID of a node to the hashMap
    	String oldElement = null;		//Used to check the key/graph validity
        // TODO check edge directed || edgedefault -- each edge must be directed either by default or separately --> not needed now 
//    	boolean edgedefault = false;
    	HashMap<String, String> dataMap = new HashMap<String, String>();	//hashMap that temporarily stores the data of a node, gathers as the necessary values to add it to the TMDS
    	ArrayList<Long> rootList = new ArrayList<Long>();					//Used to save the IDs of the root nodes
    	ArrayList<Edge> edgeList = new ArrayList<Edge>();					//Used to buffer the Edges so they can be added later when all targets/sources for the reference shoul already exist
    	
		//Read the GraphML line for line
    	while (reader.hasNext()) {
    		//saves the next occurring event type
			event = reader.next();
			
			//Switch through the event type enumeration of XMLEvent
			switch (event) {
			//Determines if an element starts and after that the type of the element			
			case XMLEvent.START_ELEMENT:
				//On the occurrence of a key element it checks if there was a graph element before, if yes, invalidity -> exception 
				if(Tokens.KEY.equals(reader.getLocalName()) && oldElement != null) {
		    		if(Tokens.GRAPH.equals(oldElement))
		    			throw new DataIOException("Element KEY is not expected.");
		    	}
				
				//save the current XML tag name for the key/graph validity check
				oldElement = reader.getLocalName();
				
				//save the current ID of the element as long as it is not null
				if(reader.getAttributeValue(null, Tokens.ID) != null)
					lastID = reader.getAttributeValue(null, Tokens.ID);
				
				//Check if a node is a root element or if an edge refers to a root element
				if(ROOT.equals(lastID) || ROOT.equals(reader.getAttributeValue(null, Tokens.TARGET))) {
					if(Tokens.EDGE.equals(reader.getLocalName())){
						//Adds the ID of the nodes referenced to root
						long rootID = Long.parseLong(reader.getAttributeValue(0).substring(1), 10);
						rootList.add(rootID);
					}
				}
				//Read the data columns from the key elements
				else if(Tokens.KEY.equals(reader.getLocalName()))
					configReader(reader);
				//add the ID to the hashMap
				else if(Tokens.NODE.equals(reader.getLocalName()))
					dataMap.put(TemporalElement.ID, lastID);
				//get the key for the value of the next hashMap data
				else if(Tokens.DATA.equals(reader.getLocalName()) && reader.getAttributeValue(null, Tokens.KEY) != null)
					nextFieldName = reader.getAttributeValue(null, Tokens.KEY);
				//add an Edge to the EdgeList
				else if(Tokens.EDGE.equals(reader.getLocalName()))
					edgeList.add(new Edge(reader.getAttributeValue(null, Tokens.SOURCE), reader.getAttributeValue(null, Tokens.TARGET)));
				//Check graph direction and number of graphs, throw exception on invadility
				else if(Tokens.GRAPH.equals(reader.getLocalName())) {
					if(! GRAPH_DIRECTED.equals(reader.getAttributeValue(null, Tokens.EDGEDEF)))
                        throw new DataIOException("Graph is not directed. (At the moment only edgedefault is supported.)");
//						edgedefault = true;
					if(graphCounter > 0) {
						throw new DataIOException("Unexpected graph element detected.");
					}
					graphCounter++;
				}
				break; // START_ELEMENT
				
			//Value saved to the HashMap to later add it as a bulk	
			case XMLEvent.CHARACTERS:
			    // TODO check the StAX API if it is possible that an element content is more than one character events -> concatenate, no overwrite  
				// StAX does not see the second set of characters of data split by another tag as a CHARACTERS event. Therefore I see no possibility to achieve this so far.
				if(nextFieldName != null){	
					//Removal of the prefix for TemporalElements "_"
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
					//decides per prefix if TemporalElement or TemporalObject
				    if (NodeType.ELEMENT == NodeType.byPrefix(lastID)) {
						createTemporalElement(dataMap);
						dataMap.clear();
					}
					else if (NodeType.OBJECT == NodeType.byPrefix(lastID)) {
						createTemporalObject(dataMap);
						dataMap.clear();
					}
				}
				if(Tokens.GRAPH.equals(reader.getLocalName()))
					oldElement = Tokens.GRAPH;
				break; // END_ELEMENT
				
			//When the file ends, the edges are created and the latest compatibility checks are being made
			case XMLEvent.END_DOCUMENT:
				createEdges(edgeList, rootList);		//add the edges to the TemporalDataset
				checkTemporalObjects();					//Check if every TemporalObject has a TemporalElement it belongs to
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
    private void configReader(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError, IOException, TemporalDataException
    {
    	//Check if TemporalElement attribute, which can be ignored since they are known
    	if(!reader.getAttributeValue(null, Tokens.ID).startsWith(TEMP_ELEMENT_ATTR_PREFIX)) {
			@SuppressWarnings("rawtypes")
            Class type = null;
			String typeString = reader.getAttributeValue(null, Tokens.ATTRTYPE);
		
			//get the defined data type as class object
			if(Tokens.STRING.equals(typeString))
				type = String.class;
			else if(Tokens.INT.equals(typeString))
				type = Integer.class;
			else if(Tokens.LONG.equals(typeString))
				type = Long.class;
			else if(Tokens.DOUBLE.equals(typeString))
				type = Double.class;
			else if(Tokens.FLOAT.equals(typeString))
				type = Float.class;
			else if(Tokens.BOOLEAN.equals(typeString))
				type = Boolean.class;
			
			//generate a new data column for the TemporalDataset
			tds.addDataColumn(reader.getAttributeValue(null, Tokens.ATTRNAME), type, null);
    	}
    }
    
    /**
     * Creates a TemporalElement with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalElement(HashMap<String, String> dataMap)
    {
    	//Get all the know attributes of a TemporalElement 
    	int id = Integer.parseInt(dataMap.get(TemporalElement.ID).split(GraphMLTemporalDatasetWriter.ELEMENT_PREFIX)[1]);
		long inf = Long.parseLong(dataMap.get(TemporalElement.INF), 10);
		long sup = Long.parseLong(dataMap.get(TemporalElement.SUP), 10);
		int granularityID = Integer.parseInt(dataMap.get(TemporalElement.GRANULARITY_ID));
		int granularityContextID = Integer.parseInt(dataMap.get(TemporalElement.GRANULARITY_CONTEXT_ID));
		int kind = Integer.parseInt(dataMap.get(TemporalElement.KIND));
		
		//add a new TemporalElement to the TemporalDataset with these values
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

		//Needs to be added as a node since the TemporalObject constructor needs the TemporalElement ID (information in edges)
    	Node node = tds.addNode();
		node.set(TemporalObject.ID, Integer.parseInt(dataMap.get(TemporalElement.ID).substring(1)));
		
		//for the amount of dataColumns in the TemporalDataset, set all their values
		for (int j = 0; j < tds.getDataColumnSchema().getColumnCount(); j++) {
			currentCol = tds.getDataColumnSchema().getColumnName(j);	//gets the current column name via the index
			currentType = tds.getDataColumnSchema().getColumnType(j);	//gets the current column type via the index
			
			String value = dataMap.get(currentCol);						//get the current value from the hashMap via the column name
			// TODO check if missing -> future work/not necessary now
			
			//Convert the value to the type defined for the column
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
    		//Relationships between mutual types of nodes
    		if(e.getSourceType().equals(e.getTargetType())) {
    			if(NodeType.ELEMENT.equals(e.getSourceType()))
    				tds.getTemporalElements().addEdge((int)e.getSourceId(),(int)e.getTargetId());    				
    			else
    				tds.addEdge(tds.getTemporalObject(e.getSourceId()), tds.getTemporalObject(e.getTargetId()));
    		}
    		//TemporalElements needed for the TemporalObject
    		else {
    			//get the node via the source ID (TO ID) and set the TemporalElement ID
    			Node node = tds.getTemporalObject(e.getSourceId());
    			node.set(TemporalObject.TEMPORAL_ELEMENT_ID, e.getTargetId());
    		}
    	}
    	
    	//adds the root elements to the TemporalDataset by first converting it to an array based on the ArrayList's length
    	// TODO consider different data structure in TemporalDataset -> AR
    	long[] roots = new long[rootList.size()];
		for(int i = 0; i < rootList.size(); i++)
			roots[i] = rootList.get(i);
		tds.setRoots(roots);
    }
    
    /**
     * Checks if all the TemporalObject of an TemporalDataset have a
     * TemporalElement. If not, it throws the TemporalDataException.
     * 
     * @throws TemporalDataException
     */
    private void checkTemporalObjects() throws TemporalDataException {
        for (TemporalObject to : tds.temporalObjects()) {
            if (to.getTemporalElement() == null) {
                throw new TemporalDataException(
                        "TemporalObject without a TemporalElement: "
                                + to.getId());
            }
        }
    }
    
    enum NodeType {
        OBJECT, ELEMENT;

        /**
         * determines the type of a node by the prefix of its id.
         * 
         * @param prefix
         *            node id starting with the prefix (e.g., "o2") or only the
         *            prefix ("t").
         * @return the NodeType or <tt>null</tt>
         */
        static NodeType byPrefix(String prefix) {
            if (prefix == null)
                return null;
            if (prefix.startsWith(GraphMLTemporalDatasetWriter.ELEMENT_PREFIX))
                return ELEMENT;
            if (prefix.startsWith(GraphMLTemporalDatasetWriter.OBJECT_PREFIX))
                return OBJECT;
            else
                return null;
        }
    }

    /**
     * Edge object for handling Edge-related data from a GraphML file in an easy
     * way.
     * 
     * @author Sascha Plessberger, Alexander Rind
     * 
     */
    class Edge {
        private NodeType sourceType;
        private NodeType targetType;
        private long sourceId;
        private long targetId;

        /**
         * Constructor
         * 
         * @param source
         * @param target
         */
        public Edge(String source, String target) {
            this.sourceType = NodeType.byPrefix(source);
            this.targetType = NodeType.byPrefix(target);

            sourceId = Long.parseLong(source.substring(1), 10);
            targetId = Long.parseLong(target.substring(1), 10);
        }

        /**
         * Returns the type of the starting point of an edge.
         * 
         * @return
         */
        public NodeType getSourceType() {
            return sourceType;
        }

        /**
         * Returns the type of the ending point of an edge.
         * 
         * @return
         */
        public NodeType getTargetType() {
            return targetType;
        }

        /**
         * Returns the Source ID as a long.
         * 
         * @return
         */
        public long getSourceId() {
            return sourceId;
        }

        /**
         * Returns the Target ID as a long.
         * 
         * @return
         */
        public long getTargetId() {
            return targetId;
        }
    }
}
