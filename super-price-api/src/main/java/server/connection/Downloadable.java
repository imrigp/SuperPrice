package server.connection;

public interface Downloadable<P> {
    P download();

    boolean isPoisoned();
}
