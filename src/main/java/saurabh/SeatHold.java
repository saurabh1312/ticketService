package saurabh;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used for both holding and reserving seats
 */
public class SeatHold implements Runnable {

    private static final Integer SEAT_EXPIRY = 15; // seconds
    private static AtomicInteger HOLD_ID_GENERATOR = new AtomicInteger(1000);
    private static AtomicInteger RESERVATION_CODE_GENERATOR = new AtomicInteger(2000);

    public enum HoldStatus {
        HELD, RESERVED, EXPIRED
    }

    public Integer seatHoldId;
    public String customerEmail;
    public List<Seat> seats;
    public HoldStatus holdStatus;
    public Integer reservationCode;
    private TicketServiceImpl ticketService;

    public SeatHold(String customerEmail, List<Seat> seats, TicketServiceImpl ticketService) {
        seatHoldId = HOLD_ID_GENERATOR.getAndIncrement();
        this.customerEmail = customerEmail;
        this.seats = seats;
        this.ticketService = ticketService;
        this.holdStatus = HoldStatus.HELD;
    }

    public void run() {
        try {
            TimeUnit.SECONDS.sleep(SEAT_EXPIRY);
            if (this.holdStatus != HoldStatus.RESERVED) {
                ticketService.expireSeatHolds(seatHoldId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Integer reserveSeats() {
        if (!(holdStatus.equals(HoldStatus.EXPIRED))) {
            for (Seat seat : seats) {
                seat.reservationStatus = Seat.ReservationStatus.RESERVED;
            }
            holdStatus = HoldStatus.RESERVED;
            reservationCode = RESERVATION_CODE_GENERATOR.getAndIncrement();
        }
        return reservationCode;
    }
}
