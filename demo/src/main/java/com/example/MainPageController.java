package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class MainPageController {
    @FXML
    public Scene mainPage;

    @FXML
    private TextField alphaCoeff;

    @FXML
    private Button applyButton;

    @FXML
    private Button autoModeButton;

    @FXML
    private TextField betaCoeff;

    @FXML
    private TextField lambdaCoeff;

    @FXML
    private TextField numberOfBuffers;

    @FXML
    private TextField numberOfDevices;

    @FXML
    private TextField numberOfSources;

    @FXML
    private Button stepModeButton;

    private int sources;

    private int devices;

    private int buffers;

    private double alpha;

    private double beta;

    private double lambda; 

    private void showWindows(String fxmlPath, Object controller, int sources, int devices, int buffers, double alpha, double beta, double lambda) throws IOException {
        Stage stage = (Stage) mainPage.getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = loader.load();
        stage.setScene(scene);
        stage.centerOnScreen();
    
        if (controller instanceof AutoModeController) {
            AutoModeController autoModeController = loader.getController();
            autoModeController.sendStatistic(new Simulation(sources, devices, buffers, alpha, beta, lambda, true).getStatistic());
        } else if (controller instanceof StepModeController) {
            StepModeController stepModeController = loader.getController();
            stepModeController.sendStatistic(new Simulation(sources, devices, buffers, alpha, beta, lambda, true).getStatistic());
        }
    
        stage.show();
    }

    @FXML
    public void applyParameters() throws IOException {
        this.sources = Integer.parseInt(numberOfSources.getText());
        this.devices = Integer.parseInt(numberOfDevices.getText());
        this.buffers = Integer.parseInt(numberOfBuffers.getText());
        this.alpha = Double.parseDouble(alphaCoeff.getText());
        this.beta = Double.parseDouble(betaCoeff.getText());
        this.lambda = Double.parseDouble(lambdaCoeff.getText());
    }
    

    @FXML
    public void autoModeAction() throws IOException {
        showWindows("auto_mode_page.fxml", new AutoModeController(), sources, devices, buffers, alpha, beta, lambda);
    }
    
    @FXML
    public void stepModeAction() throws IOException {
        showWindows("step_mode_page.fxml", new StepModeController(), sources, devices, buffers, alpha, beta, lambda);
    }
    
}
