package World;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StartWorldBuilder extends Application {
    public static void main(String[] args) {launch(args);}
    private Stage primaryStage;
    private SettingsScene settingsScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Multi-Agent Surveillance Game - Settings");
        settingsScene = new SettingsScene(primaryStage);
        primaryStage.setScene(settingsScene.getSettingsScene());
        primaryStage.show();
        primaryStage.getScene();
    }

}
