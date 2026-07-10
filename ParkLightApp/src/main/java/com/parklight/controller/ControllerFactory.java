package com.parklight.controller;

import com.parklight.service.BillingService;
import com.parklight.service.ParkingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps an action prefix (the part before the slash, e.g. "parking") to the
 * controller that handles it. The map is built once when the server starts.
 * Adding a new controller means adding one entry here, with no change to the
 * request-handling code.
 */
public class ControllerFactory {

    private final Map<String, IController> controllers = new HashMap<>();

    public ControllerFactory(ParkingService parkingService, BillingService billingService) {
        controllers.put("parking", new ParkingController(parkingService));
        controllers.put("billing", new BillingController(billingService));
    }

    // Returns the controller for an action like "parking/park", or null if the
    // prefix is unknown.
    public IController getController(String action) {
        if (action == null) {
            return null;
        }
        int slash = action.indexOf('/');
        String prefix = slash < 0 ? action : action.substring(0, slash);
        return controllers.get(prefix);
    }
}
