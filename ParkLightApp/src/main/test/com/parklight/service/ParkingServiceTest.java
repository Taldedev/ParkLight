package com.parklight.service;

import com.parklight.algorithm.DijkstraAlgoImpl;
import com.parklight.algorithm.IAlgoShortestPath;
import com.parklight.dao.DaoFileImpl;
import com.parklight.dao.IDao;
import com.parklight.dm.ParkingSpot;
import com.parklight.dm.ParkingTicket;
import com.parklight.dm.SpotType;
import com.parklight.dm.Vehicle;
import com.parklight.dm.VehicleType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end test for the parking service.
 * Wires algorithm + DAOs + services on top of throwaway files.
 */
public class ParkingServiceTest {

    private static final String SPOTS_FILE = "test_spots.dat";
    private static final String TICKETS_FILE = "test_tickets.dat";
    private static final String ENTRANCE = "ENTRANCE";

    private IDao<String, ParkingSpot> spotsDao;
    private IDao<String, ParkingTicket> ticketsDao;
    private ParkingService parking;

    @Before
    public void setUp() {
        deleteTestFiles();

        spotsDao = new DaoFileImpl<>(SPOTS_FILE);
        ticketsDao = new DaoFileImpl<>(TICKETS_FILE);

        IAlgoShortestPath<String> algo = new DijkstraAlgoImpl<>();
        addUndirected(algo, ENTRANCE, "AISLE", 1);
        addUndirected(algo, "AISLE", "S1", 1);
        addUndirected(algo, "AISLE", "S2", 2);
        addUndirected(algo, "AISLE", "S3", 3);

        BillingService billing = new BillingService(ticketsDao);
        parking = new ParkingService(algo, spotsDao, ticketsDao, billing, ENTRANCE);

        parking.registerSpot(new ParkingSpot("S1", SpotType.REGULAR, 1, 0));
        parking.registerSpot(new ParkingSpot("S2", SpotType.REGULAR, 2, 0));
        parking.registerSpot(new ParkingSpot("S3", SpotType.ELECTRIC, 3, 0));
    }

    @After
    public void tearDown() {
        deleteTestFiles();
    }

    @Test
    public void parkVehicleAssignsClosestCompatibleSpot() {
        ParkingTicket ticket = parking.parkVehicle(new Vehicle("ABC-123", VehicleType.REGULAR));
        assertNotNull(ticket);
        assertEquals("S1", ticket.getSpot().getId());
        assertTrue(parking.getSpot("S1").isOccupied());
    }

    @Test
    public void releaseVehicleFreesSpotAndChargesPrice() throws InterruptedException {
        ParkingTicket ticket = parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));
        Thread.sleep(5);
        ParkingTicket closed = parking.releaseVehicle(ticket.getTicketId());
        assertNotNull(closed);
        assertTrue(closed.getPrice() > 0);
        assertFalse(parking.getSpot("S1").isOccupied());
    }

    @Test
    public void deleteSpotRemovesItFromTheLot() {
        assertTrue(parking.deleteSpot("S3"));
        assertNull(parking.getSpot("S3"));
        assertEquals(2, parking.getAllSpots().size());
    }

    @Test
    public void dataPersistsAcrossDaoInstances() {
        parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));

        IDao<String, ParkingSpot> reloadedSpots = new DaoFileImpl<>(SPOTS_FILE);
        ParkingSpot s1 = reloadedSpots.get("S1");
        assertNotNull(s1);
        assertTrue(s1.isOccupied());
    }

    private void addUndirected(IAlgoShortestPath<String> algo, String a, String b, double w) {
        algo.addEdge(a, b, w);
        algo.addEdge(b, a, w);
    }

    private void deleteTestFiles() {
        new File(SPOTS_FILE).delete();
        new File(TICKETS_FILE).delete();
    }
}
