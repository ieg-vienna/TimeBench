package timeBench.data.io;

import static prefuse.data.io.GraphMLWriter.Tokens.*;

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.util.collections.IntIterator;
import timeBench.data.relational.GenericTemporalElement;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.relational.TemporalObject;

/**
 * 
 * <p>
 * The class is not thread-safe. Each thread should use its own instance.
 * 
 * @author Rind
 * 
 */
public class GraphMLTemporalDatasetWriter extends AbstractTemporalDatasetWriter {

    public static final String OBJECT_PREFIX = "o";
    public static final String ELEMENT_PREFIX = "t";

    private TransformerHandler hd = null;
    private AttributesImpl atts = null;

    @Override
    public void writeData(TemporalDataset tmpds, OutputStream os)
            throws DataIOException {
        try {
            // setup SAX to identity transform events to a stream
            // based on
            // http://www.javazoom.net/services/newsletter/xmlgeneration.html
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                    .newInstance();
            hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            hd.setResult(new StreamResult(os));

            atts = new AttributesImpl();

            hd.startDocument();
            hd.startElement("", "", GRAPHML, atts);
            // TODO insert schema

            atts.clear();
            atts.addAttribute("", "", ID, "CDATA", "temporalData");
            atts.addAttribute("", "", EDGEDEF, "CDATA", DIRECTED);
            hd.startElement("", "", GRAPH, atts);

            // insert nodes
            writeTemporalElements(tmpds);
            writeTemporalObjects(tmpds);

            // insert edges
            // attribute object is not cleared to improve performance
            atts.clear();
            atts.addAttribute("", "", SOURCE, "CDATA", "");
            atts.addAttribute("", "", TARGET, "CDATA", "");
            writeBipartiteEdges(tmpds);
            writeGraphEdges(tmpds, TemporalDataset.TEMPORAL_OBJECT_ID,
                    OBJECT_PREFIX);
            writeGraphEdges(tmpds.getTemporalElements(),
                    TemporalDataset.TEMPORAL_ELEMENT_ID, ELEMENT_PREFIX);

            hd.endElement("", "", GRAPH);
            hd.endElement("", "", GRAPHML);

            hd.endDocument();
        } catch (TransformerConfigurationException e) {
            throw new DataIOException(e);
        } catch (SAXException e) {
            throw new DataIOException(e);
        } finally {
            hd = null;
            atts = null;
        }
    }

    private void writeTemporalElements(TemporalDataset tmpds)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        Iterator<GenericTemporalElement> iterator = tmpds.temporalElements();
        while (iterator.hasNext()) {
            GenericTemporalElement te = iterator.next();
            atts.clear();
            atts.addAttribute("", "", ID, "CDATA", ELEMENT_PREFIX + te.getId());
            hd.startElement("", "", NODE, atts);
            // TODO insert data
            // hd.characters(desc[i].toCharArray(), 0, desc[i].length());
            hd.endElement("", "", NODE);
        }
    }

    private void writeTemporalObjects(TemporalDataset tmpds)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        Iterator<TemporalObject> iterator = tmpds.temporalObjects();
        while (iterator.hasNext()) {
            TemporalObject tObj = iterator.next();
            atts.clear();
            atts.addAttribute("", "", ID, "CDATA", OBJECT_PREFIX + tObj.getId());
            hd.startElement("", "", NODE, atts);
            // TODO insert data
            hd.endElement("", "", NODE);
        }
    }

    private void writeBipartiteEdges(TemporalDataset tmpds) throws SAXException {
        Iterator<TemporalObject> iterator = tmpds.temporalObjects();
        while (iterator.hasNext()) {
            TemporalObject tObj = iterator.next();
            long objId = tObj.getId();
            long elId = tObj.getTemporalElement().getId();

            writeEdge(ELEMENT_PREFIX + elId, OBJECT_PREFIX + objId);
        }
    }

    private void writeGraphEdges(Graph graph, String idField, String prefix)
            throws SAXException {
        IntIterator edges = graph.edgeRows();
        while (edges.hasNext()) {
            int edgeRow = edges.nextInt();
            int sRow = graph.getSourceNode(edgeRow);
            long sId = graph.getNodeTable().getLong(sRow, idField);
            int tRow = graph.getTargetNode(edgeRow);
            long tId = graph.getNodeTable().getLong(tRow, idField);

            writeEdge(prefix + sId, prefix + tId);
        }
    }

    private void writeEdge(String source, String target) throws SAXException {
        // we assume that attribute object has correct structure (performance)

        // atts.clear();
        // atts.addAttribute("", "", SOURCE, "CDATA", source);
        // atts.addAttribute("", "", TARGET, "CDATA", target);
        atts.setValue(0, source);
        atts.setValue(1, target);
        hd.startElement("", "", EDGE, atts);
        hd.endElement("", "", EDGE);
    }
}
