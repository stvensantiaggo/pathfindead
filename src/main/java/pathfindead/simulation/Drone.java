package pathfindead.simulation;

import pathfindead.model.Edge;
import pathfindead.model.Graph;
import pathfindead.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El dron de reparto (puntos 79 a 82 y 85).
 * Sabe dónde está, cuánta batería y carga tiene, y qué ruta debe seguir.
 */
public class Drone {

    public static final double MAX_BATTERY = 100;      // batería llena (%)
    public static final double BATTERY_PER_COST = 0.8; // % de batería gastado por cada unidad de costo
    public static final double SPEED = 2.5;            // casillas por segundo (para la animación)

    private final Graph city;
    private final Node base;        // centro de control: aquí se recarga
    private final int capacity;     // máximo de suministros que puede llevar

    private Node current;           // último lugar donde estuvo
    private double battery = MAX_BATTERY;
    private int cargo;              // suministros que lleva ahora

    private final List<Node> route = new ArrayList<>();   // ruta asignada (vacía si está quieto)
    private int nextIndex = 0;      // posición en la ruta del próximo lugar
    private double progress = 0;    // avance en el tramo actual: 0 = salió, 1 = llegó

    /** Puntos 79 a 82: el dron empieza en la base, con batería llena y carga completa. */
    public Drone(Graph city, Node base, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva: " + capacity);
        }
        this.city = city;
        this.base = base;
        this.capacity = capacity;
        this.current = base;
        this.cargo = capacity;
    }

    // ---------- Consultas ----------

    public Node getBase() { return base; }
    public Node getCurrent() { return current; }
    public double getBattery() { return battery; }
    public int getCargo() { return cargo; }
    public int getCapacity() { return capacity; }
    public double getProgress() { return progress; }

    public List<Node> getRoute() {
        return Collections.unmodifiableList(route);
    }

    /** true si tiene una ruta pendiente. */
    public boolean isMoving() {
        return !route.isEmpty() && nextIndex < route.size();
    }

    /** Próximo lugar de la ruta (o null si está quieto). */
    public Node getNext() {
        return isMoving() ? route.get(nextIndex) : null;
    }

    // ---------- Punto 85: asignar una ruta ----------

    /** La ruta debe empezar donde está el dron y cada tramo debe ser un corredor existente. */
    public void assignRoute(List<Node> path) {
        if (path.isEmpty() || !path.get(0).equals(current)) {
            throw new IllegalArgumentException("La ruta debe empezar en " + current.getId());
        }
        for (int i = 0; i + 1 < path.size(); i++) {
            if (edgeBetween(path.get(i), path.get(i + 1)) == null) {
                throw new IllegalArgumentException("No hay corredor entre "
                        + path.get(i).getId() + " y " + path.get(i + 1).getId());
            }
        }
        route.clear();
        route.addAll(path);
        nextIndex = 1;
        progress = 0;
        if (route.size() < 2) {   // ruta de un solo lugar: no hay que moverse
            route.clear();
        }
    }

    /** Batería que gastaría recorriendo esa ruta. */
    public double batteryNeededFor(List<Node> path) {
        double total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            Edge e = edgeBetween(path.get(i), path.get(i + 1));
            if (e != null) {
                total += e.getCost() * BATTERY_PER_COST;
            }
        }
        return total;
    }

    /** true si le alcanza la batería para esa ruta. */
    public boolean canFly(List<Node> path) {
        return batteryNeededFor(path) <= battery;
    }

    // ---------- Punto 86: avanzar por la ruta ----------

    /**
     * Avanza el dron durante "seconds" segundos a velocidad SPEED.
     * @return los lugares a los que llegó en este paso (normalmente 0 o 1).
     */
    public List<Node> update(double seconds) {
        List<Node> arrivals = new ArrayList<>();
        double toTravel = seconds * SPEED;   // casillas que puede recorrer en este paso

        while (isMoving() && toTravel > 0) {
            Node next = route.get(nextIndex);
            Edge edge = edgeBetween(current, next);
            double length = edge.getDistance();
            double left = (1 - progress) * length;   // lo que falta del tramo

            if (toTravel < left) {
                progress += toTravel / length;       // no alcanza a llegar
                toTravel = 0;
            } else {
                toTravel -= left;                    // llega y sigue con el sobrante
                arriveAt(next, edge);
                arrivals.add(next);
            }
        }
        return arrivals;
    }

    /** Llega a un lugar: gasta batería del tramo y, si es la base, se recarga. */
    private void arriveAt(Node next, Edge edge) {
        battery = Math.max(0, battery - edge.getCost() * BATTERY_PER_COST);
        current = next;
        progress = 0;
        nextIndex++;
        if (nextIndex >= route.size()) {   // terminó la ruta
            route.clear();
            nextIndex = 0;
        }
        if (current.equals(base)) {         // en la base: batería y carga completas
            battery = MAX_BATTERY;
            cargo = capacity;
        }
    }

    // ---------- Punto 89: entregar suministros ----------

    /**
     * Entrega al refugio todo lo que necesita (o lo que le quede al dron).
     * @return cuántos suministros entregó.
     */
    public int deliverTo(Shelter shelter) {
        if (!shelter.getNode().equals(current)) {
            throw new IllegalStateException("El dron no está en " + shelter.getNode().getId());
        }
        int given = shelter.receive(cargo);
        cargo -= given;
        return given;
    }

    /** Busca el corredor que une dos lugares (o null si no existe). */
    private Edge edgeBetween(Node a, Node b) {
        for (Edge e : city.getEdges(a)) {
            if (e.connects(b)) {
                return e;
            }
        }
        return null;
    }
}