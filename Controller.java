package com.hari.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColour1 = "#FF474C";
    private static final String discColour2 = "#00FFFF";

    private static String PLAYER_ONE = "Player one";
    private static String PLAYER_TWO = "Player Two";
    private boolean isPlayerOneTurn = true;

    private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMNS];

    @FXML
    public GridPane rootGridPane;
    @FXML
    public Pane insertedDisc;
    @FXML
    public Label playerName;
    @FXML
    public TextField playerOneTextField, playerTwoTextField;

    @FXML
    public Button setNamesButton;

    private boolean isAllowedToInsert = true;

    public void createPlayground(){
        setNamesButton.setOnAction(event->{
            PLAYER_ONE = playerOneTextField.getText();
            PLAYER_TWO = playerTwoTextField.getText();
            playerName.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });
        Shape rectangleHoles = new Rectangle((COLUMNS+1)* CIRCLE_DIAMETER, (ROWS+1)* CIRCLE_DIAMETER);

        for(int row=0; row<ROWS; row++){
            for(int col=0;col<COLUMNS;col++){
                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER /2);
                circle.setCenterX(CIRCLE_DIAMETER /2);
                circle.setCenterY(CIRCLE_DIAMETER /2);
                circle.setSmooth(true);

                circle.setTranslateX(col*(CIRCLE_DIAMETER +5)+ CIRCLE_DIAMETER /4);
                circle.setTranslateY(row*(CIRCLE_DIAMETER +5)+ CIRCLE_DIAMETER /4);

                rectangleHoles = Shape.subtract(rectangleHoles,circle);

            }
        }

        rectangleHoles.setFill(Color.WHITE);
        rootGridPane.add(rectangleHoles,0,1);

        List<Rectangle> rectangleList = new ArrayList<>();

        for(int col=0; col<COLUMNS; col++) {
            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col*(CIRCLE_DIAMETER +5)+ CIRCLE_DIAMETER / 4);
            rectangle.setOnMouseEntered(mouseEvent -> rectangle.setFill(Color.valueOf("#FFD70026")) );
            rectangle.setOnMouseExited(mouseEvent -> rectangle.setFill(Color.TRANSPARENT) );

            final int column = col;
            rectangle.setOnMouseClicked(mouseEvent -> {
                if(isAllowedToInsert){
                    isAllowedToInsert=false;
                    insertDisc(new Disc(isPlayerOneTurn),column);
                }
            });

            rectangleList.add(rectangle);

        }
        for (Rectangle rectangle : rectangleList) {
            rootGridPane.add(rectangle, 0, 1);
        }
    }
    private void insertDisc(Disc disc, int column){

        int row=ROWS-1;
        while(row>=0){
            if(getDiscIfPresent(row,column)==null)
                break;
            row--;
        }
        if(row<0)
            return;


        insertedDiscArray[row][column]=disc;
        insertedDisc.getChildren().add(disc);
        disc.setTranslateX(column *(CIRCLE_DIAMETER +5)+ CIRCLE_DIAMETER / 4);

        int currentRow = row;
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3),disc);
        transition.setToY(row*(CIRCLE_DIAMETER +5)+ CIRCLE_DIAMETER /4);
        transition.setOnFinished((actionEvent ->{
            isAllowedToInsert=true;
            if(gameEnded(currentRow,column)){
                gameOver();
                return;
            }

            isPlayerOneTurn=!isPlayerOneTurn;
            playerName.setText(isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO);
        } ));
        transition.play();
    }

    private boolean gameEnded(int row,int column){

        List<Point2D> verticalPoints = IntStream.rangeClosed(row-3,row+3)
                .mapToObj(r-> new Point2D(r,column))
                .collect(Collectors.toList());

        List<Point2D> horizontalPoints = IntStream.rangeClosed(column-3,column+3)
                .mapToObj(col-> new Point2D(row,col))
                .collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row-3, column+3);
        List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
                .mapToObj(i-> startPoint1.add(i,-i))
                .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row-3, column-3);
        List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
                .mapToObj(i-> startPoint2.add(i,i))
                .collect(Collectors.toList());


        boolean isEnded = checkCombinations(verticalPoints )|| checkCombinations(horizontalPoints)
                || checkCombinations(diagonal1Points)|| checkCombinations(diagonal2Points);

        return isEnded;
    }
    private boolean checkCombinations(List<Point2D> points) {
        int chain=0;
        for (Point2D point: points) {
            int rowIndexForArray = (int) point.getX();
            int columnIndexArray = (int) point.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexArray);
            if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn){
                chain++;
                if(chain==4){
                    return true;
                }
            }else {
                chain=0;
            }
        }
        return false;
    }

    private void gameOver() {
        String winner =isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO;
        System.out.println("Winner is "+ winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is "+winner);
        alert.setContentText("Want to play again? ");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yes,no);

        Platform.runLater(()->{
            Optional<ButtonType> buttonClicked = alert.showAndWait();
            if(buttonClicked.isPresent() && buttonClicked.get() == yes){
                resetGame();
            }else{
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertedDisc.getChildren().clear();
        for (int row=0; row< insertedDiscArray.length; row++){
            for(int column=0; column<insertedDiscArray[row].length; column++){
                insertedDiscArray[row][column]=null;
            }
        }
        isPlayerOneTurn=true;
        playerName.setText(PLAYER_ONE);

        createPlayground();
    }

    private Disc getDiscIfPresent(int row,int column){

        if(row>= ROWS || row<0 || column >= COLUMNS || column<0)
            return null;
        return insertedDiscArray[row][column];
    }

    private static class Disc extends Circle{
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER /2);
            setFill(isPlayerOneMove? Color.valueOf(discColour1):Color.valueOf(discColour2));
            setCenterX(CIRCLE_DIAMETER /2);
            setCenterY(CIRCLE_DIAMETER /2);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}