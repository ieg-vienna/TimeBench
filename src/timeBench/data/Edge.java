package timeBench.data;

/**
 * Edge object for handling Edge-related data from a GraphML file in an easy way.
 * 
 * @author Sascha Plessberger
 *
 */
public class Edge {
	private String sourceType;
	private String targetType;
	private long lSource;
	private long lTarget;
	private int iSource;
	private int iTarget;
	
	/**
	 * Constructor
	 * @param source
	 * @param target
	 */
	public Edge(String source, String target) {
		this.sourceType = source.substring(0, 1);
    	this.targetType = target.substring(0, 1);
    	this.lSource = Long.parseLong(source.substring(1, 2), 10);
    	this.iSource = Integer.parseInt(source.substring(1, 2));
        this.lTarget = Long.parseLong(target.substring(1, 2), 10);
        this.iTarget = Integer.parseInt(target.substring(1, 2));
	}
	
	/**
	 * Returns the type of the starting point of an edge.
	 * @return
	 */
	public String GetSourceType() {
		if("t".equals(sourceType)) 
			return "element";
		else
			return "object";
	}
	
	/**
	 * Returns the type of the ending point of an edge.
	 * @return
	 */
	public String GetTagetType() {
		if("t".equals(targetType)) 
			return "element";
		else
			return "object";
	}
	
	/**
	 * Returns the Source ID as a long.
	 * @return
	 */
	public long getSourceIdAsLong() {
		return this.lSource;
	}
	
	/**
	 * Returns the Target ID as a long.
	 * @return
	 */
	public long getTargetIdAsLong() {
		return this.lTarget;
	}
	
	/**
	 * Returns the Source ID as an int.
	 * @return
	 */
	public int getSourceIdAsInt() {
		return this.iSource;
	}
	
	/**
	 * Returns the Target ID as an int.
	 * @return
	 */
	public int getTargetIdAsInt() {
		return this.iTarget;
	}
}