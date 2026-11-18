package uk.gov.dwp.uc.pairtest.domain;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

    private final int noOfTickets;
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        if (type == null) {
            throw new InvalidPurchaseException("Ticket type cannot be null");
        }

        if (noOfTickets <= 0) {
            throw new InvalidPurchaseException("Ticket count cannot be negative");
        }
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT(25),
        CHILD(15),
        INFANT(0);

        private final int price;

        Type(int price) {
            this.price = price;
        }


        public int getPrice() {
            return price;
        }
    }

}
