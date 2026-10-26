package client.gui.Observer;

import java.util.ArrayList;
import java.util.List;

public class Observed {

    private final List<Observer> observers = new ArrayList<Observer>();

    public synchronized void registerObserver(Observer o) {
        observers.add(o);
    }

    public synchronized void unregisterObserver(Observer o) {
        observers.remove(o);
    }

    public synchronized void notifyObservers() {
        for (Observer o : observers)
            o.update(this);
    }
}
