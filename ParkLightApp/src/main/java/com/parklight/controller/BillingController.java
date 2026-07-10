package com.parklight.controller;

import com.parklight.dm.ParkingTicket;
import com.parklight.server.Request;
import com.parklight.server.Response;
import com.parklight.service.BillingService;

import com.google.gson.Gson;

/**
 * Exposes billing operations to the network layer.
 * Translates requests into BillingService calls and wraps results in responses.
 */
public class BillingController implements IController {

    private final BillingService billingService;
    private final Gson gson = new Gson();

    public BillingController(BillingService billingService) {
        if (billingService == null) {
            throw new IllegalArgumentException("BillingService cannot be null");
        }
        this.billingService = billingService;
    }

    @Override
    public Response<?> handle(String action, Request<?> request) {
        switch (action) {
            case "billing/revenue":
                return Response.ok(billingService.getTotalRevenue());
            case "billing/tickets":
                return Response.ok(billingService.getAllTickets());
            case "billing/ticket":
                return getTicket(request);
            default:
                return Response.error("Unknown billing action: " + action);
        }
    }

    // Body is expected to carry the ticket id under the "ticketId" field.
    private Response<?> getTicket(Request<?> request) {
        TicketIdBody payload =
                gson.fromJson(gson.toJson(request.getBody()), TicketIdBody.class);
        if (payload == null || payload.ticketId == null) {
            return Response.error("Missing ticketId in request body");
        }
        ParkingTicket ticket = billingService.getTicket(payload.ticketId);
        if (ticket == null) {
            return Response.error("Ticket not found");
        }
        return Response.ok(ticket);
    }

    private static class TicketIdBody {
        String ticketId;
    }
}
