package mediator;

/**
 * Mediator Design Pattern interface.
 * Centralizes and coordinates circulation workflows across circulation, reservation,
 * fine calculation, and multi-channel notifications while preventing circular loops.
 */
public interface LibraryMediator {
    ReturnCoordinationResult coordinateReturn(String callerUserId, String resourceId);
    void registerCirculationColleague(CirculationColleague colleague);
    void registerReservationColleague(ReservationColleague colleague);
    void registerFineColleague(FineColleague colleague);
    void registerNotificationColleague(NotificationColleague colleague);
}
