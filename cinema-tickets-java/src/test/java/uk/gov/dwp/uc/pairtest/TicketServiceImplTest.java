package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {
    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private TicketPaymentService ticketPaymentService;
    @Mock
    private BusinessValidation businessValidation;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void shouldPurchaseAdultTickets() {

        TicketTypeRequest adultRequest =adultTicket(2);
        doNothing().when(businessValidation).validation(1L, 2,adultRequest);
        assertDoesNotThrow(() ->
                ticketService.purchaseTickets(1L, adultRequest)
        );

        verify(seatReservationService).reserveSeat(1L, 2);
        verify(ticketPaymentService).makePayment(1L, 50);
    }

    @Test
    void shouldPurchaseMultipleTicketTypes() {

        TicketTypeRequest adult = adultTicket(2);
        TicketTypeRequest child = childTicket(1);
        TicketTypeRequest infant = infantTicket(1);
        doNothing().when(businessValidation).validation(1L, 3,adult,child,infant);
        assertDoesNotThrow(() ->
                ticketService.purchaseTickets(
                        1L, adult, child, infant)
        );
        verify(seatReservationService).reserveSeat(1L, 3);
        verify(ticketPaymentService).makePayment(1L, 65);
    }

   //Test Business Rules
   @Test
   void shouldExcludeInfantTicketsFromTotalTicketCount() {
       TicketTypeRequest adult = adultTicket(1);
       TicketTypeRequest infant = infantTicket(25);
       doNothing().when(businessValidation).validation(1L, 1,adult,infant);
       doNothing().when(seatReservationService).reserveSeat(1L, 1);


       assertDoesNotThrow(
               () -> ticketService.purchaseTickets(1L, adult, infant));
       verify(seatReservationService).reserveSeat(1L, 1);
       verify(seatReservationService, never()).reserveSeat(1L, 25);
       verify(ticketPaymentService).makePayment(1L, 25);
   }

    @Test
    void shouldThrowExceptionWhenAccountIdIsNull() {
        TicketTypeRequest adult = adultTicket(1);
        doThrow(new InvalidPurchaseException(
                "Invalid accountId: must be non-null, positive and greater than zero"))
                .when(businessValidation)
                .validation(null, 1, adult);
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(null, adult));
        verify(seatReservationService, never()).reserveSeat(0, 1);
        verify(ticketPaymentService, never()).makePayment(1L, 25);
    }

    @Test
    void shouldThrowExceptionWhenAccountIdIsZero() {
        TicketTypeRequest adult = adultTicket(1);
        doThrow(new InvalidPurchaseException(
                "Invalid accountId: must be non-null, positive and greater than zero"))
                .when(businessValidation)
                .validation(0L, 1, adult);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, adult));
        verify(seatReservationService, never()).reserveSeat(0L, 1);
        verify(ticketPaymentService, never()).makePayment(0L, 25);
    }

    @Test
    void shouldThrowExceptionWhenAccountIdIsNegative() {
        TicketTypeRequest adult = adultTicket(1);
        doThrow(new InvalidPurchaseException(
                "Invalid accountId: must be non-null, positive and greater than zero"))
                .when(businessValidation)
                .validation(-1L, 1, adult);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-1L, adult));
        verify(seatReservationService, never()).reserveSeat(-1L, 1);
        verify(ticketPaymentService, never()).makePayment(-1L, 25);
    }

   @Test
    void shouldThrowExceptionWhenNoAdultTicketIsProvided() {
        TicketTypeRequest child = childTicket(1);
       doThrow(new InvalidPurchaseException(
               "At least one Adult ticket request must be provided."))
               .when(businessValidation)
               .validation(1L, 1, child);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, child));
       verify(seatReservationService, never()).reserveSeat(1L, 1);
       verify(ticketPaymentService, never()).makePayment(1L, 25);
    }

    @Test
    void shouldThrowExceptionWhenMoreThan25NonInfantTicketsAreRequested() {
        TicketTypeRequest adult = adultTicket(1);
        TicketTypeRequest child = childTicket(25);
        doThrow(new InvalidPurchaseException(
                "Maximum 25 tickets can be purchased per transaction."))
                .when(businessValidation)
                .validation(1L, 26,adult, child);

        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, adult, child));
        verify(seatReservationService, never()).reserveSeat(1L, 1);
        verify(ticketPaymentService, never()).makePayment(1L, 400);

    }

    @Test
    void shouldAllowPurchaseWhenExactly25NonInfantTicketsAreRequested() {
        TicketTypeRequest adult = adultTicket(1);
        TicketTypeRequest child = childTicket(24);
        doNothing().when(businessValidation).validation(1L, 25,adult,child);


        assertDoesNotThrow(
                () -> ticketService.purchaseTickets(1L, adult, child));
        verify(seatReservationService).reserveSeat(1L, 25);
        verify(ticketPaymentService).makePayment(1L, 385);
    }

    @Test
    void testTryCatchInseatReservationServiceBlock() {

        TicketTypeRequest infant = infantTicket(24);
        doNothing().when(businessValidation).validation(1L, 0,infant);
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, infant));

        verify(seatReservationService, never()).reserveSeat(1L, 0);
        verify(ticketPaymentService, never()).makePayment(1L, 0);
    }


    private TicketTypeRequest adultTicket(int count) {
        return new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, count);
    }

    private TicketTypeRequest childTicket(int count) {
        return new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, count);
    }

    private TicketTypeRequest infantTicket(int count) {
        return new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, count);
    }

}
