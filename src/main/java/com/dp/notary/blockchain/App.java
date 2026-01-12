package com.dp.notary.blockchain;

//import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordLight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class App extends Application {

    private static App instance;
    private Stage stage;
    private ConfigurableApplicationContext springContext;
    // размеры окна для auth
    private static final double AUTH_W = 420;
    private static final double AUTH_H = 520;
    @Getter
    @Setter
    private String token = "";
    @Override
    public void init() {
        instance = this;
        springContext = new SpringApplicationBuilder(SApp.class)
                .run();
        try {
            Config.getInstance().loadConfig();
        }
        catch(Exception ignored) {}
    }

    public static App get() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {

//        Application.setUserAgentStylesheet(
//                new Dracula().getUserAgentStylesheet()
//        );
//
//        this.stage = stage;
//        stage.setTitle("Notary Blockchain UI");
//        showLogin();
//        stage.show();

        this.stage = stage;

        Application.setUserAgentStylesheet(
                new NordLight().getUserAgentStylesheet()
        );

        // Load FXML
//        Parent root = FXMLLoader.load(
//                getClass().getResource("/fxml/LoginView.fxml")
//        );

        // Create scene
//        Scene scene = new Scene(root);

        // Optional css above
//        scene.getStylesheets().add("/css/auth.css");
        showLogin();
        stage.setTitle("Notary Blockchain");
//        stage.setScene(scene);
        this.stage.show();
    }

    // Navigation

    public void showLogin() throws IOException {
        setScene(loadFXML("/fxml/LoginView.fxml"), AUTH_W, AUTH_H);
    }

    public void showSignup() throws IOException {
        setScene(loadFXML("/fxml/SignupView.fxml"), AUTH_W, AUTH_H);
    }

    // пока просто заглушка "после логина"
    public void showMain() throws IOException {
        setScene(loadFXML("/fxml/MainView.fxml"), 1000, 700);
    }

    // Inside helpers

    private void setScene(Parent root, double w, double h) {
        Scene scene = new Scene(root, w, h);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private Parent loadFXML(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        loader.setControllerFactory(springContext::getBean);
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void stop() {
        springContext.close();
    }

}
