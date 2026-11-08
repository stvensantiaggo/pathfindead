package pathfindead;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.ui.ControlPanel;
import pathfindead.ui.MapView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Crear la ciudad (el grafo)
        Graph city = CityFactory.createCity();

        // 2. El mapa y el panel de controles
        MapView mapa = new MapView(city, 940, 600);
        ControlPanel panel = new ControlPanel(city, mapa);

        // 3. BorderPane: el mapa en el centro y el panel a la derecha
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