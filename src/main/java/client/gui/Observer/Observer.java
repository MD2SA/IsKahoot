package client.gui.Observer;

@FunctionalInterface
public interface Observer {

    void update(Observed source);
}
