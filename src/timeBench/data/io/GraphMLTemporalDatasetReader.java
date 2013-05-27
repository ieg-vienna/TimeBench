package timeBench.data.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader.Tokens;
import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;


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
	private static final String EDGE_TARGET = "_target";


    private static final String EDGE_SOURCE = "_source";


    /**
	 * TemporalDataset to be filled and accessible via the readData method.
	 */
	

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
        TemporalDataset tds = new TemporalDataset();
		//Try catch for all the various exception that can occur in the mainReader and it's subcalls.
    	try {
			//mainReader is called upon the start the reading
			mainReader(is, tds);
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
    private void mainReader(InputStream is, TemporalDataset tds) throws XMLStreamException, FactoryConfigurationError, IOException, TemporalDataException, DataIOException
    {    	
    	//create an instance of the XMLStreamReader via the InputStream
    	XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
    	
    	//Objects needed for operation
    	int event;						//Takes the type of the occurring XMLEvent
    	int graphCounter = 0;			//Used to count graph occurrences, only 1 allowed
    	String oldElement = null;		//Used to check the key/graph validity
        // TODO check edge directed || edgedefault -- each edge must be directed either by default or separately --> not needed now 
//    	boolean edgedefault = false;
    	ArrayList<Long> rootList = new ArrayList<Long>();					//Used to save the IDs of the root nodes
    	Table edgeCache = prepareEdgeCache();
    	HashMap<String, String> attributeIdToName = new HashMap<String, String>();
    	
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
				
				//Check if a node is a root element or if an edge refers to a root element
				if(Tokens.EDGE.equals(reader.getLocalName()) && ROOT.equals(reader.getAttributeValue(null, Tokens.TARGET))) {
					//Adds the ID of the nodes referenced to root
					long rootID = Long.parseLong(reader.getAttributeValue(0).substring(1), 10);
					rootList.add(rootID);
				}
				//Read the data columns from the key elements
				else if(Tokens.KEY.equals(reader.getLocalName()))
					configReader(reader, tds, edgeCache, attributeIdToName);
				//add the ID to the hashMap
				else if(Tokens.NODE.equals(reader.getLocalName()))
					parseNode(reader, tds, attributeIdToName);
				//add an Edge to the EdgeList
				else if(Tokens.EDGE.equals(reader.getLocalName()))
				    parseEdge(reader, edgeCache, attributeIdToName);
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
				
			//When the element ends, call the appropriate method to add it to the TemporalDataset
			case XMLEvent.END_ELEMENT:
				if(Tokens.GRAPH.equals(reader.getLocalName()))
					oldElement = Tokens.GRAPH;
				break; // END_ELEMENT
				
			//When the file ends, the edges are created and the latest compatibility checks are being made
			case XMLEvent.END_DOCUMENT:
				createEdges(tds, edgeCache);	//add the edges to the TemporalDataset
				createRoots(tds, rootList);
				checkTemporalObjects(tds);		//Check if every TemporalObject has a TemporalElement it belongs to
				break; // END_DOCUMENT

			default:
				break;
			}	
		}
    }
    
    private void parseNode(XMLStreamReader reader, TemporalDataset tmpds,
            HashMap<String, String> attributeIdToName)
            throws XMLStreamException, DataIOException {

        String graphMLId = reader.getAttributeValue(null, Tokens.ID);
        // hashMap that temporarily stores the data of a node, gathers as the
        // necessary values to add it to the TMDS
        HashMap<String, String> dataMap = new HashMap<String, String>();

        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLEvent.START_ELEMENT:
                if (Tokens.DATA.equals(reader.getLocalName())) {
                    Pair<String, String> data = parseData(reader,
                            attributeIdToName);
                    dataMap.put(data.getKey(), data.getValue());
                }
                break;
            case XMLEvent.END_ELEMENT:
                if (Tokens.NODE.equals(reader.getLocalName())) {
                    // node parsing complete
                    // ignore root node
                    if (!ROOT.equals(graphMLId)) {
                        long id = Long.parseLong(graphMLId.substring(1));
                        if (NodeType.OBJECT == NodeType.byPrefix(graphMLId)) {
                            createTemporalObject(tmpds, id, dataMap);
                        } else if (NodeType.ELEMENT == NodeType
                                .byPrefix(graphMLId)) {
                            createTemporalElement(tmpds, id, dataMap);
                        }
                    }
                    return;
                }
            }
        }
        throw new IllegalStateException(
                "GraphML document ended prematurely in <node>.");
    }

    /**
     * reads XML content until the edge element ends and adds it to the cache.
     * @param reader
     * @param edgeCache
     * @param attributeIdToName
     * @throws XMLStreamException
     * @throws DataIOException
     */
    private void parseEdge(XMLStreamReader reader, Table edgeCache,
            HashMap<String, String> attributeIdToName)
            throws XMLStreamException, DataIOException {
        // we assume that reader is on a <edge> element
        Tuple edge = edgeCache.getTuple(edgeCache.addRow());
        // parse source and target from attributes -> cache
        edge.set(EDGE_SOURCE, reader.getAttributeValue(null, Tokens.SOURCE));
        edge.set(EDGE_TARGET, reader.getAttributeValue(null, Tokens.TARGET));

        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLEvent.START_ELEMENT:
                if (Tokens.DATA.equals(reader.getLocalName())) {
                    Pair<String, String> data = parseData(reader,
                            attributeIdToName);
                    if (edge.getColumnIndex(data.getKey()) >= 0) {
                        edge.setString(data.getKey(), data.getValue());
                    } else {
                        throw new DataIOException("Unknown attribute key "
                                + data.getKey() + " with value "
                                + data.getValue());
                    }
                }
                break;
            case XMLEvent.END_ELEMENT:
                if (Tokens.EDGE.equals(reader.getLocalName())) {
                    // edge parsing complete
                    return;
                }
            }
        }
        throw new IllegalStateException(
                "GraphML document ended prematurely in <edge>.");
    }

    private Pair<String, String> parseData(XMLStreamReader reader,
            HashMap<String, String> attributeIdToName)
            throws XMLStreamException, DataIOException {
        // we assume that reader is on a <data> element
        String key = reader.getAttributeValue(null, Tokens.KEY);
        if (key == null) {
            throw new DataIOException("<data> element without key.");
        }

        // build string from possibly multiple characters events
        StringBuilder value = new StringBuilder();
        // keep track of nested elements
        int depth = 0;

        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLEvent.CHARACTERS:
                if (depth == 0) {
                    value.append(reader.getText());
                }
                break;
            case XMLEvent.START_ELEMENT:
                depth++;
                break;
            case XMLEvent.END_ELEMENT:
                if (depth == 0 && Tokens.DATA.equals(reader.getLocalName())) {
                    // <data> parsing complete
                    String attName = attributeIdToName.get(key);
                    return new ImmutablePair<String, String>(attName,
                            value.toString());
                } else {
                    depth--;
                }
            }
        }

        throw new DataIOException(
                "GraphML document ended prematurely in <data>.");
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
    private void configReader(XMLStreamReader reader, TemporalDataset tds, Table edgeCache, HashMap<String, String> attributeIdToName) throws XMLStreamException, FactoryConfigurationError, IOException, TemporalDataException
    {
        final String attId = reader.getAttributeValue(null, Tokens.ID);
        final String attName = reader.getAttributeValue(null, Tokens.ATTRNAME);
        final String attFor = reader.getAttributeValue(null, Tokens.FOR);
        final String attType = reader.getAttributeValue(null, Tokens.ATTRTYPE);
        
        if (Logger.getLogger(this.getClass()).isDebugEnabled()) {
            Logger.getLogger(this.getClass()).debug(
                    "consider KEY for " + attFor + " column: " + attName);
        }
        
        // keep track of attribute ids
        attributeIdToName.put(attId, attName);
        
    	// ignore GraphML attributes with a reserved name (e.g. TemporalElement schema) since they are known
    	if(! attId.startsWith(TEMP_ELEMENT_ATTR_PREFIX)) {
			@SuppressWarnings("rawtypes")
            Class type = null;
		
			//get the defined data type as class object
			if(Tokens.STRING.equals(attType))
				type = String.class;
			else if(Tokens.INT.equals(attType))
				type = int.class;
			else if(Tokens.LONG.equals(attType))
				type = long.class;
			else if(Tokens.DOUBLE.equals(attType))
				type = double.class;
			else if(Tokens.FLOAT.equals(attType))
				type = float.class;
			else if(Tokens.BOOLEAN.equals(attType))
				type = boolean.class;
			
            //generate a new column for edges in the TemporalDataset
			if (Tokens.EDGE.equals(attFor) || Tokens.ALL.equals(attFor)) {
                tds.getEdgeTable().addColumn(attName, type, null);
                edgeCache.addColumn(attName, String.class, null);
			}
            //generate a new data column for the TemporalDataset
            if (Tokens.NODE.equals(attFor) || Tokens.ALL.equals(attFor)) {
                tds.addDataColumn(reader.getAttributeValue(null, Tokens.ATTRNAME), type, null);
            }
    	}
    }
    
    /**
     * Creates a TemporalElement with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalElement(TemporalDataset tds, long id, HashMap<String, String> dataMap) {
    	//Get all the know attributes of a TemporalElement 
		long inf = Long.parseLong(dataMap.get(GraphMLTemporalDatasetWriter.RESERVED_FIELD_PREFIX + TemporalElement.INF), 10);
		long sup = Long.parseLong(dataMap.get(GraphMLTemporalDatasetWriter.RESERVED_FIELD_PREFIX + TemporalElement.SUP), 10);
		int granularityID = Integer.parseInt(dataMap.get(GraphMLTemporalDatasetWriter.RESERVED_FIELD_PREFIX + TemporalElement.GRANULARITY_ID));
		int granularityContextID = Integer.parseInt(dataMap.get(GraphMLTemporalDatasetWriter.RESERVED_FIELD_PREFIX + TemporalElement.GRANULARITY_CONTEXT_ID));
		int kind = Integer.parseInt(dataMap.get(GraphMLTemporalDatasetWriter.RESERVED_FIELD_PREFIX + TemporalElement.KIND));
		
		//add a new TemporalElement to the TemporalDataset with these values
		tds.addTemporalElement(id, inf, sup, granularityID, granularityContextID, kind);
    }
    
    /**
     * Creates a TemporalObject with the Data given in the HashMap.
     * @param dataMap
     */
    private void createTemporalObject(TemporalDataset tds, long id, HashMap<String, String> dataMap) {
    	String currentCol = null;

		//Needs to be added as a node since the TemporalObject constructor needs the TemporalElement ID (information in edges)
    	Node node = tds.addNode();
		node.setLong(TemporalObject.ID, id);
		
		//for the amount of dataColumns in the TemporalDataset, set all their values
		for (int j = 0; j < tds.getDataColumnSchema().getColumnCount(); j++) {
			currentCol = tds.getDataColumnSchema().getColumnName(j);	//gets the current column name via the index
			
			String value = dataMap.get(currentCol);						//get the current value from the hashMap via the column name
			// TODO check if missing -> future work/not necessary now
			
			//Convert the value to the type defined for the column
            // XXX here we trust the string parsing to prefuse
			node.set(currentCol, value);
		}
    }
    
    /**
     * Adds the Edges from the edgeList to the TemporalDataset. (Use only after adding nodes!)
     * @param edgeList
     * @throws DataIOException 
     */
    private void createEdges(TemporalDataset tds, Table edgeCache) throws DataIOException {
        IntIterator edgeRows = edgeCache.rows();
        while (edgeRows.hasNext()) {
            Tuple edge = edgeCache.getTuple(edgeRows.nextInt());
            
            // extract types and TimeBench id from GraphML node id
            NodeType sType = NodeType.byPrefix(edge.getString(EDGE_SOURCE));
            NodeType tType = NodeType.byPrefix(edge.getString(EDGE_TARGET));
            long sId = Long.parseLong(edge.getString(EDGE_SOURCE).substring(1));
            long tId = Long.parseLong(edge.getString(EDGE_TARGET).substring(1));

            // Relationships between mutual types of nodes
            if (sType == tType) {
                if (NodeType.ELEMENT.equals(sType)) {
                    Node source = tds.getTemporalElement(sId);
                    Node target = tds.getTemporalElement(tId);
                    tds.getTemporalElements().addEdge(source, target);
                } else if (NodeType.OBJECT.equals(sType)) {
                    Node source = tds.getTemporalObject(sId);
                    Node target = tds.getTemporalObject(tId);
                    Edge tdEdge = tds.addEdge(source, target);

                    // the fields 0 and 1 contain source and target
                    // the other fields contain domain data
                    for (int i = 2; i < edge.getColumnCount(); i++) {
                        // XXX here we trust the string parsing to prefuse
                        tdEdge.set(edge.getColumnName(i), edge.getString(i));
                    }
                } else {
                    throw new DataIOException(
                            "Unappropriate node ids in edge: "
                                    + edge.getString(EDGE_SOURCE) + ", "
                                    + edge.getString(EDGE_TARGET));
                }
                // TemporalElements needed for the TemporalObject
            } else if (NodeType.OBJECT.equals(sType)
                    && NodeType.ELEMENT.equals(tType)) {
                // get the object via the source ID and set the element ID
                Node node = tds.getTemporalObject(sId);
                node.set(TemporalObject.TEMPORAL_ELEMENT_ID, tId);
            } else {
                throw new DataIOException("Unappropriate node ids in edge: "
                        + edge.getString(EDGE_SOURCE) + ", "
                        + edge.getString(EDGE_TARGET));
            }
        }
    }
    
    private void createRoots(TemporalDataset tds, ArrayList<Long> rootList) throws DataIOException {
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
    private void checkTemporalObjects(TemporalDataset tds) throws TemporalDataException {
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
    
    private Table prepareEdgeCache() {
        Table cache = new Table();
        cache.addColumn(EDGE_SOURCE, String.class);
        cache.addColumn(EDGE_TARGET, String.class);
        
        return cache;
    }
}
