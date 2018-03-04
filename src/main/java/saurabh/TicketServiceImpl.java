package saurabh;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ticket Service Implementation
 * @author Saurabh Singh
 */
public class TicketServiceImpl implements TicketService {

    private Map<Seat.Row, List<Seat>> venue;
    private int numAvailableSeats;
    private Map<Integer, SeatHold> seatHolds;
    private ExecutorService executor;

    public TicketServiceImpl() {
        venue = new HashMap<Seat.Row, List<Seat>>();
        seatHolds = new HashMap<Integer, SeatHold>();

        for (Seat.Row row : Seat.Row.values()) {
            List<Seat> rowSeats = new ArrayList<Seat>();
            for (int i = 1; i <= Seat.numSeatsPerRow; i++) {
                rowSeats.add(new Seat(row, i));
            }
            venue.put(row, rowSeats);
        }
        numAvailableSeats = Seat.numSeatsPerRow * Seat.Row.values().length;
        executor = Executors.newCachedThreadPool();
    }

    public int numSeatsAvailable() {
        return numAvailableSeats;
    }

    /**
     * Finds the best available seats and holds them. The "best" seats are
     * determined by appeal ranked lists in Seat.java
     * It's synchronized to avoid multiple holds on the same seats.
     *
     * @param numSeats the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return
     */
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {

        if (numAvailableSeats < numSeats) {
            return null;
        }

        for (Seat.Row row : Seat.rowAppealRankedList) {
            List<Seat> thisRow = venue.get(row);
            for (Integer seatNumber : Seat.seatAppealRankedList) {
                seatNumber--; // index = seatNumber - 1
                if ((seatNumber + numSeats) > Seat.numSeatsPerRow) {
                    continue;
                }

                List<Seat> seats = new ArrayList<Seat>();
                for (int i = 0; i < numSeats; i++) {
                    if (thisRow.get(seatNumber + i).reservationStatus.equals(Seat.ReservationStatus.AVAILABLE)) {
                        seats.add(thisRow.get(seatNumber + i));
                    } else {
                        seats = new ArrayList<Seat>();
                    }
                }

                if (seats.size() == numSeats) {
                    return holdSeats(customerEmail, seats);
                }
            }
        }

        /**
         * If we are here, it means seats are available, but not together
         * Just assign separate seats, in this case
         */
        List<Seat> seats = new ArrayList<Seat>();
        for (Seat.Row row : Seat.rowAppealRankedList) {
            List<Seat> thisRow = venue.get(row);
            for (Integer seatNumber : Seat.seatAppealRankedList) {
                seatNumber--; // index = seatNumber - 1
                if (thisRow.get(seatNumber).reservationStatus.equals(Seat.ReservationStatus.AVAILABLE) ) {
                    seats.add(thisRow.get(seatNumber));
                    if (seats.size() == numSeats) {
                        return holdSeats(customerEmail, seats);
                    }
                }
            }
        }

        // We shouldn't reach here ever.
        return null;
    }

    /**
     * Adds to seatHold map, updates available seats and starts the clock
     * to expire seat holds.
     * @param customerEmail
     * @param seats
     * @return
     */
    private SeatHold holdSeats(String customerEmail, List<Seat> seats) {
        for (Seat seat : seats) {
            seat.reservationStatus = Seat.ReservationStatus.HELD;
        }
        SeatHold seatHold = new SeatHold(customerEmail, seats, this);
        seatHolds.put(seatHold.seatHoldId, seatHold);
        numAvailableSeats -= seats.size();
        executor.execute(seatHold);
        return seatHold;
    }

    /**
     * Time is up! Remove the holds on these seats. This method is called from
     * another thread in SeatHold.java
     * @param seatHoldId
     */
    public void expireSeatHolds(int seatHoldId) {
        SeatHold seatHold = seatHolds.get(seatHoldId);
        for (Seat seat : seatHold.seats) {
            seat.reservationStatus = Seat.ReservationStatus.AVAILABLE;
        }
        numAvailableSeats += seatHold.seats.size();
        seatHold.holdStatus = SeatHold.HoldStatus.EXPIRED; // Just to be safe
        seatHolds.remove(seatHoldId);
        System.out.printf("Seat Hold ID %d expired\n", seatHoldId);
    }

    /**
     * Reserves the previously held seats
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *  seat hold is assigned
     * @return Reservation code or error message
     */
    public String reserveSeats(int seatHoldId, String customerEmail) {
        SeatHold seatHold = seatHolds.get(seatHoldId);
        if (seatHold == null) {
            return "Seat hold expired!";
        }

        if (!(seatHold.customerEmail.equals(customerEmail))) {
            return "Customer email doesn't match";
        }

        Integer reservationCode = seatHold.reserveSeats();
        if (reservationCode < 0) {
            return "Seat hold expired!";
        } else {
            return reservationCode.toString();
        }
    }

    public void printVenueMap() {
        System.out.println();
        for (Seat.Row row : Seat.Row.values()) {
            List<Seat> rowSeats = venue.get(row);
            for (int i = 0; i < Seat.numSeatsPerRow; i++) {
                if (rowSeats.get(i).reservationStatus.equals(Seat.ReservationStatus.RESERVED)) {
                    System.out.print("R.");
                } else if (rowSeats.get(i).reservationStatus.equals(Seat.ReservationStatus.HELD)) {
                    System.out.print("H.");
                } else {
                    System.out.print("A.");
                }
            }
            System.out.println();
        }
    }

    public static void main( String[] args) {
        TicketServiceImpl ticketService = new TicketServiceImpl();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your email: ");
        String email = scanner.nextLine();
        if (email.length() == 0) {
            email = "saurabh1312@gmail.com";
        }

        while (true) {
            System.out.printf("\nNumber of available seats: %d", ticketService.numAvailableSeats);
            System.out.println("\n1. Hold seats\n2. Reserve seats" +
                    "\n3. Print seat map\n4. Exit");
            int userAction = scanner.nextInt();

            switch(userAction) {
                case 1 :
                    System.out.println("Enter number of seats: ");
                    int numSeats = scanner.nextInt();
                    SeatHold seatHold = ticketService.findAndHoldSeats(numSeats, email);
                    ticketService.printVenueMap();
                    System.out.printf("Seat hold ID: %d\n", seatHold.seatHoldId);
                    break;

                case 2 :
                    System.out.println("Enter seat hold ID: ");
                    int seatHoldId = scanner.nextInt();
                    String reservationCode = ticketService.reserveSeats(seatHoldId, email);
                    ticketService.printVenueMap();
                    System.out.printf("Confirmation: %s\n", reservationCode);
                    break;

                case 3 :
                    ticketService.printVenueMap();
                    break;

                default :
                    System.exit(0);
            }
        }

    }
}
