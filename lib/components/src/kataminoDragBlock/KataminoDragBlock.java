    package kataminoDragBlock;

    import javafx.event.EventHandler;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.GridPane;
    import javafx.scene.layout.TilePane;
    import javafx.scene.paint.Color;
    import kataminoDragCell.KataminoDragCell;

    import java.io.IOException;
    import java.util.ArrayList;

    public class KataminoDragBlock extends TilePane {

        // node position
        private double x = 0;
        private double y = 0;
        // mouse position
        private double mousex = 0;
        private double mousey = 0;
        private boolean dragging = false;
        private boolean moveToFront = false;

        private ArrayList<Color> colorList = new ArrayList<Color>(){{
            add(Color.web("FF3B30"));
            add(Color.web("FF9500"));
            add(Color.web("FFCC00"));
            add(Color.web("4CD964"));
            add(Color.web("5AFAFA"));
            add(Color.web("007AFF"));
            add(Color.web("5856D6"));
            add(Color.web("FF2D55"));
            add(Color.web("8B572A"));
            add(Color.web("B8E986"));
            add(Color.web("BD10E0"));
            add(Color.web("FF5700"));
        }};

        private int[][] grid;

        @FXML
        private TilePane dragBlockGrid;

        public KataminoDragBlock() throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("kataminoDragBlock.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();

            init();
        }

        public void setPentomino(int[][] grid) {
            this.grid = grid;

            for (int i= 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    KataminoDragCell currentCell = (KataminoDragCell) dragBlockGrid.getChildren().get((i*22)+j);
                    Color cellColor = ((grid[i][j] % 12) >= 0 && (grid[i][j] != 0)) ? colorList.get(grid[i][j] % 12) : Color.TRANSPARENT;
                    currentCell.customizeCell(grid[i][j], grid[i][j] != 0, cellColor);

                    if (currentCell.getPentominoInstanceID() <= 0) {
                        currentCell.setBorderColor(Color.TRANSPARENT);
                        currentCell.setBlocked(false);
                    }
                }
            }
        }

        public int[][] getGrid() {
            return grid;
        }

        private void init() {
            for (Node node : dragBlockGrid.getChildren()) {
                KataminoDragCell currentCell = (KataminoDragCell) node;
                currentCell.setCellColor(Color.TRANSPARENT);
            }

            onMousePressedProperty().set(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                    // record the current mouse X and Y position on Node
                    mousex = event.getSceneX();
                    mousey = event.getSceneY();

                    x = getLayoutX();
                    y = getLayoutY();

                    if (isMoveToFront()) {
                        toFront();
                    }
                }
            });

            //Event Listener for MouseDragged
            onMouseDraggedProperty().set(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                        // Get the exact moved X and Y

                        double offsetX = event.getSceneX() - mousex;
                        double offsetY = event.getSceneY() - mousey;

                        x += offsetX;
                        y += offsetY;

                        double scaledX = x;
                        double scaledY = y;

                        setLayoutX(scaledX);
                        setLayoutY(scaledY);

                        dragging = true;

                        // again set current Mouse x AND y position
                        mousex = event.getSceneX();
                        mousey = event.getSceneY();

                        event.consume();
                }
            });

            onMouseClickedProperty().set(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                    dragging = false;
                }
            });

        }

        /**
         * @return the dragging
         */
        protected boolean isDragging() {
            return dragging;
        }

        /**
         * @param moveToFront the moveToFront to set
         */
        public void setMoveToFront(boolean moveToFront) {
            this.moveToFront = moveToFront;
        }

        /**
         * @return the moveToFront
         */
        public boolean isMoveToFront() {
            return moveToFront;
        }

        public void removeNode(Node n) {
            getChildren().remove(n);
        }
    }
