package org.timebench.data.io;

import static prefuse.data.io.GraphMLWriter.Tokens;
import static prefuse.data.io.GraphMLWriter.TYPES;

import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.timebench.data.GenericTemporalElement;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLWriter;
import prefuse.util.collections.IntIterator;

/**
 * writes a {@link TemporalDataset} to XML using the GraphML language. It is
 * implemented with SAX using JAXP.
 * 
 * TODO calendar
 * TODO handle default values of data columns
 * 
 * <p>
 * GraphML only supports attributes of primitive types and string. Only
 * attributes of temporal objects are stored, i.e. the attributes of edges
 * between temporal objects are ignored.
 * 
 * <p>
 * The class is not thread-safe. Each thread should use its own instance.
 * 
 * @author Rind
 * 
 * @see {@linkplain http://graphml.graphdrawing.org/primer/graphml-primer.html}
 */
public class GraphMLTemporalDatasetWriter extends AbstractTemporalDatasetWriter {

    public static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    public static final String OBJECT_PREFIX = "o";
    public static final String ELEMENT_PREFIX = "t";
    public static final String ROOT = "root";
    public static final String EDGE_DATA_PREFIX = "edge-";
    public static final String RESERVED_FIELD_PREFIX = "_";

    private TransformerHandler hd = null;

    @Override
    public void writeData(TemporalDataset tmpds, OutputStream os)
            throws DataIOException {
        writeData(tmpds, new StreamResult(os));
    }

    /**
     * Write a {@link TemporalDataset} to a JAXP {@link Result}.
     * 
     * @param tmpds
     *            the {@link TemporalDataset} to write
     * @param result
     *            the JAXP result to write the temporal dataset to
     * @throws DataIOException
     */
    public void writeData(TemporalDataset tmpds, Result result)
            throws DataIOException {
        try {
            // first, check the schema to ensure GraphML compatibility
            GraphMLWriter.checkGraphMLSchema(tmpds.getDataColumnSchema());
            GraphMLWriter.checkGraphMLSchema(tmpds.getEdgeTable().getSchema());

            // setup SAX to identity transform events to a stream; cp.
            // http://www.javazoom.net/services/newsletter/xmlgeneration.html
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                    .newInstance();
            hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            hd.setResult(result);

            // check XML namespace support -- did not work??
            // System.err.println(tf.getFeature("http://xml.org/sax/features/namespaces"));
            // System.err.println(tf.getFeature("http://xml.org/sax/features/namespace-prefixes"));

            // *** prologue ***
            hd.startDocument();
            // <graphml ...
            hd.startPrefixMapping("", GRAPHML_NS);
            hd.startPrefixMapping("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance");
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(
                    "http://www.w3.org/2001/XMLSchema-instance",
                    "schemaLocation",
                    "xsi:schemaLocation",
                    "CDATA",
                    GRAPHML_NS
                            + " http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
            hd.startElement(GRAPHML_NS, Tokens.GRAPHML, Tokens.GRAPHML, atts);
            // <key ... elements
            writeAttributeSchema(tmpds);
            // <graph ...
            atts.clear();
            atts.addAttribute(GRAPHML_NS, Tokens.ID, Tokens.ID, "CDATA",
                    "temporalData");
            atts.addAttribute(GRAPHML_NS, Tokens.EDGEDEF, Tokens.EDGEDEF,
                    "CDATA", Tokens.DIRECTED);
            hd.startElement(GRAPHML_NS, Tokens.GRAPH, Tokens.GRAPH, atts);

            // *** nodes ***
            AttributesImpl nodeAtts = new AttributesImpl();
            nodeAtts.addAttribute(GRAPHML_NS, Tokens.ID, Tokens.ID, "CDATA", "");
            AttributesImpl dataAtts = new AttributesImpl();
            dataAtts.addAttribute(GRAPHML_NS, Tokens.KEY, Tokens.KEY, "CDATA",
                    "");
            writeTemporalElements(tmpds, nodeAtts, dataAtts);
            writeTemporalObjects(tmpds, nodeAtts, dataAtts);

            // *** edges ***
            AttributesImpl edgeAtts = new AttributesImpl();
            edgeAtts.addAttribute(GRAPHML_NS, Tokens.SOURCE, Tokens.SOURCE,
                    "CDATA", "");
            edgeAtts.addAttribute(GRAPHML_NS, Tokens.TARGET, Tokens.TARGET,
                    "CDATA", "");
            writeBipartiteEdges(tmpds, edgeAtts);
            writeGraphEdgesWithData(tmpds, TemporalObject.ID,
                    OBJECT_PREFIX, edgeAtts, dataAtts);
            writeGraphEdges(tmpds.getTemporalElements(),
                    TemporalElement.ID, ELEMENT_PREFIX,
                    edgeAtts);
            
            // *** root objects ***
            writeRoots(tmpds, nodeAtts, edgeAtts);

            // *** epilogue ***
            hd.endElement(GRAPHML_NS, Tokens.GRAPH, Tokens.GRAPH);
            hd.endElement(GRAPHML_NS, Tokens.GRAPHML, Tokens.GRAPHML);
            hd.endPrefixMapping("");
            hd.endPrefixMapping("xsi");
            hd.endDocument();
        } catch (TransformerConfigurationException e) {
            throw new DataIOException(e);
        } catch (SAXException e) {
            throw new DataIOException(e);
        } finally {
            hd = null;
        }
    }

    // ----- middle abstraction layer: write TimeBench data structures -----

    /**
     * generate key elements needed for this temporal dataset.
     * 
     * @param tmpds
     * @throws SAXException
     */
    private void writeAttributeSchema(TemporalDataset tmpds)
            throws SAXException {
        AttributesImpl keyAtts = new AttributesImpl();
        keyAtts.addAttribute(GRAPHML_NS, Tokens.ID, Tokens.ID, "CDATA", "");
        keyAtts.addAttribute(GRAPHML_NS, Tokens.ATTRNAME, Tokens.ATTRNAME,
                "CDATA", "");
        keyAtts.addAttribute(GRAPHML_NS, Tokens.ATTRTYPE, Tokens.ATTRTYPE,
                "CDATA", "");
        keyAtts.addAttribute(GRAPHML_NS, Tokens.FOR, Tokens.FOR, "CDATA",
                "node");

        // key elements for predefined field values
        writeGraphMLKey(RESERVED_FIELD_PREFIX + TemporalElement.INF,
                long.class, keyAtts);
        writeGraphMLKey(RESERVED_FIELD_PREFIX + TemporalElement.SUP,
                long.class, keyAtts);
        writeGraphMLKey(RESERVED_FIELD_PREFIX + TemporalElement.GRANULARITY_ID,
                int.class, keyAtts);
        writeGraphMLKey(RESERVED_FIELD_PREFIX
                + TemporalElement.GRANULARITY_CONTEXT_ID, int.class, keyAtts);
        writeGraphMLKey(RESERVED_FIELD_PREFIX + TemporalElement.KIND,
                int.class, keyAtts);

        // key elements for application-specific field values
        // XXX consider getDataColumnSchema() -- Bug with depth & MuTIny
        Schema dataElements = tmpds.getNodeTable().getSchema(); 
        for (int i = 0; i < dataElements.getColumnCount(); i++) {
            String name = dataElements.getColumnName(i);
            // column names starting with "_" are reserved
            if (! name.startsWith(RESERVED_FIELD_PREFIX)) {
                writeGraphMLKey(name, dataElements.getColumnType(i), keyAtts);
            }
        }
        
        Schema edgeElements = tmpds.getEdgeTable().getSchema(); 
        keyAtts.setValue(3, "edge");
        for (int i = 0; i < edgeElements.getColumnCount(); i++) {
            String name = edgeElements.getColumnName(i);
            if (! (name.equals(tmpds.getEdgeSourceField()) || name.equals(tmpds.getEdgeTargetField()))) {
                writeGraphMLKey(EDGE_DATA_PREFIX+name, name, edgeElements.getColumnType(i), keyAtts);
            }
        }

    }

    /**
     * generate node elements for all temporal elements in the temporal dataset.
     * 
     * @param tmpds
     * @param nodeAtts
     * @param dataAtts
     * @throws SAXException
     */
    private void writeTemporalElements(TemporalDataset tmpds,
            AttributesImpl nodeAtts, AttributesImpl dataAtts)
            throws SAXException {
        for (GenericTemporalElement te : tmpds.temporalElements()) {
            nodeAtts.setValue(0, ELEMENT_PREFIX + te.getId());
            hd.startElement(GRAPHML_NS, Tokens.NODE, Tokens.NODE, nodeAtts);

            // data elements for predefined field values
            writeGraphMLData(RESERVED_FIELD_PREFIX + TemporalElement.INF,
                    Long.toString(te.getInf()), dataAtts);
            writeGraphMLData(RESERVED_FIELD_PREFIX + TemporalElement.SUP,
                    Long.toString(te.getSup()), dataAtts);
            writeGraphMLData(RESERVED_FIELD_PREFIX
                    + TemporalElement.GRANULARITY_ID,
                    Integer.toString(te.getGranularityId()), dataAtts);
            writeGraphMLData(RESERVED_FIELD_PREFIX
                    + TemporalElement.GRANULARITY_CONTEXT_ID,
                    Integer.toString(te.getGranularityContextId()), dataAtts);
            writeGraphMLData(RESERVED_FIELD_PREFIX + TemporalElement.KIND,
                    Integer.toString(te.getKind()), dataAtts);

            hd.endElement(GRAPHML_NS, Tokens.NODE, Tokens.NODE);
        }
    }

    /**
     * generate node elements for all temporal objects in the temporal dataset.
     * 
     * @param tmpds
     * @param nodeAtts
     * @param dataAtts
     * @throws SAXException
     */
    private void writeTemporalObjects(TemporalDataset tmpds,
            AttributesImpl nodeAtts, AttributesImpl dataAtts)
            throws SAXException {
        for (TemporalObject tObj : tmpds.temporalObjects()) { 
            nodeAtts.setValue(0, OBJECT_PREFIX + tObj.getId());
            hd.startElement("", Tokens.NODE, Tokens.NODE, nodeAtts);

            // data elements for application-specific field values
            // XXX consider getDataColumnSchema() -- Bug with depth & MuTIny
            for (int i = 0; i < tObj.getColumnCount(); i++) {
                String name = tObj.getColumnName(i);
                // predefined columns are handled differently
                if (! name.startsWith(RESERVED_FIELD_PREFIX)) {
                    writeGraphMLData(name, tObj.getString(i), dataAtts);
                }
            }

            hd.endElement(GRAPHML_NS, Tokens.NODE, Tokens.NODE);
        }
    }

    /**
     * generate edge elements for all temporal object &ndash; temporal element
     * relationships.
     * 
     * @param tmpds
     * @param edgeAtts
     * @throws SAXException
     */
    private void writeBipartiteEdges(TemporalDataset tmpds,
            AttributesImpl edgeAtts) throws SAXException {
        for (TemporalObject tObj : tmpds.temporalObjects()) { 
            long objId = tObj.getId();
            long elId = tObj.getTemporalElement().getId();

            writeGraphMLEdge(OBJECT_PREFIX + objId, ELEMENT_PREFIX + elId,
                    edgeAtts);
        }
    }
    
    /**
     * generate edge elements for all edges in a graph.
     * 
     * @param graph
     * @param idField
     *            field name of the node identifier
     * @param prefix
     *            prefix for the identifier in GraphML
     * @param edgeAtts
     * @throws SAXException
     */
    private void writeGraphEdges(Graph graph, String idField, String prefix,
            AttributesImpl edgeAtts) throws SAXException {
        IntIterator edges = graph.edgeRows();
        while (edges.hasNext()) {
            int edgeRow = edges.nextInt();
            int sRow = graph.getSourceNode(edgeRow);
            long sId = graph.getNodeTable().getLong(sRow, idField);
            int tRow = graph.getTargetNode(edgeRow);
            long tId = graph.getNodeTable().getLong(tRow, idField);

            writeGraphMLEdge(prefix + sId, prefix + tId, edgeAtts);
        }
    }

    private void writeGraphEdgesWithData(Graph graph, String idField, String prefix,
            AttributesImpl edgeAtts, AttributesImpl dataAtts) throws SAXException {
        IntIterator edges = graph.edgeRows();
        while (edges.hasNext()) {
            int edgeRow = edges.nextInt();
            int sRow = graph.getSourceNode(edgeRow);
            long sId = graph.getNodeTable().getLong(sRow, idField);
            int tRow = graph.getTargetNode(edgeRow);
            long tId = graph.getNodeTable().getLong(tRow, idField);

            edgeAtts.setValue(0, prefix + sId);
            edgeAtts.setValue(1, prefix + tId);
            hd.startElement(GRAPHML_NS, Tokens.EDGE, Tokens.EDGE, edgeAtts);
            
            // data elements for application-specific field values
            Schema edgeSchema = graph.getEdgeTable().getSchema(); 
            for (int i = 0; i < edgeSchema.getColumnCount(); i++) {
                String name = edgeSchema.getColumnName(i);
                // columns for source and target nodes are excluded 
                if (! (name.equals(graph.getEdgeSourceField()) || name.equals(graph.getEdgeTargetField()))) {
                    writeGraphMLData(EDGE_DATA_PREFIX+name, graph.getEdgeTable().getString(edgeRow, name), dataAtts);
                }
            }
            
            hd.endElement(GRAPHML_NS, Tokens.EDGE, Tokens.EDGE);
        }
    }
    
    /**
     * generate a fake node that has edges from all root objects.
     * 
     * @param tmpds
     * @param nodeAtts
     * @param edgeAtts
     * @throws SAXException
     */
    private void writeRoots(TemporalDataset tmpds, AttributesImpl nodeAtts,
            AttributesImpl edgeAtts) throws SAXException {
        if (tmpds.getRootCount() > 0) {
            nodeAtts.setValue(0, ROOT);
            hd.startElement("", Tokens.NODE, Tokens.NODE, nodeAtts);
            hd.endElement(GRAPHML_NS, Tokens.NODE, Tokens.NODE);

            for (TemporalObject obj : tmpds.roots()) {
                writeGraphMLEdge(OBJECT_PREFIX + obj.getId(), ROOT, edgeAtts);
            }
        }
    }

    // ----- lowest abstraction layer: write atomic GraphML elements -----

    /**
     * generate a GraphML key element.
     * 
     * @param id
     * @param type
     * @param keyAtts
     * @throws SAXException
     */
    private void writeGraphMLKey(String id,
            @SuppressWarnings("rawtypes") Class type, AttributesImpl keyAtts)
            throws SAXException {
        writeGraphMLKey(id, id, type, keyAtts);
    }
    
    private void writeGraphMLKey(String id, String name,
            @SuppressWarnings("rawtypes") Class type, AttributesImpl keyAtts)
            throws SAXException {
        keyAtts.setValue(0, id);
        keyAtts.setValue(1, name);
        keyAtts.setValue(2, (String) TYPES.get(type));
        hd.startElement(GRAPHML_NS, Tokens.KEY, Tokens.KEY, keyAtts);
        hd.endElement(GRAPHML_NS, Tokens.KEY, Tokens.KEY);
    }

    /**
     * generate a GraphML data element.
     * 
     * @param key
     * @param value
     * @param dataAtts
     * @throws SAXException
     */
    private void writeGraphMLData(String key, String value,
            AttributesImpl dataAtts) throws SAXException {
        dataAtts.setValue(0, key);
        hd.startElement(GRAPHML_NS, Tokens.DATA, Tokens.DATA, dataAtts);
        hd.characters(value.toCharArray(), 0, value.length());
        hd.endElement(GRAPHML_NS, Tokens.DATA, Tokens.DATA);
    }

    /**
     * generate a GraphML edge element.
     * 
     * @param source
     * @param target
     * @param edgeAtts
     * @throws SAXException
     */
    private void writeGraphMLEdge(String source, String target,
            AttributesImpl edgeAtts) throws SAXException {
        // we assume that attribute object has correct structure (performance)
        // atts.clear();
        // atts.addAttribute("", "", SOURCE, "CDATA", source);
        // atts.addAttribute("", "", TARGET, "CDATA", target);
        edgeAtts.setValue(0, source);
        edgeAtts.setValue(1, target);
        hd.startElement(GRAPHML_NS, Tokens.EDGE, Tokens.EDGE, edgeAtts);
        hd.endElement(GRAPHML_NS, Tokens.EDGE, Tokens.EDGE);
    }
}
