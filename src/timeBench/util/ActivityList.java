package timeBench.util;

import prefuse.action.Action;
import prefuse.activity.Activity;
import prefuse.util.collections.CopyOnWriteArrayList;

/**
 * Abstract base class for Action implementations that hold a collection
 * of subclasses.
 *  
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class ActivityList extends Activity {

    protected CopyOnWriteArrayList m_activities = new CopyOnWriteArrayList();
    
    /**
     * Creates a new run-once CompositeAction.
     */
    public ActivityList() {
        super(0);
    }

    /**
     * Creates a new ActivityList of specified duration and default
     * step time of 20 milliseconds.
     * @param duration the duration of this Activity, in milliseconds
     */
    public ActivityList(long duration) {
        super(duration, Activity.DEFAULT_STEP_TIME);
    }
    
    /**
     * Creates a new ActivityList of specified duration and step time.
     * @param duration the duration of this Activity, in milliseconds
     * @param stepTime the time to wait in milliseconds between executions
     *  of the action list
     */
    public ActivityList(long duration, long stepTime) {
        super(duration, stepTime);
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Returns the number of Activities in the composite.
     * @return the size of this composite
     */
    public int size() {
        return m_activities.size();
    }
    
    /**
     * Adds an Activity to the end of the composite list.
     * @param a the Action instance to add
     */
    public void add(Activity a) {
        m_activities.add(a);
    }
    
    /**
     * Adds an Activity at the given index.
     * @param i the index at which to add the Action
     * @param a the Action instance to add
     */
    public void add(int i, Activity a) {
        m_activities.add(i, a);
    }
    
    /**
     * Returns the Activity at the specified index.
     * @param i the index
     * @return the requested Action
     */
    public Activity get(int i) {
        return (Activity)m_activities.get(i);
    }
    
    /**
     * Removes a given Activity from the composite.
     * @param a the Action to remove
     * @return true if the Action was found and removed, false otherwise
     */
    public boolean remove(Activity a) {
        return m_activities.remove(a);
    }
    
    /**
     * Removes the Activity at the specified index.
     * @param i the index
     * @return the removed Action
     */
    public Activity remove(int i) {
        return (Activity)m_activities.remove(i);
    }
    

    /**
     * @see prefuse.action.Action#run(double)
     */
    public void run(double frac) {
        Object[] actions = m_activities.getArray();
        for ( int i=0; i<actions.length; ++i ) {
            Action a = (Action)actions[i];
            try {
                if ( a.isEnabled() ) a.run(frac);
            } catch ( Exception e ) {
//                s_logger.warning(e.getMessage() + '\n'
//                        + StringLib.getStackTrace(e));
            }
        }
    }

    
    
} // end of class CompositeAction
