package timeBench.data.io;

import prefuse.data.Table;
import timeBench.data.relational.TemporalDataset;

public class TableConverter {
    
    // TODO support for multiple schemata (e.g., 2 instants, interval) in a list
//    int[] datacol = {3, 4, 5};
//    private TemporalObjectSchema schema = new DateInstantTemporalObjectSchema(0, datacol);

    public TemporalDataset importTable(Table table) {
        // 1. analyze & prepare schemata 
        // 1.1. auto-detect schema (optional)
        // 1.2. prepare table for data elements
        // 2. for each data row
        // 2.1. for each schema 
        // 2.1.1. extract temporal element & append to TempDS
        // 2.1.2. if it has data columns  
        // 2.1.2.1. extract data element & append to TempDS (optional
        // 2.1.2.2. link temporal element with data element in TempDS
        
        return null;
    }
    
    public Table exportTable(TemporalDataset tmpds) {
        return null;
    }
}
