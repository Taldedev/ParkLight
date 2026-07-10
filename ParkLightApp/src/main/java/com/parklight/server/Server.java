package com.parklight.server;

import com.parklight.algorithm.DijkstraAlgoImpl;
import com.parklight.algorithm.IAlgoShortestPath;
import com.parklight.controller.ControllerFactory;
import com.parklight.dao.DaoFileImpl;
import com.parklight.dao.IDao;
import com.parklight.dm.ParkingSpot;
import com.parklight.dm.ParkingTicket;
import com.parklight.service.BillingService;
import com.parklight.service.ParkingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens on a TCP port and serves parking and billing requests.
 * On startup it wires the services, algorithm and factory together, then
 * accepts client connections and handles each one on its own thread.
 */
public class Server implements Runnable {

    private static final String SPOTS_FILE = "spots.dat";
    private static final String TICKETS_FILE = "tickets.dat";
    private static final String ENTRANCE = "ENTRANCE";

    private final int port;
    private ControllerFactory factory;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        initComponents();
        listen();
    }

    // Builds the services, algorithm and factory used to serve requests.
    private void initComponents() {
        IDao<String, ParkingSpot> spotsDao = new DaoFileImpl<>(SPOTS_FILE);
        IDao<String, ParkingTicket> ticketsDao = new DaoFileImpl<>(TICKETS_FILE);

        IAlgoShortestPath<String> algo = new DijkstraAlgoImpl<>();
        buildLotGraph(algo);

        BillingService billing = new BillingService(ticketsDao);
        ParkingService parking =
                new ParkingService(algo, spotsDao, ticketsDao, billing, ENTRANCE);

        seedSpotsIfEmpty(parking);

        this.factory = new ControllerFactory(parking, billing);
    }

    // Defines the parking-lot layout as a weighted graph.
    private void buildLotGraph(IAlgoShortestPath<String> algo) {
        addUndirected(algo, ENTRANCE, "AISLE", 1);
        addUndirected(algo, "AISLE", "S1", 1);
        addUndirected(algo, "AISLE", "S2", 2);
        addUndirected(algo, "AISLE", "S3", 3);
        addUndirected(algo, "AISLE", "S4", 4);
    }

    private void addUndirected(IAlgoShortestPath<String> algo, String a, String b, double w) {
        algo.addEdge(a, b, w);
        algo.addEdge(b, a, w);
    }

    // Registers a default set of spots the first time the server runs.
    private void seedSpotsIfEmpty(ParkingService parking) {
        if (!parking.getAllSpots().isEmpty()) {
            return;
        }
        parking.registerSpot(new ParkingSpot("S1", com.parklight.dm.SpotType.REGULAR, 1, 0));
        parking.registerSpot(new ParkingSpot("S2", com.parklight.dm.SpotType.REGULAR, 2, 0));
        parking.registerSpot(new ParkingSpot("S3", com.parklight.dm.SpotType.ELECTRIC, 3, 0));
        parking.registerSpot(new ParkingSpot("S4", com.parklight.dm.SpotType.DISABLED, 4, 0));
    }

    // Accepts connections and hands each one to a HandleRequest thread.
    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ParkLight server listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new HandleRequest(client, factory)).start();
            }
        } catch (IOException e) {
            System.err.println("Server failed on port " + port + ": " + e.getMessage());
        }
    }
}
