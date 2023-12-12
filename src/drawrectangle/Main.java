package drawrectangle;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main extends Application {

    private final ObservableList<Double> pointyStrokeType = FXCollections.observableArrayList(5d, 5d);
    private final ObservableList<Double> otherStrokeType = FXCollections.observableArrayList(20d, 20d);
    private final ObservableList<Double> defaultStrokeType = FXCollections.observableArrayList();

    private final ComboBox<ObservableList<Double>> lineStyleComboBox = new ComboBox<>();
    private final ColorPicker colorPicker = new ColorPicker(Color.RED);
    private final Label edgeColorLabel = new Label("Barva okraje:");
    private final Label lineStyleLabel = new Label("Styl čáry:");

    private final Pane pane = new Pane();

    private Point2D startPoint;
    private Point2D endPoint;

    private Rectangle rectangle;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();

        HBox buttonsHbox = new HBox();
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setBackground(new Background(new BackgroundFill(Color.web("ffff00"), null, null)));
        buttonsHbox.setSpacing(10);
        
        Callback<ListView<ObservableList<Double>>, ListCell<ObservableList<Double>>> callback =  (ListView<ObservableList<Double>> observableListListView) -> new ListCell<>(){
            @Override
            protected void updateItem(ObservableList<Double> doubles, boolean b) {
                super.updateItem(doubles, b);
                
                Label label = new Label();
                label.setTextFill(Color.BLACK);
                
                if (doubles == pointyStrokeType){
                    label.setText("Tečkována");
                }
                else if (doubles == otherStrokeType){
                    label.setText("Čárkována");
                }
                else {
                    label.setText("Implicitni");
                }
                
                setGraphic(label);
            }
        };

        lineStyleComboBox.setCellFactory(callback);

        lineStyleComboBox.setButtonCell(callback.call(null));

        lineStyleComboBox.setItems(FXCollections.observableArrayList(defaultStrokeType, pointyStrokeType, otherStrokeType));
        lineStyleComboBox.setValue(defaultStrokeType);

        buttonsHbox.getChildren().addAll(edgeColorLabel, colorPicker, lineStyleLabel, lineStyleComboBox);
        buttonsHbox.setPadding(new Insets(10));
        root.setBottom(buttonsHbox);

        Stop[] stops = {new Stop(0, Color.WHITE),
                        new Stop(0.5, Color.GREEN),
                        new Stop(1, Color.WHITE)

        };
        pane.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 0.5, 0, true, CycleMethod.REFLECT, stops),
                null, null)));


        pane.setOnMousePressed(event -> {if (event.getButton() == MouseButton.PRIMARY) {
            onMouseClicked(event.getX(), event.getY());
        }});
        pane.setOnMouseDragged(event -> {if (event.getButton() == MouseButton.PRIMARY) {
            onMouseDragged(event.getX(), event.getY());
        }});
        pane.setOnMouseReleased(event -> {if (event.getButton() == MouseButton.PRIMARY) {
            onMouseReleased(event.getX(), event.getY());
        }});

        pane.widthProperty().addListener(observable -> {clearPane();});
        pane.heightProperty().addListener(observable -> {clearPane();});

        root.setCenter(pane);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void clearPane(){
        pane.getChildren().clear();
    }

    private void onMouseClicked(double x, double y){
        startPoint = new Point2D(x, y);
    }

    private void onMouseDragged(double x, double y){
        endPoint = new Point2D(x, y);
        if (rectangle != null){
            pane.getChildren().remove(rectangle);
        }
        rectangle = createRectangle();
        pane.getChildren().add(rectangle);
    }

    private void onMouseReleased(double x, double y){
        endPoint = new Point2D(x, y);
        if (rectangle != null){
            pane.getChildren().remove(rectangle);
        }
        Rectangle finalRectangle = createRectangle();
        pane.getChildren().add(finalRectangle);
        rectangle = null;
    }

    private Rectangle createRectangle(){

        double x = 0;
        double y = 0;
        double width = 0;
        double height = 0;

        if (endPoint == null || startPoint == null){
            return new Rectangle(x, y, width, height);
        }

        if (endPoint.getX() > startPoint.getX() && endPoint.getY() < startPoint.getY()){
            
            height = 2 * (startPoint.getY() - endPoint.getY());
            width = 2 * (endPoint.getX() - startPoint.getX());
            y = endPoint.getY();
            x = startPoint.getX() -(endPoint.getX() - startPoint.getX());
        }
        else if (endPoint.getX() > startPoint.getX() && endPoint.getY() > startPoint.getY()){
            
            height = 2 * (endPoint.getY() - startPoint.getY());
            width = 2 * (endPoint.getX() - startPoint.getX());
            y = endPoint.getY() - height;
            x = endPoint.getX() - width;
        }
        else if (endPoint.getX() < startPoint.getX() && endPoint.getY() < startPoint.getY()){
            
            height = 2 * (startPoint.getY() - endPoint.getY());
            width = 2 * (startPoint.getX() - endPoint.getX());
            y = endPoint.getY();
            x = endPoint.getX();
        }
        else if (endPoint.getX() < startPoint.getX() && endPoint.getY() > startPoint.getY()){
            
            height = 2 * (endPoint.getY() - startPoint.getY());
            width = 2 * (startPoint.getX() - endPoint.getX());
            y = endPoint.getY() - height;
            x = endPoint.getX();
        }

        Rectangle result = new Rectangle(x, y, width, height);
        result.setFill(Color.LIGHTGRAY);
        result.setStrokeWidth(2);
        result.getStrokeDashArray().addAll(lineStyleComboBox.getValue());
        result.setStroke(colorPicker.getValue());

        result.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.SECONDARY){
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
              new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null){
                try {
                    result.setFill(new ImagePattern(new Image(new FileInputStream(file))));
                } catch (FileNotFoundException e){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Obrazek nelze nacist");
                    alert.showAndWait();
                }
            }

        });
        
        return result;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
