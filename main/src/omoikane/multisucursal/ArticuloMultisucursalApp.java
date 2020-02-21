package omoikane.multisucursal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import omoikane.principal.Principal;
import phesus.configuratron.model.Configuration;
import phesus.configuratron.model.ConfigurationDao;

public class ArticuloMultisucursalApp extends Application {

    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ArticuloMultisucursalApp.class);

    FXMLLoader fxmlLoader;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Principal.urlStatusWS = "http://localhost:8080";
        Principal.userStatusWS = "";
        Principal.passStatusWS = "";
        Application.launch(ArticuloMultisucursalApp.class, (java.lang.String[])null);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Scene scene = initConfigurator();
            primaryStage.setScene(scene);
            primaryStage.setTitle("Artículo multisucursal");
            primaryStage.show();
        } catch (Exception ex) {
            logger.error( ex.getMessage(), ex );
        }
    }

    public Scene initConfigurator() throws IOException {
        fxmlLoader = new FXMLLoader(getClass().getResource("articulo-multisucursal-view.fxml"));
        AnchorPane page = (AnchorPane) fxmlLoader.load();
        Scene scene = new Scene(page);
        ArticuloMultisucursalController am = fxmlLoader.<ArticuloMultisucursalController>getController();
        am.loadData("7501298214986");

        return scene;
    }

    public FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }
}