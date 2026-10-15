package mediator;

/**
 * Abstract Colleague participant in the Mediator Design Pattern.
 * Holds reference to the coordinating LibraryMediator.
 */
public abstract class Colleague {
    protected LibraryMediator mediator;

    public void setMediator(LibraryMediator mediator) {
        this.mediator = mediator;
    }
}
