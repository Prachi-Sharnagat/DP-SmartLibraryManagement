package prototype;

/**
 * Prototype Design Pattern interface for deep-cloning library catalog items and templates.
 *
 * @param <T> The target clone type
 */
public interface Prototype<T> {
    T clone();
}
