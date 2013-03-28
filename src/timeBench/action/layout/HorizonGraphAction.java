package timeBench.action.layout;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import prefuse.action.Action;
import prefuse.data.Node;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NotPredicate;
import prefuse.data.expression.ObjectLiteral;
import prefuse.data.expression.Predicate;
import prefuse.util.DataLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import timeBench.action.analytical.IndexingAction;
import timeBench.action.analytical.TemporalDataIndexingAction;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.GenericTemporalElement;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.data.expression.GranularityPredicate;
import timeBench.ui.HorizonSettings;

/**
 * Complex action for creating a horizon graph as polygons. Actually two new
 * groups are created: a table for vertexes in the polygon and an aggregate
 * table for the polygons.
 * 
 * <p>
 * This action should be decomposed into smaller units.
 * 
 * @author Rind (based on work by Atanasov and Schindler)
 */
public class HorizonGraphAction extends Action {

    public static final String COL_INDEXED_VALUE = "indexedValue";
    private static final String COL_BAND_ID = "band_id";
    public static final String COL_IS_HELP_NODE = "help_node";
    public static final String COL_Y_POSITION = "y_value";
    public static final String COL_COLOR_INDEX = null;

    public static final int MAX_BANDS = 5;
    public static final int BAND_ID_POSITIVE_EXTREME_VALUES = MAX_BANDS + 1;
    public static final int BAND_ID_NEGATIVE_EXTREME_VALUES = -MAX_BANDS - 1;
    public static final int BAND_ID_NULL_VALUES = 0;

    private String groupData;
    private String groupControlPoints = "groupHorizonPoints";
    private String groupBands = "groupHorizonBands";

    private String fieldVariable;

    /**
     * assumed to be double
     */
    private String fieldValue;

    private TemporalDataIndexingAction indexing;

    private HorizonSettings settings;

    public IndexingAction getIndexing() {
        return indexing;
    }

    public HorizonGraphAction(String groupData, String groupControlPoints,
            String groupBands, String fieldVariable, String fieldValue, HorizonSettings settings) {
        super();
        this.groupData = groupData;
        this.groupControlPoints = groupControlPoints;
        this.groupBands = groupBands;
        this.fieldVariable = fieldVariable;
        this.fieldValue = fieldValue;
        this.settings = settings;

        indexing = new TemporalDataIndexingAction(groupControlPoints,
                fieldValue, COL_Y_POSITION, fieldVariable);
    }

    @Override
    public void run(double frac) {
        // create tuple set for control points
        TemporalDataset tmpds = (TemporalDataset) m_vis
                .getSourceData(groupData);
        TemporalDataset ctrlPts = new TemporalDataset(
                tmpds.getTemporalElements());
        initControlPointSchema(tmpds, ctrlPts);

        // pre-fill control points with original points
        for (GenericTemporalElement el : tmpds
                .getTemporalElements()
                .temporalElements(
                        new NotPredicate(
                                new GranularityPredicate(
                                        JavaDateCalendarManager.Granularities.Millisecond
                                                .toInt())))) {
            // translate time primitives to instant in center
            long midTime = (el.getInf() + el.getSup()) / 2;
            Instant newEl = el.getTemporalElementStore().addInstant(midTime,
                    midTime,
                    JavaDateCalendarManager.Granularities.Millisecond.toInt(),
                    JavaDateCalendarManager.Granularities.Top.toInt());

            for (TemporalObject obj : el.temporalObjects(tmpds)) {
                TemporalObject cp = ctrlPts.addTemporalObject(newEl);
                // copy raw data
                cp.set(fieldVariable, obj.get(fieldVariable));
                cp.set(fieldValue, obj.get(fieldValue));
            }

        }

        // TODO handle missing values
        // TODO transform step chart
        // TODO transform base

        // indexing
        indexing.setVisualization(m_vis);
        indexing.setIndexedPoint((VisualItem) m_vis
                .getVisualGroup(groupControlPoints).tuples().next());
        indexing.run(frac);

        // create helper points
        Object[] vars = DataLib.ordinalArray(ctrlPts.getNodes(), fieldVariable);
        for (Object variable : vars) {
            System.out.println(variable);
            addHelpNodes(ctrlPts, variable, settings.BandsCount, settings.getBandWidth());
        }

        // shift bands over each other
        joinBands(ctrlPts, settings.BandsCount, settings.getBandWidth());
        // mirror
        applyMirrorTransformation(ctrlPts);
        // TODO offset

        // arrange variables in separate areas
        double offset = 0;
        for (Object variable : vars) {
            Predicate predVar = new ComparisonPredicate(ComparisonPredicate.EQ,
                    new ColumnExpression(fieldVariable), new ObjectLiteral(
                            variable));

            for (TemporalObject newNode : ctrlPts.temporalObjects(predVar)) {
                newNode.setDouble(COL_Y_POSITION,
                        newNode.getDouble(COL_Y_POSITION) + offset);
            }

            offset -= settings.getBandWidth() * 11.0 / 10.0; // factor of 0.1 space between
                                               // two parameters
            // ATTENTION: HorizonAxisLabel needs to be changed if the factor is
            // changed here
        }

        // create aggregate tuple set for bands & add items to bands
        initBands(vars);
    }

    private void initControlPointSchema(TemporalDataset tmpds,
            TemporalDataset ctrlPts) {
        try {
            ctrlPts.addDataColumn(fieldVariable, String.class, null);
            ctrlPts.addDataColumn(fieldValue, double.class, Double.NaN);
            ctrlPts.addDataColumn(COL_INDEXED_VALUE, double.class, Double.NaN);
            ctrlPts.addDataColumn(COL_Y_POSITION, double.class, Double.NaN);
            ctrlPts.addDataColumn(COL_IS_HELP_NODE, boolean.class, false);
            ctrlPts.addDataColumn(COL_BAND_ID, int.class, Integer.MIN_VALUE);
            ctrlPts.getNodeTable().index(fieldVariable);

            m_vis.removeGroup(groupControlPoints);
            m_vis.add(groupControlPoints, ctrlPts);

        } catch (TemporalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Calls addHelpNodesInBand(...) for each normal band. This results in
     * adding all nodes, which are needed to draw the different colored
     * polygons, to the graph. Extreme value and null value ranges are not
     * handled by the method.
     * 
     * @param graph
     *            the graph instance, where the help node should be added
     * @param table
     *            the data table, from which the help node positions are
     *            calculated
     * @param bandsCount
     *            the count of bands, e.g. _settings.BandsCount
     * @param bandWidth
     *            the width of a single band, e.g. _settings.getBandWidth()
     */
    private void addHelpNodes(TemporalDataset ctrlPts, Object variable,
            int bandsCount, double bandWidth) {
        for (int bandId = 1; bandId <= bandsCount; bandId++) {
            double upperBound = bandWidth * bandId, lowerBound = upperBound
                    - bandWidth, negativeUpperBound = -lowerBound, negativeLowerBound = -upperBound;

            addHelpNodesInBand(ctrlPts, variable, bandId, lowerBound,
                    upperBound);
            addHelpNodesInBand(ctrlPts, variable, -bandId, negativeLowerBound,
                    negativeUpperBound);
        }
    }

    /**
     * Calls getPointsInBand(...) to determinate the help node positions for the
     * specified band and adds the nodes to the graph instance.
     * 
     * @param graph
     *            the graph instance, where the help node should be added
     * @param table
     *            the data table, from which the help node positions are
     *            calculated
     * @param bandId
     *            the current band id
     * @param lowerBound
     *            the current lower value bound of the band
     * @param upperBound
     *            the current upper value bound of the band
     */
    private void addHelpNodesInBand(TemporalDataset ctrlPts, Object variable,
            int bandId, double lowerBound, double upperBound) {

        List<TimeValuePair> pointsInBand = getPointsInBand(ctrlPts, variable,
                lowerBound, upperBound);

        for (TimeValuePair point : pointsInBand) {
            addHelpNode(ctrlPts, variable, bandId, point.time, point.value);
        }
    }

    /**
     * adds a single help node to the graph
     * 
     * @param graph
     *            the graph instance, where the tuple should be added
     * @param bandId
     *            the id of the band the help node should belong to
     * @param x
     *            the x coordinate of the help node
     * @param y
     *            the y coordinate of the help node
     * @return the added a node instance
     */
    private Node addHelpNode(TemporalDataset ctrlPts, Object variable,
            int bandId, long time, double y) {
        TemporalElement el = null;
        // search an identical temporal element
        // Iterator<GenericTemporalElement> iter = ctrlPts.temporalElements(new
        // ComparisonPredicate(ComparisonPredicate.EQ, new
        // ColumnExpression(TemporalElement.INF), new
        // NumericLiteral(time))).iterator();
        // while (iter.hasNext()) {
        // GenericTemporalElement curr = iter.next();
        // if (curr.getSup() == time) {
        // el = curr;
        // break;
        // }
        // }
        // otherwise make a new one
        if (el == null) {
            el = ctrlPts.addInstant(time, time,
                    JavaDateCalendarManager.Granularities.Millisecond.toInt(),
                    JavaDateCalendarManager.Granularities.Top.toInt());
        }

        TemporalObject node = ctrlPts.addTemporalObject(el);
        node.set(fieldVariable, variable);
        node.setDouble(COL_Y_POSITION, y);
        node.setBoolean(COL_IS_HELP_NODE, true);
        node.setInt(COL_BAND_ID, bandId);
        // node.setDate(HorizonVisualization.COL_X_TIMESTAMP, new Date(new
        // Double(x).longValue()));
        // node.setString(HorizonVisualization.COL_Y_FIELD_VALUE, "");

        return node;
    }

    /**
     * Calculates all points (including helper points) between the specified
     * bounds. The points later make up a single polygon, which is rendered.
     * 
     * @param variable
     * @param table
     *            the data table, from which the points are calculated
     * @param lowerBound
     *            the current lower value bound of the band
     * @param upperBound
     *            the current upper value bound of the band
     * @return a list of the calculated points
     */
    private List<TimeValuePair> getPointsInBand(TemporalDataset ctrlPts,
            Object variable, double lowerBound, double upperBound) {
        double boundNearBase = lowerBound < 0 ? upperBound : lowerBound;

        List<TimeValuePair> pointsInBand = new Vector<TimeValuePair>();

        Predicate predVar = new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(fieldVariable),
                new ObjectLiteral(variable));
        Predicate predReal = new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(COL_IS_HELP_NODE), new BooleanLiteral(
                        false));
        Iterator<TemporalObject> rows = ctrlPts.temporalObjects(
                new AndPredicate(predVar, predReal)).iterator();

        if (!rows.hasNext())
            return pointsInBand;

        TimeValuePair p1 = new TimeValuePair(ctrlPts.getInf(), boundNearBase);

        // TemporalObject t =
        // ctrlPts.getTemporalElementByRow(getTuple(rows.nextInt()); rows =
        // table.rows();
        TemporalObject t;

        boolean finished = false;
        while (!finished) {
            // add the current point if it is between the bounds
            if (lowerBound <= p1.value && p1.value <= upperBound)
                pointsInBand.add(p1);

            // get the next point
            TimeValuePair p2;
            if (rows.hasNext()) {
                t = rows.next();
                p2 = new TimeValuePair(t.getTemporalElement().getInf(),
                        t.getDouble(COL_Y_POSITION));
            } else {
                finished = true;
                p2 = new TimeValuePair(p1.time, boundNearBase);
            }

            TimeValuePair min = p1.value < p2.value ? p1 : p2;
            TimeValuePair max = p1.value < p2.value ? p2 : p1;

            TimeValuePair pn1 = null, pn2 = null;

            // calculate possible points of intersection
            if (min.value < lowerBound && max.value > lowerBound) {
                double hx = (lowerBound - p1.value)
                        / ((p2.value - p1.value) / (p2.time - p1.time))
                        + p1.time;
                pn1 = new TimeValuePair(Math.round(hx), lowerBound);
            }
            if (min.value < upperBound && max.value > upperBound) {
                double hx = (upperBound - p1.value)
                        / ((p2.value - p1.value) / (p2.time - p1.time))
                        + p1.time;
                pn2 = new TimeValuePair(Math.round(hx), upperBound);
            }

            if (min != p1) // restore order
            {
                TimeValuePair h = pn1;
                pn1 = pn2;
                pn2 = h;
            }

            // add the intersection point if not null
            if (pn1 != null)
                pointsInBand.add(pn1);
            if (pn2 != null)
                pointsInBand.add(pn2);

            p1 = p2;
        }

        pointsInBand.add(p1);

        for (TimeValuePair d : pointsInBand)
            if (d.value != boundNearBase)
                return pointsInBand;

        return new Vector<TimeValuePair>();
    }

    static class TimeValuePair {
        long time;
        double value;

        public TimeValuePair(long time, double value) {
            this.time = time;
            this.value = value;
        }
    }

    /**
     * Performs the band join operation. After joining the bands, the different
     * colored polygons will be rendered on top of each other. The polygons for
     * extreme value bands or null value bands are drawn on top.
     * 
     * @param graph
     *            the graph instance
     * @param bandsCount
     *            the count of bands, e.g. _settings.BandsCount
     * @param bandWidth
     *            the width of a single band, e.g. _settings.getBandWidth()
     */
    private void joinBands(TemporalDataset graph, int bandsCount,
            double bandWidth) {
        for (TemporalObject node : graph.temporalObjects()) {

            double y = node.getDouble(COL_Y_POSITION);
            double sign = Math.signum(y);

            if (node.getBoolean(COL_IS_HELP_NODE)) {
                int bandId = node.getInt(COL_BAND_ID);

                // filter extreme values (bandId > bandsCount) and missing
                // values (bandId == 0)
                if (Math.abs(bandId) <= bandsCount && bandId != 0)
                    y -= ((Math.abs(bandId) - 1) * bandWidth) * sign; // join
                                                                      // the
                                                                      // bands
            } else {
                if (Math.abs(y) >= bandWidth * bandsCount)
                    y = sign * bandWidth;
                else
                    while (Math.abs(y) > bandWidth)
                        y -= bandWidth * sign;
            }

            node.setDouble(COL_Y_POSITION, y);
        }
    }

    /**
     * Performs the mirror transformation for negative values.
     * 
     * @param graph
     *            the graph instance
     */
    private void applyMirrorTransformation(TemporalDataset graph) {
        for (TemporalObject node : graph.temporalObjects()) {

            double y = node.getDouble(COL_Y_POSITION);
            y = Math.abs(y);

            node.setDouble(COL_Y_POSITION, y);
        }
    }

    /**
     * Initializes the colored polygon (called aggregates).
     * 
     * @param visualGraph
     *            the visualGraph used by the visualization
     * @param bandsCount
     *            the count of bands, e.g. _settings.BandsCount
     * @param variableSet
     *            a set of the different variables (e.g. { "insulin-dose",
     *            "glucose" })
     */
    private void initBands(Object[] variableSet) {
        m_vis.removeGroup(groupBands);
        AggregateTable aggregates = m_vis.addAggregates(groupBands);

        aggregates.addColumn(VisualItem.POLYGON, float[].class);
        aggregates.addColumn(COL_BAND_ID, int.class);
        aggregates.addColumn(COL_COLOR_INDEX, int.class);

        for (Object variable : variableSet) {
            for (int bandId = 1; bandId <= settings.BandsCount; bandId++) {
                addAggregate(aggregates, variable, bandId);
                addAggregate(aggregates, variable, -bandId);
            }

            // addAggregate(visualGraph, aggregates, variable,
            // BAND_ID_POSITIVE_EXTREME_VALUES);
            // addAggregate(visualGraph, aggregates, variable,
            // BAND_ID_NEGATIVE_EXTREME_VALUES);
            addAggregate(aggregates, variable, BAND_ID_NULL_VALUES);
        }
    }

    /**
     * Adds a single aggregate to the aggregate table.
     * 
     * @param visualGraph
     *            the visualGraph used by the visualization
     * @param aggregates
     *            the aggregate table used by the visualization
     * @param variable
     *            the variable
     * @param bandId
     *            the bandId of the aggregate
     */
    private void addAggregate(AggregateTable aggregates, Object variable,
            int bandId) {

        List<VisualItem> itemsInBand = new Vector<VisualItem>();

        Predicate predVar = new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(fieldVariable),
                new ObjectLiteral(variable));
        @SuppressWarnings("unchecked")
        Iterator<VisualItem> items = m_vis.items(groupControlPoints, predVar);

        while (items.hasNext()) {
            VisualItem item = items.next();
            if (item.getBoolean(COL_IS_HELP_NODE)
                    && item.getInt(COL_BAND_ID) == bandId)
                itemsInBand.add(item);
        }

        if (itemsInBand.size() > 2) {
            AggregateItem aggregate = (AggregateItem) aggregates.addItem();
            aggregate
                    .set(VisualItem.POLYGON, new float[2 * itemsInBand.size()]);
            aggregate.setInt(COL_BAND_ID, bandId);
            aggregate.setInt(COL_COLOR_INDEX, bandId + MAX_BANDS + 1); // see
                                                                       // palette
                                                                       // structure

            for (VisualItem item : itemsInBand)
                aggregate.addItem(item);

            aggregate.setInteractive(false);
        }
    }

}
