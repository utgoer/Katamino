/** Game controller controls the game board logic and tile actions.
 * @author Bartu Atabek
 * @version 1.0
 */

import java.io.FileNotFoundException;
import java.util.*;

import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import kataminoDragBlock.KataminoDragBlock;
import kataminoDragCell.KataminoDragCell;

public abstract class GameController implements Initializable {

    Game game;

    protected ArrayList<Color> colorList = new ArrayList<Color>() {{
        add(Color.web("FF3B30"));
        add(Color.web("FF9500"));
        add(Color.web("FFF700"));
        add(Color.web("4CD964"));
        add(Color.web("2D7D7D"));
        add(Color.web("007AFF"));
        add(Color.web("5856D6"));
        add(Color.web("9856D6"));
        add(Color.web("8B572A"));
        add(Color.web("B8E986"));
        add(Color.web("BD10E0"));
        add(Color.web("FFBB99"));
    }};

    KataminoDragCell kataminoDragCell;

    protected int currentPentominoId;

    protected ArrayList<ArrayList<Integer>> coordinateArr;

    Color hintIndicationColor = Color.ROSYBROWN;
    @FXML
    protected GridPane timerPane;

    @FXML
    protected TilePane gameTilePane;

    @FXML
    protected Label stopwatchLabel;

    @FXML
    protected Label playerLabel;

    @FXML
    protected AnchorPane gridStack;

    protected KataminoDragBlock preview;
    private FileManager fm;

    public boolean isFull() {
        ObservableList<Node> cells = gameTilePane.getChildren();
        for (int i = 0; i < cells.size(); i++) {
            KataminoDragCell currentPentomino = (KataminoDragCell) cells.get(i);
            if ((currentPentomino.isOnBoard() && (currentPentomino.getPentominoInstanceID() == -1)) || (currentPentomino.getPentominoInstanceID() == 0)) {
                return false;
            }
        }
        animate(true,true);
        return true;
    }

    public void gameOverAction() {
        System.out.println("Game Over!");
    }

    public boolean clashCheck(int row, int col) {
        //TODO:border bugs
        ArrayList<KataminoDragCell> newLocationCells = new ArrayList<>();
        Color cellColor = ((currentPentominoId % 12) >= 0 && (currentPentominoId != 0)) ? colorList.get(currentPentominoId % 12) : Color.web("#262626");
        int transferX = row - coordinateArr.get(0).get(0);
        int transferY = col - coordinateArr.get(0).get(1);
        for (ArrayList<Integer> array : coordinateArr) {
            array.set(0, array.get(0) + transferX);
            array.set(1, array.get(1) + transferY);
        }
        int index;
        for (int i = 0; i < coordinateArr.size(); i++) {
            index = (coordinateArr.get(i).get(0) * 22) + coordinateArr.get(i).get(1);
            if (index >= 0) {
                KataminoDragCell currentPentomino = (KataminoDragCell) gameTilePane.getChildren().get(index);
                if (currentPentomino.getPentominoInstanceID() == -1 || currentPentomino.getPentominoInstanceID() == 0) {
                    newLocationCells.add(currentPentomino);
                } else {
                    ShakeTransition shakeTransition = new ShakeTransition(gridStack);
                    shakeTransition.playFromStart();
                    return true;
                }
            }
        }
        for (KataminoDragCell currentCell : newLocationCells) {
            currentCell.setPentominoInstanceID(currentPentominoId);
            currentCell.setCellColor(cellColor);
        }
        return false;
    }

    public Integer[] findLocationTilePane(Node node, TilePane tilePane){
        for (int i = 0; i < tilePane.getPrefRows(); i++) {
            for(int j = 0; j < tilePane.getPrefColumns(); j++) {
                if(node == tilePane.getChildren().get((i * 22) + j)) {
                    Integer[] location = new Integer[2];
                    location[0] = i;
                    location[1] = j;
                    return location;
                }
            }
        }
        return null;
    }
    public void generateHint(KeyEvent event){
        Integer [][] solution;
        ArrayList<ArrayList<Integer>> hintCoords = new ArrayList<>();
        fm = new FileManager();
        try {
            solution = fm.loadSolution(((SinglePlayerGame)game).getCurrentLevel());
            if (event.getCode() == KeyCode.H) {
                for (int i = 0; i < solution.length; i++) {
                    for (int j = 0; j < solution[0].length; j++) {
                        ArrayList<Integer> coord = new ArrayList<>();
                        if (solution[i][j] == currentPentominoId) {
                            coord.add(i);
                            coord.add(j);
                            hintCoords.add(coord);
                        }
                    }
                }
            }
            displayHint(hintCoords);

        } catch (FileNotFoundException e) {
            System.out.println("No solution for level");
    }

        }
    public void displayHint(ArrayList<ArrayList<Integer>> coords) {
        Color[] prevColors = new Color[coords.size()];
        ObservableList<Node> cells = gameTilePane.getChildren();
        final int[] ind = {0};
        for (ArrayList<Integer> coord : coords) {
            KataminoDragCell currentPentomino = (KataminoDragCell) gameTilePane.getChildren().get((coord.get(0) * 22) + coord.get(1));
            prevColors[ind[0]] = currentPentomino.getColor();
            ind[0]++;
            currentPentomino.setCellColor(hintIndicationColor);
        }

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        ind[0] = 0;
                        for (ArrayList<Integer> coord : coords){
                            KataminoDragCell currentPentomino = (KataminoDragCell) gameTilePane.getChildren().get((coord.get(0) * 22) + coord.get(1));
                            if(currentPentomino.getPentominoInstanceID() == 0)
                            {
                                currentPentomino.setCellColor(prevColors[ind[0]]);
                            }
                            ind[0]++;
                        }
                    }
                },
                2000
        );
    }


    public void generatePreview(MouseEvent e){
        if (((KataminoDragCell) e.getSource()).getPentominoInstanceID() > 0 && !gridStack.isVisible()) {
            try {
                preview.relocate(0, 0);
                int[][] children = findSiblings((Node) e.getSource());
                for (int i = 0; i < children.length; i++) {
                    for (int j = 0; j < children[0].length; j++) {
                        KataminoDragCell currentCell = (KataminoDragCell) preview.getChildren().get((i * 22) + j);
                        KataminoDragCell currentTileCell = (KataminoDragCell) gameTilePane.getChildren().get((i * 22) + j);
                        if (children[i][j] == 0) {
                            currentCell.setCellColor(Color.TRANSPARENT);
                            currentCell.setBorderColor(Color.TRANSPARENT);
                        } else {
                            currentTileCell.setCellColor(Color.web("#262626"));
                            currentTileCell.setPentominoInstanceID(-1);
                        }
                    }
                }

                preview.setPentomino(children);
                preview.setOpacity(0.5);
                preview.setLayoutY(preview.getLayoutY() + timerPane.getHeight());
                gridStack.setVisible(true);
                System.out.println(coordinateArr);
                gridStack.getScene().setOnKeyPressed(keyPressed);
            } catch (Exception exp) {
                System.out.println(exp);
            }
        }
    }

    public void updateStopwatch() {
        Timeline stopwatchChecker = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!game.isStopped()) {
                stopwatchLabel.setText(String.valueOf(String.format("%02d:%02d", game.getElapsedSeconds() / 60, game.getElapsedSeconds() % 60)));
            }
        }));
        stopwatchChecker.setCycleCount(Timeline.INDEFINITE);
        stopwatchChecker.play();
    }

    public void startGame() {
        game.startStopWatch();
        updateStopwatch();
    }

    public void pauseGame() {
        stopwatchLabel.setText("▶️" + stopwatchLabel.getText());
        game.pause();
    }

    public void resumeGame() {
        game.resume();
    }

    public int[][] pentominoTransform(KeyEvent e) {

        boolean crossesBorder = false;
        ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
        for(ArrayList<Integer> coord : coordinateArr){
            ArrayList<Integer> t_coord = new ArrayList<>();
            t_coord.add(coord.get(0));
            t_coord.add(coord.get(1));
            temp.add(t_coord);
        }
        System.out.println(temp);

        int grid[][] = new int[11][22];
        int smllsRow = 1000;
        int smllsCol = 1000;
        int bgstCol = 0;
        int bgstRow = 0;

        for (ArrayList coord : coordinateArr) {
            if ((int) coord.get(0) < smllsRow) {
                smllsRow = (int) coord.get(0);
            }
            if ((int) coord.get(1) < smllsCol) {
                smllsCol = (int) coord.get(1);
            }
            if ((int) coord.get(1) > bgstCol) {
                bgstCol = (int) coord.get(1);
            }
            if ((int) coord.get(0) > bgstRow) {
                bgstRow = (int) coord.get(0);
            }
        }
        int width = bgstCol - smllsCol + 1;
        int height = bgstRow - smllsRow + 1;
        switch (e.getCode()) {
            case W:{
                flipVertically(width, height, smllsRow, smllsCol, temp);
                break;
            }
            case S: {
                flipVertically(width, height, smllsRow, smllsCol, temp);
                break;
            }
            case A: {
                rotateLeft(width, height, smllsRow, smllsCol, temp);
                break;
            }

            case D:{
                rotateRight(width, height, smllsRow, smllsCol, temp);
                break;
            }
        }
        // find smallest column and row that is negative
        int smNegRow = 0;
        int smNegCol = 0;
        for (ArrayList<Integer> cell_coord : temp) {
            if (cell_coord.get(0) < smNegRow){
                smNegRow = cell_coord.get(0);
            }
            if(cell_coord.get(1) < smNegCol){
                smNegCol = cell_coord.get(1);
            }
        }

        System.out.println("smallest Negative Col: " + smNegCol);
        System.out.println("smallest Negative Row: " + smNegRow);

        int bigCrossingRow = 10;
        int bigCrossingCol = 21;
        for (ArrayList<Integer> cell_coord : temp) {
            if (cell_coord.get(0) > bigCrossingRow){
                bigCrossingRow = cell_coord.get(0);
            }
            if(cell_coord.get(1) > bigCrossingCol){
                bigCrossingCol = cell_coord.get(1);
            }
        }

        System.out.println("bigCrossing Col: " + bigCrossingCol);
        System.out.println("sbigCrossingRow: " + bigCrossingRow);

        for (ArrayList<Integer> cell_coord : temp) {
            cell_coord.set(0,  cell_coord.get(0) - (bigCrossingRow - 10)  - smNegRow);
            cell_coord.set(1,  cell_coord.get(1) - (bigCrossingCol - 21)  -  smNegCol) ;
        }


        System.out.println(temp);
        int ind = 0;
        for(ArrayList<Integer> coord : temp){
            ArrayList<Integer> t_coord = new ArrayList<>();
            t_coord.add(coord.get(0));
            t_coord.add(coord.get(1));
            coordinateArr.set(ind, t_coord);
            ind++;
        }
        for (ArrayList<Integer> coord : coordinateArr) {
            grid[coord.get(0)][coord.get(1)] = currentPentominoId;
        }
        return grid;
    }

    public void rotateRight(int width, int height, int smllsRow, int smllsCol, ArrayList<ArrayList<Integer>> tempCoords) {

        int origin_row = tempCoords.get(0).get(0);
        int origin_col = tempCoords.get(0).get(1);
        boolean corssesBorder = false;
        // move origin to 0,0
        for (ArrayList<Integer> coord : tempCoords) {
            int row = coord.get(0) - origin_row;
            int col = coord.get(1) - origin_col;
            int ncol = -row + origin_col; // clockwise
            int nrow = col + origin_row;
            coord.set(0, nrow);
            coord.set(1, ncol);
        }
    }

    public void rotateLeft(int width, int height, int smllsRow, int smllsCol, ArrayList<ArrayList<Integer>> tempCoords) {

        int origin_row = tempCoords.get(0).get(0);
        int origin_col = tempCoords.get(0).get(1);

        // move origin to 0,0
        for (ArrayList<Integer> coord : tempCoords) {
            int row = coord.get(0) - origin_row;
            int col = coord.get(1) - origin_col;
            int ncol = row + origin_col; // clockwise
            int nrow = -col + origin_row;
            coord.set(0, nrow);
            coord.set(1, ncol);
        }
    }

    public void flipVertically(int width, int height, int smllsRow, int smllsCol, ArrayList<ArrayList<Integer>> tempCoords) {
        int[][] pentomino = new int[height][width];
        ArrayList [][] pent2coord = new ArrayList [height][width];

        for (ArrayList coord : tempCoords) {
            pentomino[(int) coord.get(0) - smllsRow][(int) coord.get(1) - smllsCol] = currentPentominoId;
            pent2coord[(int) coord.get(0) - smllsRow][(int) coord.get(1) - smllsCol] = coord;
        }

        // Column Reverse
        for (int col = 0; col < pent2coord[0].length; col++) {
            for (int row = 0; row < pent2coord.length / 2; row++) {
                ArrayList temp = pent2coord[row][col];
                pent2coord[row][col] = pent2coord[pent2coord.length - row - 1][col];
                pent2coord[pent2coord.length - row - 1][col] = temp;
            }
        }

        // Fill coordinate array
        for (int i = 0; i < pent2coord.length; i++) {
            for (int j = 0; j < pent2coord[0].length; j++) {
                if (pent2coord[i][j] != null) {
                    ArrayList<Integer> n_coords = new ArrayList<>();
                    Integer newCoordx = i + smllsRow;
                    Integer newCoordy = j + smllsCol;
                    n_coords.add(newCoordx);
                    n_coords.add(newCoordy);
                    int ind = tempCoords.indexOf(pent2coord[i][j]);
                    if (ind != -1)
                        tempCoords.set(ind, n_coords);
                }
            }
        }
    }

    private int[][] transposeMatrix(int[][] matrix) {
        int[][] temp = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                temp[j][i] = matrix[i][j];
        return temp;
    }

    private int[][] flipHorizontally(int[][] matrix) {
        for (int j = 0; j < matrix.length; j++) {
            for (int i = 0; i < matrix[j].length / 2; i++) {
                int temp = matrix[j][i];
                matrix[j][i] = matrix[j][matrix[j].length - i - 1];
                matrix[j][matrix[j].length - i - 1] = temp;
            }
        }
        return matrix;
    }

    public void playPause(MouseEvent e) {
        if (game.isStopped()) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    EventHandler<KeyEvent> keyPressed = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            preview.setPentomino(pentominoTransform(event));
            if(game instanceof SinglePlayerGame &&((SinglePlayerGame) game).getGameScore() >= 10 && event.getCode() == KeyCode.H){
                ((SinglePlayerGame) game).getPlayer().setHighScore(((SinglePlayerGame) game).getPlayer().getHighScore() - 10);
                generateHint(event);
                playerLabel.setText(((SinglePlayerGame) game).getPlayer().getPlayerName() + " " + ((SinglePlayerGame) game).getPlayer().getHighScore());
            }
            event.consume();
        }
    };


    public int[][] findSiblings(Node source) {
        ArrayList<Node> oldNodes = new ArrayList<>();
        KataminoDragCell currentPentomino;
        kataminoDragCell = (KataminoDragCell) source;
        currentPentominoId = kataminoDragCell.getPentominoInstanceID();
        int pentominoInstanceID = ((KataminoDragCell) source).getPentominoInstanceID();
        ObservableList<Node> cells = gameTilePane.getChildren();
        Stack<Node> currentSearch = new Stack<>();
        int[][] currentShape = new int[11][22];
        currentSearch.push(source);
        Integer colIndex;
        Integer rowIndex;
        coordinateArr = new ArrayList<>();
        while (!currentSearch.empty()) {
            Integer[] location = findLocationTilePane(currentSearch.peek(), gameTilePane);
            rowIndex = location[0];
            colIndex = location[1];
            currentShape[rowIndex][colIndex] = pentominoInstanceID;
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(rowIndex);
            arrayList.add(colIndex);
            boolean has = false;
            for (ArrayList <Integer> coord: coordinateArr){
                if(arrayList.get(0).equals(coord.get(0)) && arrayList.get(1).equals(coord.get(1))){
                    has =true;
                }
            }
            if(!has){
                coordinateArr.add(arrayList);
            }
            oldNodes.add(currentSearch.pop());
            if (colIndex + 1 <= 21) {
                currentPentomino = (KataminoDragCell) cells.get(rowIndex * 22 + colIndex + 1);
                if (!oldNodes.contains(currentPentomino) && currentPentomino.getPentominoInstanceID() == pentominoInstanceID)
                    currentSearch.push(currentPentomino);
            }

            if (colIndex - 1 >= 0) {
                currentPentomino = (KataminoDragCell) cells.get(rowIndex * 22 + colIndex - 1);
                if (!oldNodes.contains(currentPentomino) && currentPentomino.getPentominoInstanceID() == pentominoInstanceID)
                    currentSearch.push(currentPentomino);
            }
            if (rowIndex + 1 <= 10) {
                currentPentomino = (KataminoDragCell) cells.get((rowIndex + 1) * 22 + colIndex);
                if (!oldNodes.contains(currentPentomino) && currentPentomino.getPentominoInstanceID() == pentominoInstanceID)
                    currentSearch.push(currentPentomino);
            }
            if (rowIndex - 1 >= 0) {
                currentPentomino = (KataminoDragCell) cells.get((rowIndex - 1) * 22 + colIndex);
                if (!oldNodes.contains(currentPentomino) && currentPentomino.getPentominoInstanceID() == pentominoInstanceID)
                    currentSearch.push(currentPentomino);
            }
        }
        return currentShape;
    }

    public static void print2D(int[][] mat) {
        // Loop through all rows
        for (int[] row : mat)
            System.out.println(Arrays.toString(row));
    }
    public void animate(boolean disappear, boolean gameOver){
        for(int i = 0; i < gameTilePane.getChildren().size() - 1; i++) {
            RotateTransition rotator = new RotateTransition(Duration.millis(1000),  gameTilePane.getChildren().get(i));
            if(disappear) {
                rotator.setFromAngle(0);
                rotator.setToAngle(120);
            }
            else{
                rotator.setFromAngle(120);
                rotator.setToAngle(0);
            }
            rotator.setInterpolator(Interpolator.EASE_BOTH);
            rotator.play();
        }
        RotateTransition rotator = new RotateTransition(Duration.millis(1000),  gameTilePane.getChildren().get(gameTilePane.getChildren().size() - 1));
        if(disappear) {
            rotator.setFromAngle(0);
            rotator.setToAngle(120);
        }
        else {
            rotator.setFromAngle(120);
            rotator.setToAngle(0);
        }
        rotator.setInterpolator(Interpolator.EASE_BOTH);
        if(gameOver){
            rotator.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    gameOverAction();
                    game.resetTime();
                }
            });
        }
        rotator.play();
    }
}
