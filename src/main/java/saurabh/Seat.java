package saurabh;

import java.util.Arrays;
import java.util.List;

public class Seat {

    public enum Row {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O
    }

    public enum ReservationStatus {
        AVAILABLE, HELD, RESERVED
    }

    public static final int numSeatsPerRow = 15;

    /**
     * To figure out the "best" available seats
     * Middle rows and seats are good, corner ones are bad
     */
    public static List<Row> rowAppealRankedList = Arrays.asList(Row.H,
            Row.G, Row.I, Row.F, Row.J, Row.E, Row.K, Row.D, Row.L,
            Row.C, Row.M, Row.B, Row.N, Row.A, Row.O);

    public static List<Integer> seatAppealRankedList = Arrays.asList(8,
            7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15);

    public Seat (Row row, Integer seatNumber) {
        this.row = row;
        this.seatNumber = seatNumber;
        this.reservationStatus = ReservationStatus.AVAILABLE;
    }

    public Row row;
    public Integer seatNumber;
    public ReservationStatus reservationStatus;
}
