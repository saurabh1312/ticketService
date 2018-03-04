package saurabh;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for TicketServiceImpl.
 */
public class TicketServiceImplTest extends TestCase {

    protected TicketServiceImpl ticketService;

    @Override
    protected void setUp() {
        ticketService = new TicketServiceImpl();
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TicketServiceImplTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( TicketServiceImplTest.class );
    }

    public void testSeatAvailability() {
        System.out.println("Running testSeatAvailability...");
        SeatHold seatHold = ticketService.findAndHoldSeats(3, "saurabh1312@gmail.com");
        String reservationCode = ticketService.reserveSeats(seatHold.seatHoldId, "saurabh1312@gmail.com");
        seatHold = ticketService.findAndHoldSeats(5, "saurabh1312@gmail.com");
        assertEquals(ticketService.numSeatsAvailable(), (15 * 15) - 8);
    }

    public void testDifferentEmail() {
        System.out.println("Running testDifferentEmail...");
        SeatHold seatHold = ticketService.findAndHoldSeats(3, "saurabh1312@gmail.com");
        String reservationCode = ticketService.reserveSeats(seatHold.seatHoldId, "1312saurabh@gmail.com");
        assertEquals(reservationCode, "Customer email doesn't match");
    }

    public void testSeatsExpiry() throws InterruptedException {
        System.out.println("Running testSeatsExpiry...");
        int initialSeats = ticketService.numSeatsAvailable();
        SeatHold seatHold = ticketService.findAndHoldSeats(3, "saurabh1312@gmail.com");
        int afterHoldSeats = ticketService.numSeatsAvailable();
        assertEquals(afterHoldSeats, initialSeats - 3);
        TimeUnit.SECONDS.sleep(20);
        int afterExpirySeats = ticketService.numSeatsAvailable();
        assertEquals(afterExpirySeats, initialSeats);
    }

    public void testReservedSeatsDontExpire() throws InterruptedException {
        System.out.println("Running testReservedSeatsDontExpire...");
        int initialSeats = ticketService.numSeatsAvailable();
        SeatHold seatHold = ticketService.findAndHoldSeats(3, "saurabh1312@gmail.com");
        String reservationCode = ticketService.reserveSeats(seatHold.seatHoldId, "saurabh1312@gmail.com");
        int afterReserveSeats = ticketService.numSeatsAvailable();
        assertEquals(afterReserveSeats, initialSeats - 3);
        TimeUnit.SECONDS.sleep(20);
        int afterExpirySeats = ticketService.numSeatsAvailable();
        assertEquals(afterExpirySeats, afterReserveSeats);
    }
}
