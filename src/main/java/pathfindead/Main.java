package pathfindead;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.simulation.Drone;
import pathfindead.simulation.SupplyState;
import pathfindead.ui.ControlPanel;
import pathfindead.ui.MapView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // 1. La ciudad (el grafo)
        Graph city = CityFactory.createCity();

        // 2. Cada refugio necesita 4 suministros; el dron sale de C-01 con 10
        SupplyState supplies = new SupplyState(city, 4);
        Drone drone = new Drone(city, city.getNode("C-01"), 10);

        // 3. El mapa dibuja la ciudad, el dron y el estado de los refugios
        MapView mapa = new MapView(city, 940, 600);
        mapa.setSupplies(supplies);
        mapa.setDrone(drone);

        // 4. Panel de controles: ahora también controla el dron
        ControlPanel panel = new ControlPanel(city, mapa, drone, supplies);

        BorderPane root = new BorderPane();
        root.setCenter(mapa);
        root.setRight(panel);

        Scene escena = new Scene(root, 940 + 280, 600);
        stage.setTitle("Pathfindead");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}