package uk.gov.dwp.uc.pairtest.domain;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;


public record TicketOrder(int adults, int children, int infants) {

    public static final int MAX_NUMBER_TICKETS_PER_ORDER = 25;

    public TicketOrder {
        if (adults < 0 || children < 0 || infants < 0) {
            throw new InvalidPurchaseException("Ticket count can't be negative for an order");
        }

        if (adults > MAX_NUMBER_TICKETS_PER_ORDER || children > MAX_NUMBER_TICKETS_PER_ORDER
                || infants > MAX_NUMBER_TICKETS_PER_ORDER) {
            throw tooManyTicketsException();
        }

        int total = adults + children + infants;

        if (total == 0) {
            throw new InvalidPurchaseException("Number of tickets needs to be at least 1");
        }

        if (total > MAX_NUMBER_TICKETS_PER_ORDER) {
            throw tooManyTicketsException();
        }

        if (adults == 0) {
            throw new InvalidPurchaseException("At least 1 ticket needs to be for an adult");
        }

        if (infants > adults) {
            throw new InvalidPurchaseException("Number of infants can't be higher than number of adults");
        }
    }

    public static TicketOrder from(TicketTypeRequest... requests) {
        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("A purchase must include at least 1 ticket request");
        }

        Map<TicketTypeRequest.Type, Integer> counts = new EnumMap<>(TicketTypeRequest.Type.class);
        int totalCount = 0;

        for (TicketTypeRequest request : requests) {
            requireValid(request);
            if (request.getNoOfTickets() > MAX_NUMBER_TICKETS_PER_ORDER - totalCount) {
                throw tooManyTicketsException();
            }
            totalCount += request.getNoOfTickets();
            counts.merge(request.getTicketType(), request.getNoOfTickets(), Integer::sum);
        }

        return new TicketOrder(counts.getOrDefault(ADULT, 0), counts.getOrDefault(CHILD, 0),
                counts.getOrDefault(INFANT, 0));
    }

    private static void requireValid(TicketTypeRequest request) {
        if (request == null) {
            throw new InvalidPurchaseException("A ticket request can't be null");
        }

        if (request.getTicketType() == null) {
            throw new InvalidPurchaseException("A ticket request should have a type");
        }

        if (request.getNoOfTickets() < 0) {
            throw new InvalidPurchaseException("Number of tickets in a request can't be negative");
        }
    }

    public int totalTicketCount() {
        return adults + children + infants;
    }

    public int totalPrice() {
        return adults * TicketPrices.priceFrom(ADULT) + children * TicketPrices.priceFrom(CHILD)
                + infants * TicketPrices.priceFrom(INFANT);
    }

    public int totalSeats() {
        return adults + children;
    }

    private static InvalidPurchaseException tooManyTicketsException() {
        return new InvalidPurchaseException("No more than " + MAX_NUMBER_TICKETS_PER_ORDER + " tickets can be purchased");
    }
}
