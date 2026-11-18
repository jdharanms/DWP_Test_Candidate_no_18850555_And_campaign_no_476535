package uk.gov.dwp.uc.pairtest.domine;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TicketTypeRequestTest {

    @Test
    void shouldHaveFinalClass() {

        assertTrue(
                Modifier.isFinal(
                        TicketTypeRequest.class.getModifiers()
                ),
                "TicketTypeRequest should be final"
        );
    }

    @Test
    void shouldHavePrivateFinalFields() {

        Field[] fields = TicketTypeRequest.class.getDeclaredFields();

        for (Field field : fields) {

            assertTrue(
                    Modifier.isPrivate(field.getModifiers()),
                    field.getName() + " should be private"
            );

            assertTrue(
                    Modifier.isFinal(field.getModifiers()),
                    field.getName() + " should be final"
            );
        }
    }

    @Test
    void shouldNotHaveSetterMethods() {

        Method[] methods =
                TicketTypeRequest.class.getDeclaredMethods();

        boolean hasSetter = Arrays.stream(methods)
                .anyMatch(method ->
                        method.getName().startsWith("set"));

        assertFalse(
                hasSetter,
                "TicketTypeRequest should not have setter methods"
        );
    }

    @Test
    void shouldRejectNullTicketType() {

        assertThrows(
                InvalidPurchaseException.class,
                () -> new TicketTypeRequest(null, 2)
        );
    }

    @Test
    void shouldRejectZeroTickets() {

        assertThrows(
                InvalidPurchaseException.class,
                () -> new TicketTypeRequest(
                        TicketTypeRequest.Type.ADULT, 0)
        );
    }

}
