package uk.gov.dwp.uc.pairtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {
    private static final Logger logger =
            LoggerFactory.getLogger(TicketServiceImpl.class);

    private final SeatReservationService seatReservationService;
    private final TicketPaymentService ticketPaymentService;
    private BusinessValidation businessValidation;


    public TicketServiceImpl(
            SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService,
            BusinessValidation businessValidation) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
        this.businessValidation = businessValidation;
    }

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        // Remove Infant from total Number of tickets
        int totalTickets = Arrays.stream(ticketTypeRequests)
                .filter(request ->
                        request.getTicketType() != TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();

        // Validate Business rules
        businessValidation.validation(accountId, totalTickets, ticketTypeRequests);

        try {
            if (totalTickets <= 0) {
                throw new InvalidPurchaseException("Invalid totalTickets: must be positive and greater than zero");
            }
            seatReservationService.reserveSeat(accountId, totalTickets);
            logger.info("Seat reservation completed successfully");
        } catch (RuntimeException e) {
            logger.error("Seat reservation failed", e);
            throw e;
        }
        int totalAmountToPay = Arrays.stream(ticketTypeRequests)
                .filter(request ->
                        request.getTicketType() != TicketTypeRequest.Type.INFANT)
                .mapToInt(request ->
                        request.getNoOfTickets()
                                * request.getTicketType().getPrice())
                .sum();
        try {
            if (totalAmountToPay <= 0) {
                throw new InvalidPurchaseException("Invalid totalAmountToPay: must be positive and greater than zero");
            }
            ticketPaymentService.makePayment(accountId, totalAmountToPay);
            logger.info("Ticket payment completed successfully");
        } catch (RuntimeException  e) {
            logger.error("Ticket payment failed", e);
            throw e;
        }
        logger.info("Ticket purchase completed successfully");
    }

}
