package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BusinessValidation {
    private static final Logger logger =
            LoggerFactory.getLogger(BusinessValidation.class);

    public void validation(Long accountId, int totalTickets, TicketTypeRequest... ticketTypeRequests) {
        logger.info("Validating Business Rules");
            if (accountId == null || accountId <= 0) {
                throw new InvalidPurchaseException("Invalid accountId: must be non-null, positive and greater than zero");
            }
            if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
                throw new InvalidPurchaseException("At least one ticket request must be provided.");
            }
            if (Arrays.stream(ticketTypeRequests)
                    .noneMatch(request ->
                            request.getTicketType() == TicketTypeRequest.Type.ADULT)) {
                throw new InvalidPurchaseException("At least one Adult ticket request must be provided.");
            }
            if (totalTickets > 25) {
                throw new InvalidPurchaseException(
                        "Maximum 25 tickets can be purchased per transaction.");
            }

            logger.info("businessValidation Rules completed successfully");


    }


}
