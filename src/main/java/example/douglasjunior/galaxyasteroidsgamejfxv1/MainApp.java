package example.douglasjunior.galaxyasteroidsgamejfxv1;

import static com.sun.javafx.perf.PerformanceTracker.getSceneTracker;
import static java.lang.System.exit;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import static javafx.animation.Animation.INDEFINITE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import static javafx.application.Platform.exit;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.SPACE;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import static javafx.scene.paint.Color.YELLOWGREEN;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import static javafx.util.Duration.millis;

public class MainApp extends Application {

    private static final Double SCREEN_WIDTH = 800.0;
    private static final Double SCREEN_HEIGHT = 600.0;

    private static final Double VELOCITY_MAX = 50.0;
    private static final Double VELOCITY_INCREMENT = 0.7;
    private static final Double VELOCITY_DECREMENT = 1.0;
    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keySpace = false;

    private boolean toLeft = false;
    private boolean toRight = false;
    private Double shipVelocity = 0.0;
    private ImageView ship;

    private final List<Rectangle> bullets = new ArrayList<>();
    private final List<ImageView> asteroids = new ArrayList<>();
    private Integer currentBullet = 0;

    private Pane scenario;

    private Timeline gameLoop;

    private final Image img1 = new Image("/drawable/rock1.png");
    private final Image img2 = new Image("/drawable/rock2.png");
    private final Image img3 = new Image("/drawable/rock3.png");

    private Integer dropBullet = 0;
    private Integer dropAsteroid = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest((WindowEvent event) -> {
            exit();
            exit(0);
        });

        scenario = new Pane();
        scenario.setCache(true);
        scenario.setStyle("-fx-background-color: rgb(10,10,50);");
        scenario.setFocusTraversable(true);

        scenario.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == LEFT) {
                keyLeft = true;
            } else if (event.getCode() == RIGHT) {
                keyRight = true;
            } else if (event.getCode() == SPACE) {
                keySpace = true;
            } else if (event.getCode() == ESCAPE) {
                exit();
                exit(0);
            }
        });
        scenario.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == LEFT) {
                keyLeft = false;
            } else if (event.getCode() == RIGHT) {
                keyRight = false;
            } else if (event.getCode() == SPACE) {
                keySpace = false;
            }
        });

        Scene scene = new Scene(scenario);
        stage.setScene(scene);
        stage.setWidth(SCREEN_WIDTH);
        stage.setHeight(SCREEN_HEIGHT);

        ship = new ImageView("/drawable/ship.png");
        scenario.getChildren().add(ship);

        ship.setX(stage.getWidth() / 2 - ship.getImage().getWidth() / 2);
        ship.setY(stage.getHeight() - ship.getImage().getHeight() * 2);

        for (int i = 0; i < 20; i++) {
            Rectangle b = new Rectangle(5, 20, YELLOWGREEN);
            b.setVisible(false);
            b.setCache(true);
            bullets.add(b);
        }

        //create a timeline for moving the circle
        gameLoop = new Timeline();
        gameLoop.setCycleCount(INDEFINITE);
        gameLoop.setAutoReverse(true);

        //create a keyFrame, the keyValue is reached at time 2s
        Duration duration = millis(10);
        //one can add a specific action when the keyframe is reached

        EventHandler onFinished = (EventHandler<ActionEvent>) (ActionEvent t) -> {
            updateGame();
        };

        KeyFrame keyFrame = new KeyFrame(duration, onFinished);

        //add the keyframe to the timeline
        gameLoop.getKeyFrames().add(keyFrame);

        stage.setWidth(SCREEN_WIDTH);
        stage.setHeight(SCREEN_HEIGHT);
        stage.show();
        gameLoop.play();
        scenario.requestFocus();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                out.println("FPS " + getSceneTracker(stage.getScene()).getInstantFPS());
            }
        }, 0, 1000);
    }

    private void updateGame() {
        updateShip();
        updateBullet();
        updateAsteroid();
        checkCollision();
    }

    private void updateShip() {
        if ((keyLeft && toRight) || (keyRight && toLeft)) {
            shipVelocity = 0.0;
        }

        if (keyLeft || keyRight) {
            shipVelocity += VELOCITY_INCREMENT;
            shipVelocity = shipVelocity > VELOCITY_MAX ? VELOCITY_MAX : shipVelocity;
        } else {
            shipVelocity -= VELOCITY_DECREMENT;
            shipVelocity = shipVelocity < 0 ? 0 : shipVelocity;
        }

        if (keyLeft) {
            toLeft = true;
            toRight = false;
        } else if (keyRight) {
            toLeft = false;
            toRight = true;
        }

        if (toLeft) {
            ship.setX(ship.getX() - shipVelocity);
            if (ship.getX() + ship.getImage().getWidth() < 0) {
                ship.setX(SCREEN_WIDTH);
            }
        } else if (toRight) {
            ship.setX(ship.getX() + shipVelocity);
            if (ship.getX() > SCREEN_WIDTH) {
                ship.setX(-ship.getImage().getWidth());
            }
        }
    }

    private void updateBullet() {
        if (dropBullet > 10 || !keySpace) {
            dropBullet = 0;
        }
        if (keySpace && dropBullet == 0) {

            if (currentBullet >= bullets.size()) {
                currentBullet = 0;
            }

            Rectangle b = bullets.get(currentBullet);

            if (!scenario.getChildren().contains(b)) {
                scenario.getChildren().add(b);
                b.setVisible(true);

                b.setY(ship.getY());
                b.setX(ship.getX() + ship.getImage().getWidth() / 2);

                currentBullet++;
            }
        }

        for (Rectangle b : bullets) {
            if (b.isVisible()) {
                b.setY(b.getY() - 10);

                if (b.getY() + b.getHeight() < 0) {
                    b.setVisible(false);
                    scenario.getChildren().remove(b);
                }
            }
        }
        if (keySpace) {
            dropBullet++;
        }
    }

    private void updateAsteroid() {
        Random rand = new Random();
        if (dropAsteroid > 40) {
            dropAsteroid = 0;
        }
        if (asteroids.size() < 20 && dropAsteroid == 0 && rand.nextBoolean()) {

            ImageView a;
            switch (rand.nextInt(3) + 1) {
                case 1:
                    a = new ImageView(img1);
                    break;
                case 2:
                    a = new ImageView(img2);
                    break;
                default:
                    a = new ImageView(img3);
                    break;
            }
            a.setY(-a.getImage().getHeight());
            a.setX(rand.nextInt(SCREEN_WIDTH.intValue() - (int) a.getImage().getWidth()) + 1.0);

            asteroids.add(a);
            scenario.getChildren().add(0, a);
        }

        for (Iterator<ImageView> iterator = asteroids.iterator(); iterator.hasNext();) {
            ImageView a = iterator.next();
            a.setY(a.getY() + 2);

            if (a.getY() > SCREEN_HEIGHT) {
                iterator.remove();
                scenario.getChildren().remove(a);
            }
        }
        dropAsteroid++;
    }

    private void checkCollision() {
        List<ImageView> addLater = new ArrayList<>();
        for (Iterator<ImageView> iterator = asteroids.iterator(); iterator.hasNext();) {
            ImageView a = iterator.next();
            for (Rectangle b : bullets) {
                if (b.isVisible()) {
                    if (a.contains(b.getX(), b.getY())) {
                        b.setVisible(false);
                        scenario.getChildren().remove(b);
                        iterator.remove();
                        scenario.getChildren().remove(a);

                        if (a.getImage().getWidth() > 60) {
                            ImageView a1 = new ImageView(img1);
                            a1.setX(a.getX() - 20);
                            a1.setY(a.getY());
                            ImageView a2 = new ImageView(img1);
                            a2.setX(a.getX() + 20);
                            a2.setY(a.getY());

                            addLater.add(a1);
                            addLater.add(a2);
                        }
                    }
                }
            }
            if (a.contains(ship.getX(), ship.getY()) || a.contains(ship.getX() + ship.getImage().getWidth(), ship.getY())) {
                gameLoop.stop();
            }
        }

        asteroids.addAll(addLater);
        scenario.getChildren().addAll(0, addLater);
    }

}
