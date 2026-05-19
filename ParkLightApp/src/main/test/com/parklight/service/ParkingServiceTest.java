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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end test for the parking service. Wires the real components together
 * (algorithm + DAOs + services) on top of two throwaway files on disk.
 *
 * Per the assignment notes, this test only uses one of the algorithm
 * implementations (the algorithms themselves were already covered in Part A).
 */
public class ParkingServiceTest {

    private static final String SPOTS_FILE = "test_spots.dat";
    private static final String TICKETS_FILE = "test_tickets.dat";
    private static final String ENTRANCE = "ENTRANCE";

    private IDao<String, ParkingSpot> spotsDao;
    private IDao<String, ParkingTicket> ticketsDao;
    private ParkingService parking;
    private BillingService billing;

    @Before
    public void setUp() {
        deleteTestFiles();

        spotsDao = new DaoFileImpl<>(SPOTS_FILE);
        ticketsDao = new DaoFileImpl<>(TICKETS_FILE);

        // Build the parking-lot graph:
        //   ENTRANCE --1-- AISLE --1-- S1
        //                    |
        //                    +--2-- S2
        //                    |
        //                    +--3-- S3 (electric)
        //                    |
        //                    +--4-- S4 (disabled)
        IAlgoShortestPath<String> algo = new DijkstraAlgoImpl<>();
        addUndirected(algo, ENTRANCE, "AISLE", 1);
        addUndirected(algo, "AISLE", "S1", 1);
        addUndirected(algo, "AISLE", "S2", 2);
        addUndirected(algo, "AISLE", "S3", 3);
        addUndirected(algo, "AISLE", "S4", 4);

        billing = new BillingService(ticketsDao);
        parking = new ParkingService(algo, spotsDao, ticketsDao, billing, ENTRANCE);

        parking.registerSpot(new ParkingSpot("S1", SpotType.REGULAR, 1, 0));
        parking.registerSpot(new ParkingSpot("S2", SpotType.REGULAR, 2, 0));
        parking.registerSpot(new ParkingSpot("S3", SpotType.ELECTRIC, 3, 0));
        parking.registerSpot(new ParkingSpot("S4", SpotType.DISABLED, 4, 0));
    }

    @After
    public void tearDown() {
        deleteTestFiles();
    }

    // ----- parkVehicle -----

    @Test
    public void parkVehicleAssignsClosestCompatibleSpot() {
        Vehicle v = new Vehicle("ABC-123", VehicleType.REGULAR);
        ParkingTicket ticket = parking.parkVehicle(v);
        assertNotNull(ticket);
        assertEquals("S1", ticket.getSpot().getId()); // closest regular spot
        assertTrue(parking.getSpot("S1").isOccupied());
    }

    @Test
    public void parkVehicleRespectsSpotType() {
        Vehicle electric = new Vehicle("EV-1", VehicleType.ELECTRIC);
        ParkingTicket ticket = parking.parkVehicle(electric);
        assertNotNull(ticket);
        assertEquals("S3", ticket.getSpot().getId());
        assertEquals(SpotType.ELECTRIC, ticket.getSpot().getType());
    }

    @Test
    public void parkVehicleSkipsOccupiedSpots() {
        parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR)); // takes S1
        ParkingTicket second = parking.parkVehicle(new Vehicle("CAR-2", VehicleType.REGULAR));
        assertNotNull(second);
        assertEquals("S2", second.getSpot().getId()); // next closest regular
    }

    @Test
    public void parkVehicleReturnsNullWhenNoCompatibleSpot() {
        // Fill all REGULAR spots, then try to park another regular vehicle.
        parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));
        parking.parkVehicle(new Vehicle("CAR-2", VehicleType.REGULAR));
        ParkingTicket none = parking.parkVehicle(new Vehicle("CAR-3", VehicleType.REGULAR));
        assertNull(none);
    }

    // ----- releaseVehicle -----

    @Test
    public void releaseVehicleFreesSpotAndChargesPrice() throws InterruptedException {
        ParkingTicket ticket = parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));
        assertNotNull(ticket);
        Thread.sleep(5); // ensure exitTime > entryTime

        ParkingTicket closed = parking.releaseVehicle(ticket.getTicketId());
        assertNotNull(closed);
        assertTrue(closed.getExitTime() > 0);
        assertTrue(closed.getPrice() > 0);
        assertFalse(parking.getSpot("S1").isOccupied());
    }

    @Test
    public void releaseVehicleReturnsNullForUnknownTicket() {
        assertNull(parking.releaseVehicle("does-not-exist"));
    }

    @Test
    public void releaseVehicleReturnsNullForAlreadyClosedTicket() throws InterruptedException {
        ParkingTicket ticket = parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));
        Thread.sleep(5);
        parking.releaseVehicle(ticket.getTicketId());
        assertNull(parking.releaseVehicle(ticket.getTicketId()));
    }

    // ----- spot management -----

    @Test
    public void getAvailableAndOccupiedSpotsReflectCurrentState() {
        assertEquals(4, parking.getAvailableSpots().size());
        assertEquals(0, parking.getOccupiedSpots().size());

        parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));

        assertEquals(3, parking.getAvailableSpots().size());
        assertEquals(1, parking.getOccupiedSpots().size());
    }

    @Test
    public void deleteSpotRemovesItFromTheLot() {
        assertTrue(parking.deleteSpot("S4"));
        assertNull(parking.getSpot("S4"));
        assertEquals(3, parking.getAllSpots().size());
    }

    // ----- billing -----

    @Test
    public void totalRevenueAccumulatesAcrossClosedTickets() throws InterruptedException {
        ParkingTicket t1 = parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));
        ParkingTicket t2 = parking.parkVehicle(new Vehicle("EV-1", VehicleType.ELECTRIC));
        Thread.sleep(5);
        parking.releaseVehicle(t1.getTicketId());
        parking.releaseVehicle(t2.getTicketId());

        double expected = billing.calculatePrice(billing.getTicket(t1.getTicketId()))
                        + billing.calculatePrice(billing.getTicket(t2.getTicketId()));
        assertEquals(expected, billing.getTotalRevenue(), 1e-9);
    }

    // ----- persistence: data survives a fresh DAO instance -----

    @Test
    public void dataPersistsAcrossDaoInstances() {
        parking.parkVehicle(new Vehicle("CAR-1", VehicleType.REGULAR));

        // Recreate the DAOs - they should reload the same data from disk.
        IDao<String, ParkingSpot> reloadedSpots = new DaoFileImpl<>(SPOTS_FILE);
        IDao<String, ParkingTicket> reloadedTickets = new DaoFileImpl<>(TICKETS_FILE);

        List<ParkingSpot> spots = reloadedSpots.getAll();
        assertEquals(4, spots.size());

        // S1 should be occupied in the reloaded data
        ParkingSpot s1 = reloadedSpots.get("S1");
        assertNotNull(s1);
        assertTrue(s1.isOccupied());

        assertEquals(1, reloadedTickets.getAll().size());
    }

    // ----- helpers -----

    private void addUndirected(IAlgoShortestPath<String> algo, String a, String b, double w) {
        algo.addEdge(a, b, w);
        algo.addEdge(b, a, w);
    }

    private void deleteTestFiles() {
        new File(SPOTS_FILE).delete();
        new File(TICKETS_FILE).delete();
    }
}
