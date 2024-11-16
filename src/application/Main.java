package application;

import javafx.application.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Random;

public class Main extends Application {
    private int GRID_SIZE = 15; 
    private int MINE_COUNT = 20; 

    Cell[][] cells;
    boolean gameOver = false;
    int score = 0;
    int timeElapsed;
    
    Label scoreLabel;
    Label timerLabel;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        
        Stage setupStage = new Stage();
        
        Label difficultyLabel = new Label("SELECT DIFFICULTY:");
        difficultyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ComboBox<String> difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll("Easy", "Normal", "Hard", "Custom");
        difficultyComboBox.setValue("Normal");
        difficultyComboBox.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-color: #f7c6d4;");
        
        Label customLabel = new Label("Enter Grid Size and Mine Count:");
        TextField gridTextField = new TextField("15");
        TextField mineTextField = new TextField("20");
        gridTextField.setVisible(false);
        mineTextField.setVisible(false);
        customLabel.setVisible(false);
        
        gridTextField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-color: #f7c6d4; -fx-border-radius: 5px;");
        mineTextField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-background-color: #f7c6d4; -fx-border-radius: 5px;");
        
        Button startButton = new Button("PLAY");
        startButton.setPrefWidth(100);
        startButton.setStyle("-fx-font-size: 16px; -fx-background-color: white; -fx-text-fill: #FF7F7F; -fx-padding: 10px; -fx-border-radius: 100px;");
        startButton.setOnAction(e -> {
            String selectedDifficulty = difficultyComboBox.getValue();
            if (selectedDifficulty.equals("Easy")) {
                GRID_SIZE = 10;
                MINE_COUNT = 10;
            } else if (selectedDifficulty.equals("Normal")) {
                GRID_SIZE = 15;
                MINE_COUNT = 20;
            } else if (selectedDifficulty.equals("Hard")) {
                GRID_SIZE = 10;
                MINE_COUNT = 20;
            } else if (selectedDifficulty.equals("Custom")) {
                try {
                    GRID_SIZE = Integer.parseInt(gridTextField.getText());
                    MINE_COUNT = Integer.parseInt(mineTextField.getText());
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter valid numbers for grid size and mine count.");
                    alert.showAndWait();
                    return;
                }
            }
            setupStage.close();
            setupGame(primaryStage);
        });

        difficultyComboBox.setOnAction(e -> {
            if (difficultyComboBox.getValue().equals("Custom")) {
                customLabel.setVisible(true);
                gridTextField.setVisible(true);
                mineTextField.setVisible(true);
            } else {
                customLabel.setVisible(false);
                gridTextField.setVisible(false);
                mineTextField.setVisible(false);
            }
        });

        Label gameTitle = new Label("MINESWEEPER.");
        gameTitle.setStyle("-fx-padding: 10; -fx-font-weight:bold; -fx-font-size:40px; -fx-text-fill: white;");
        difficultyComboBox.setStyle("-fx-padding: 2; -fx-font-size: 14px;");
        
        
        VBox setupLayout = new VBox(10, gameTitle, startButton, difficultyLabel, difficultyComboBox, customLabel, gridTextField, mineTextField);
        setupLayout.setStyle("-fx-padding: 20; -fx-background-color: #FF7F7F; -fx-border-radius: 15px;");
        setupLayout.setAlignment(Pos.CENTER);
        setupStage.setScene(new Scene(setupLayout, 400, 500));
        setupStage.setTitle("Minesweeper Setup");
        setupStage.show();
    }

    private void setupGame(Stage primaryStage) {
        BorderPane root = new BorderPane();
        GridPane grid = new GridPane();
        
        cells = new Cell[GRID_SIZE][GRID_SIZE];

        scoreLabel = new Label("SCORE 			: " + score);
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        timerLabel = new Label("TIME ELAPSED 	: 0s");
        timerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox stats = new VBox(10, scoreLabel, timerLabel);
        stats.setStyle("-fx-padding: 15; -fx-border-color: black; -fx-border-width: 3; -fx-background-color: #FF7F7F;");
        stats.setMaxWidth(200);

        root.setCenter(grid);
        root.setRight(stats);
        
        initializeCells(grid);
        
        TimerThread timer = new TimerThread();
        Thread timerThread = new Thread(timer);
        timerThread.start();
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();
    }

    class TimerThread implements Runnable {
        public void run() {
            try {
                while (!gameOver) {
                    Thread.sleep(1000);
                    timeElapsed++;
                    
                    javafx.application.Platform.runLater(() -> {
                        timerLabel.setText("TIME ELAPSED 	: " + timeElapsed + "s");
                        scoreLabel.setText("SCORE			: " + score);
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeCells(GridPane grid) {
        Random random = new Random();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col] = new Cell(row, col);
                grid.add(cells[row][col].button, col, row);
            }
        }

        int placedMines = 0;
        while (placedMines < MINE_COUNT) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (!cells[row][col].isMine) {
                cells[row][col].isMine = true;
                placedMines++;
            }
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (!cells[row][col].isMine) {
                    cells[row][col].adjacentMines = countAdjacentMines(row, col);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE && cells[r][c].isMine) {
                    count++;
                }
            }
        }
        return count;
    }

    private void reveal(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE || cells[row][col].isRevealed) {
            return;
        }
        cells[row][col].isRevealed = true;
        cells[row][col].button.setDisable(true);

        if (cells[row][col].isMine) {
            cells[row][col].button.setText("💣");
            cells[row][col].button.setStyle("-fx-background-color: #FF3333; -fx-text-fill: black; -fx-opacity: 1;");
            gameOver("Game Over! You hit a mine.\n");
        } else if (cells[row][col].adjacentMines > 0) {
            score += 100 / (timeElapsed);
            cells[row][col].button.setText(String.valueOf(cells[row][col].adjacentMines));
            cells[row][col].button.setStyle("-fx-text-fill: red; -fx-opacity: 1;");
        } else {
            score += 100 / (timeElapsed);
            cells[row][col].button.setText("");
            cells[row][col].button.setStyle("--fx-text-fill: red; -fx-opacity: 1;");
        
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    reveal(r, c);
                }
            }
        }
    }
    private void gameOver(String message) {
        gameOver = true;
        String link="application/boom.png";
        if(message.equals("Game Over! You hit a mine.\n")) {
        	link = "application/boom.png";
        	message += "SCORE : " + score;
        }
        else if(message.equals("Congrats ! You win \n")) {
        	link = "application/medal.png";
        	message += "SCORE : " + score;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Minesweeper");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Image image = new Image(link); 
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);  
        imageView.setFitHeight(50); 
        imageView.setPreserveRatio(true);
        
        alert.setGraphic(imageView);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].button.setDisable(true);
            }
        }
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK || response == ButtonType.CANCEL) {
                Platform.exit();  
            }
        });
    }

    private class Cell {
        int row, col;
        boolean isMine = false;
        boolean isRevealed = false;
        int adjacentMines = 0;
        Button button;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
            button = new Button();
            button.setPrefSize(40, 40);
            button.setOnAction(e -> {
                if (gameOver) return;
                reveal(row, col);
                if (checkWin()) {
                    gameOver("Congrats ! You win \n");
                }
            });
            button.setTextFill(Color.RED);
            button.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1; -fx-background-color : FFFF");
        }
    }

    private boolean checkWin() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (!cells[row][col].isMine && !cells[row][col].isRevealed) {
                    return false;
                }
            }
        }
        return true;
    }
}
