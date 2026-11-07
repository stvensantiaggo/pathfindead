package pathfindead;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

    public class Main extends Application {

        @Override
        public void start(Stage stage) {
            Label etiqueta = new Label("Pathfindead");
            Scene escena = new Scene(new StackPane(etiqueta), 800, 600);
            stage.setTitle("Pathfindead");
            stage.setScene(escena);
            stage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }

