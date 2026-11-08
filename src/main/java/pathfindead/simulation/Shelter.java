package pathfindead.simulation;

import pathfindead.model.Node;

/** Estado de un refugio: cuántos suministros necesita y cuántos ha recibido. */
public class Shelter {

    private final Node node;
    private final int need;      // suministros que necesita en total
    private int received = 0;    // suministros recibidos hasta ahora

    public Shelter(Node node, int need) {
        if (need <= 0) {
            throw new IllegalArgumentException("La necesidad debe ser positiva: " + need);
        }
        this.node = node;
        this.need = need;
    }

    public Node getNode() { return node; }
    public int getNeed() { return need; }
    public int getReceived() { return received; }

    /** Suministros que todavía le faltan. */
    public int remaining() {
        return need - received;
    }

    /** true cuando ya recibió todo lo que necesitaba. */
    public boolean isSupplied() {
        return received >= need;
    }

    /**
     * Punto 90: recibe una entrega. Nunca acepta más de lo que le falta.
     * @return cuántos suministros aceptó realmente.
     */
    public int receive(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cantidad negativa: " + amount);
        }
        int accepted = Math.min(amount, remaining());
        received += accepted;
        return accepted;
    }
}