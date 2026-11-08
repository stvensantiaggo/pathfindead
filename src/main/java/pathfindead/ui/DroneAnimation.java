package pathfindead.ui;

import javafx.animation.AnimationTimer;
import pathfindead.model.Node;
import pathfindead.simulation.Drone;
import pathfindead.simulation.Shelter;
import pathfindead.simulation.SupplyState;

import java.util.function.Consumer;

/**
 * Punto 87: anima el vuelo del dron con JavaFX.
 * AnimationTimer llama a handle() unas 60 veces por segundo; en cada llamada
 * el dron avanza un poquito, se revisa si llegó a algún lugar y se redibuja el mapa.
 */
public class DroneAnimation extends AnimationTimer {

    private final Drone drone;
    private final SupplyState supplies;
    private final MapView map;
    private final Consumer<String> onEvent;   // qué hacer con cada mensaje (mostrarlo en el panel)
    private final Runnable onFrame;           // qué hacer en cada cuadro (actualizar el estado)

    private long lastTime = 0;                // momento del cuadro anterior (en nanosegundos)

    public DroneAnimation(Drone drone, SupplyState supplies, MapView map,
                          Consumer<String> onEvent, Runnable onFrame) {
        this.drone = drone;
        this.supplies = supplies;
        this.map = map;
        this.onEvent = onEvent;
        this.onFrame = onFrame;
    }

    @Override
    public void start() {
        lastTime = 0;
        super.start();
    }

    /** Se ejecuta en cada cuadro de la animación. */
    @Override
    public void handle(long now) {
        if (lastTime == 0) {          // primer cuadro: solo se guarda la hora
            lastTime = now;
            return;
        }
        // Segundos desde el cuadro anterior (máximo 0.1 para evitar saltos si la ventana se congela)
        double seconds = Math.min((now - lastTime) / 1_000_000_000.0, 0.1);
        lastTime = now;

        // Punto 86: avanzar. Punto 88: revisar a qué lugares llegó
        for (Node arrived : drone.update(seconds)) {
            onArrival(arrived);
        }

        map.draw();
        onFrame.run();

        if (!drone.isMoving()) {
            stop();
            onEvent.accept("Fin del vuelo en " + drone.getCurrent().getId() + ".");
        }
    }

    /** Puntos 88, 89 y 90: al llegar a un refugio entrega; al llegar a la base se recarga. */
    private void onArrival(Node node) {
        if (supplies.isShelter(node)) {
            Shelter shelter = supplies.get(node);
            int given = drone.deliverTo(shelter);
            if (given > 0) {
                onEvent.accept("Entregó " + given + " en " + node.getId()
                        + " (" + shelter.getReceived() + "/" + shelter.getNeed() + ").");
            } else if (shelter.isSupplied()) {
                onEvent.accept(node.getId() + " ya estaba abastecido.");
            } else {
                onEvent.accept("Sin carga para " + node.getId() + ".");
            }
        } else if (node.equals(drone.getBase())) {
            onEvent.accept("Recarga completa en la base.");
        }
    }
}