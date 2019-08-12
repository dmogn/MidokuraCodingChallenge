package com.github.dmogn.restaurant;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SeatingManager {
    private final TreeMap<Integer, List<Table>> freeTablesPool;
    
    private final Set<CustomerGroup> waitingGroups;
    
    private final Map<CustomerGroup, Table> seatingGroups;
    
    private final Set<CustomerGroup> leavedGroups;
    
    /* Constructor */
    public SeatingManager(List<Table> tables) {
        freeTablesPool = new TreeMap<>();
        tables.forEach(table -> freeTable(table)); 
        
        waitingGroups = new LinkedHashSet<>();
        seatingGroups = new HashMap<>();
        leavedGroups = new LinkedHashSet<>();
    }
    
    /* Group arrives and wants to be seated. */
    public void arrives(CustomerGroup group) {
        waitingGroups.add(group);
        reallocateWaitingGroups();
    }
    
    /* Whether seated or not, the group leaves the restaurant. */
    public void leaves(CustomerGroup group) {
        if (waitingGroups.contains(group))
            waitingGroups.remove(group);
        else if (seatingGroups.containsKey(group)) {
            freeTable(seatingGroups.get(group));
            seatingGroups.remove(group);
            reallocateWaitingGroups();
        }
            
        leavedGroups.add(group);
    }
    
    /* Return the table at which the group is seated, or null if
    they are not seated (whether they're waiting or already left). */
    public Table locate(CustomerGroup group) {
        if (leavedGroups.contains(group))
            return null;// already left
        else if (waitingGroups.contains(group))
            return null;// in waiting
        else
            return seatingGroups.getOrDefault(group, null);
    }
    
    /**
     * Try to allocate tables for waiting groups.
     */
    private void reallocateWaitingGroups() {
        // try to allocate groups in the order of life queue
        Iterator<CustomerGroup> iter = waitingGroups.iterator();
        while (iter.hasNext()) {
            CustomerGroup group = iter.next();
            final Table table = allocateTable(group.getSize());
            if (table != null) {
                // asignee the table to the group
                iter.remove();
                
                seatingGroups.put(group, table);
            }
                
            if (freeTablesPool.isEmpty())
                return;// no available tables
        }
    }
    
    /**
     * Try to allocate table with required size.
     * 
     * Worst computation complexity: O(log n)
     */
    private Table allocateTable(int minSize) {
        Map.Entry<Integer, List<Table>> e = freeTablesPool.ceilingEntry(minSize);
        if (e != null) {
            Table table = e.getValue().remove(0);
            if (e.getValue().isEmpty())
                freeTablesPool.remove(e.getKey());

            return table;
        } else {
            return null;
        }
    }
    
    /**
     * Add table to pool of free tables.
     * 
     * Worst computation complexity: O(log n)
     * 
     * @param table 
     */
    private void freeTable(Table table) {
        List<Table> l = freeTablesPool.get(table.getSize());
        if (l == null) {
            l = new LinkedList<>();
            freeTablesPool.put(table.getSize(), l);
        }
        l.add(table);
    }
    
    public int getUnallocatedTablesCount() {
        AtomicInteger counter = new AtomicInteger(); 
        freeTablesPool
                .values()
                .forEach(list -> counter.addAndGet(list.size()));
        return counter.intValue();
    }
    
    public int getAllocatedTablesCount() {
        return seatingGroups.size();
    }
    
    public int getWaitingGroupsCount() {
        return waitingGroups.size();
    }
    
    public int getLeavedGroupsCount() {
        return leavedGroups.size();
    }
}