package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketOrderTest {

    TicketTypeRequest adults(int n) {
        return new TicketTypeRequest(ADULT, n);
    }

    TicketTypeRequest children(int n) {
        return new TicketTypeRequest(CHILD, n);
    }

    TicketTypeRequest infants(int n) {
        return new TicketTypeRequest(INFANT, n);
    }

    String rejectionOf(TicketTypeRequest... requests) {
        return assertThrows(InvalidPurchaseException.class,
                () -> TicketOrder.from(requests)).getMessage();
    }

    @Nested
    public class PricesAndSeats {

        @ParameterizedTest
        @CsvSource({
                "1,  0,  0,  25,  1",
                "1,  1,  0,  40,  2",
                "1,  0,  1,  25,  1",
                "2,  3,  2,  95,  5",
                "25, 0,  0,  625, 25",
                "1,  24, 0,  385, 25",
                "12, 1,  12, 315, 13",
        })
        void adultsAndChildrenPayAndGetSeatsWhileInfantsDoNot(int adults, int children, int infants, int price,
                                                              int seats) {
            TicketOrder order = TicketOrder.from(adults(adults), children(children), infants(infants));

            assertEquals(adults + children + infants, order.totalTicketCount());
            assertEquals(seats, order.totalSeats());
            assertEquals(price, order.totalPrice());
        }
    }

    @Nested
    public class OrderFromRequests {

        @Test
        void orderCountsIndividualRequests() {
            assertEquals(new TicketOrder(1, 2, 1),
                    TicketOrder.from(adults(1), children(2), infants(1)));
        }

        @Test
        void orderSumsIndividualRequests() {
            assertEquals(new TicketOrder(2, 2, 0),
                    TicketOrder.from(adults(1), children(2), adults(1)));
        }

        @Test
        void orderCountsIndividualRequestsIndependentlyOfOrder() {
            assertEquals(new TicketOrder(1, 2, 1),
                    TicketOrder.from(infants(1), adults(1), children(2)));
        }

        @Test
        void orderIgnoresAZeroTicketRequests() {
            assertEquals(new TicketOrder(1, 0, 1),
                    TicketOrder.from(infants(1), adults(1), children(0)));
        }

        @Test
        void orderRejectsAllZeroTicketsRequests() {
            assertEquals("Number of tickets needs to be at least 1",
                    rejectionOf(infants(0), adults(0), children(0)));
        }

        @Test
        void orderRejectsNullAndEmptyRequests() {
            assertThrows(InvalidPurchaseException.class,
                    () -> TicketOrder.from((TicketTypeRequest[]) null));
            assertThrows(InvalidPurchaseException.class,
                    TicketOrder::from);
        }

        @Test
        void orderRejectsANullRequest() {
            assertThrows(InvalidPurchaseException.class,
                    () -> TicketOrder.from(adults(1), null));
        }

        @Test
        void orderRejectsRequestsWithoutType() {
            assertThrows(InvalidPurchaseException.class,
                    () -> TicketOrder.from(new TicketTypeRequest(null, 3)));
        }

        @Test
        void orderRejectsRequestsWithNegativeValue() {
            assertThrows(InvalidPurchaseException.class,
                    () -> TicketOrder.from(new TicketTypeRequest(ADULT, -3)));
        }

    }

    @Nested
    public class MaximumNumberOfTickets {

        @Test
        void orderAllows25TicketsRequested() {
            assertEquals(new TicketOrder(1, 23, 1),
                    TicketOrder.from(adults(1), children(23), infants(1)));
        }

        @Test
        void orderRejectsAboveMaxRequests() {
            assertEquals("No more than 25 tickets can be purchased",
                    rejectionOf(adults(1), children(24), infants(1)));
        }

        @Test
        void orderRejectsAboveMaxRequestsIncludingInfantsEvenThoughTheyDoNotGetASeat() {
            assertEquals("No more than 25 tickets can be purchased",
                    rejectionOf(adults(13), infants(13)));
        }

        @Test
        void orderRejectsBigRequestsWithoutOverflowingAnInt() {
            assertEquals("No more than 25 tickets can be purchased",
                    rejectionOf(adults(Integer.MAX_VALUE), infants(Integer.MAX_VALUE)));
        }

    }

    @Nested
    public class AdultRules {
        @Test
        void orderRejectsInfantsWithoutAdult() {
            assertEquals("At least 1 ticket needs to be for an adult",
                    rejectionOf(adults(0), infants(1)));
        }

        @Test
        void orderRejectsChildrenWithoutAdult() {
            assertEquals("At least 1 ticket needs to be for an adult",
                    rejectionOf(adults(0), children(1)));
        }

        @Test
        void orderAllowsInfantsInEqualNumberToAdults() {
            assertEquals(new TicketOrder(12, 0, 12),
                    TicketOrder.from(infants(12), adults(12)));
        }

        @Test
        void orderRejectsInfantsNumberHigherThanAdults() {
            assertEquals("Number of infants can't be higher than number of adults",
                    rejectionOf(adults(1), infants(2)));
        }

        @Test
        void orderAllowsAnyNumberOfChildrenWithOneAdult() {
            assertEquals(new TicketOrder(1, 24, 0),
                    TicketOrder.from(children(24), adults(1)));
        }
    }

    @Test
    void ticketOrderConstructorRejectsInvalidValues() {
        assertThrows(InvalidPurchaseException.class, () -> new TicketOrder(-1, 0, 1));
        assertThrows(InvalidPurchaseException.class, () -> new TicketOrder(0, 0, 0));
        assertThrows(InvalidPurchaseException.class, () -> new TicketOrder(26, 0, 0));
        assertThrows(InvalidPurchaseException.class, () -> new TicketOrder(Integer.MAX_VALUE, Integer.MAX_VALUE, 0));
    }

}
