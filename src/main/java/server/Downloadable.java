package server;

public interface Downloadable<P> {
    P execute(); // Downloads the file

    boolean isPoisoned(); // Feed to each thread to let it know work is done
}
