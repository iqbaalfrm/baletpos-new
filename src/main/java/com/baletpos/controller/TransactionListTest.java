package com.baletpos.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TransactionListTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the transaction list FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/transaction_list.fxml"));
        
        Scene scene = new Scene(root, 1200, 800);
        
        primaryStage.setTitle("Transaction List - Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

