package server;

public interface Downloadable<P> {
    P download();

    boolean isPoisoned();
}
