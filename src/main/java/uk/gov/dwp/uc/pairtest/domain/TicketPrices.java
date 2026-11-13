package uk.gov.dwp.uc.pairtest.domain;

//The price of each ticket in pounds

public final class TicketPrices {

    private TicketPrices() {
    }

    public static int priceFrom(TicketTypeRequest.Type type) {
        return switch (type) {
            case ADULT -> 25;
            case CHILD -> 15;
            case INFANT -> 0;
        };
    }
}
