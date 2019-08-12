package com.github.dmogn.restaurant;

import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SeatingManagerTest {
    
    static List<Table> tables;
    
    @BeforeEach
    public void init() {
        // init with 12 tables of different sizes:
        tables = new LinkedList<>();
        tables.add(new Table(2));
        tables.add(new Table(6));
        tables.add(new Table(4));
        tables.add(new Table(6));
        tables.add(new Table(2));
        tables.add(new Table(5));
        tables.add(new Table(4));
        tables.add(new Table(2));
        tables.add(new Table(3));
        tables.add(new Table(3));
        tables.add(new Table(5));
        tables.add(new Table(2));
    }
    
    @Test
    public void emptySeatingManagerTest() {
        SeatingManager manager = new SeatingManager(tables);
        
        // check empty restaurant
        assertEquals(tables.size(), manager.getUnallocatedTablesCount());
        assertEquals(0, manager.getAllocatedTablesCount());
        assertEquals(0, manager.getWaitingGroupsCount());
        assertEquals(0, manager.getLeavedGroupsCount());
    }
    
    @Test
    public void groupAllocationTest() {
        SeatingManager manager = new SeatingManager(tables);
        
        // add seating group
        CustomerGroup group1 = new CustomerGroup(2);
        manager.arrives(group1);
        
        CustomerGroup group2 = new CustomerGroup(4);
        manager.arrives(group2);
        
        CustomerGroup group3 = new CustomerGroup(4);
        manager.arrives(group3);
        
        CustomerGroup group4 = new CustomerGroup(6);
        manager.arrives(group4);
        
        CustomerGroup group5 = new CustomerGroup(6);
        manager.arrives(group5);
        
        CustomerGroup group6 = new CustomerGroup(5);
        manager.arrives(group6);
        
        CustomerGroup group7 = new CustomerGroup(5);
        manager.arrives(group7);
        
        assertEquals(tables.size()-7, manager.getUnallocatedTablesCount());
        assertEquals(7, manager.getAllocatedTablesCount());
        assertEquals(0, manager.getWaitingGroupsCount());
        assertEquals(0, manager.getLeavedGroupsCount());
        
        // add waiting groups
        CustomerGroup group8 = new CustomerGroup(5);
        manager.arrives(group8);
        
        CustomerGroup group9 = new CustomerGroup(4);
        manager.arrives(group9);
        
        assertEquals(2, manager.getWaitingGroupsCount());
        assertNull(manager.locate(group8));
        assertNotNull(manager.locate(group3));
        assertEquals(4, manager.locate(group3).getSize());
        
        // leave seating groups
        manager.leaves(group3);
        
        assertNull(manager.locate(group3));
        assertEquals(1, manager.getWaitingGroupsCount());
        assertEquals(1, manager.getLeavedGroupsCount());
        
        manager.leaves(group1);
        
        assertEquals(1, manager.getWaitingGroupsCount());
        assertEquals(2, manager.getLeavedGroupsCount());
        
        // leave waiting group
        manager.leaves(group8);
        
        assertNull(manager.locate(group8));
        assertEquals(0, manager.getWaitingGroupsCount());
        assertEquals(3, manager.getLeavedGroupsCount());
        
        // total tables count
        assertEquals(tables.size(), manager.getUnallocatedTablesCount() + manager.getAllocatedTablesCount());
    }
    
    @Test
    public void customersArrivalOrderTest() {
        final SeatingManager manager = new SeatingManager(tables);
        
        final int clientDroupsCount = 100;
        
        final List<CustomerGroup> clientGroups = new ArrayList<>(clientDroupsCount);
        
        // init client groups queue
        IntStream.range(0, clientDroupsCount).forEach(i -> {
            final CustomerGroup g = new CustomerGroup(2);
            clientGroups.add(g);
            manager.arrives(g);
        });
        
        // all tables are allocated
        assertEquals(tables.size(), manager.getAllocatedTablesCount());
        
        IntStream.range(0, tables.size()).forEach(i -> {
            assertNotNull(manager.locate(clientGroups.get(i)));
        });
        
        // ordered client queue
        IntStream.range(tables.size(), clientGroups.size()).forEach(i -> {
            // leave the group on front size
            manager.leaves(clientGroups.get(i - tables.size()));
            // check next order has been allocated
            assertNotNull(manager.locate(clientGroups.get(i)));
        });
        
        // all tables are still allocated
        assertEquals(tables.size(), manager.getAllocatedTablesCount());
    }
}
