package pathfindead;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pathfindead.model.CityFactory;
import pathfindead.model.Graph;
import pathfindead.ui.MapView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Crear la ciudad (el grafo con sus 14 lugares y 24 conexiones)
        Graph city = CityFactory.createCity();

        // 2. Crear el lienzo que la dibuja (el dibujo se ajusta a este tamaño)
        MapView mapa = new MapView(city, 940, 600);

        // 3. Mostrarlo en la ventana
        Scene escena = new Scene(new StackPane(mapa), 940, 600);
        stage.setTitle("Pathfindead");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}