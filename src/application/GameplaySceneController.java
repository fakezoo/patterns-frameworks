package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import javafx.beans.property.SimpleLongProperty;
import static java.nio.file.StandardOpenOption.*;


public class GameplaySceneController {
    private PatternHeroGame game;
    private int lives;
    private String midiFileName;
    private int BPM;

    @FXML
    private Text P1noteFeedback;

    @FXML
    private Text P2noteFeedback;

    @FXML
    private Text songLabel;
    @FXML
    private Text sharedLivesDisplay;

    @FXML
    private Text P1PointsDisplay;

    @FXML
    private Text P2PointsDisplay;

    static SimpleLongProperty observableTick = new SimpleLongProperty();

    /**
     * Displays information needed for start of game. Like songname + title.
     * This information comes from the StartScene
     */
    public void setStartInformation(String songName, String midiFileName, int BPM, int initialLives){
        this.BPM = BPM;
        lives = initialLives;
        songLabel.setText(songName);
        this.midiFileName = midiFileName;
        sharedLivesDisplay.setText(String.valueOf(initialLives));
    }

    /**
     *Sets text in scene according to how well user hit note, updates live count. Gets called on key press or mouse click.
     */
    public void giveFeedback(String inputKey) {

        long tick = game.ph.sequencerTickPosition;

        int player;
        String key;
        int notePointsAmount;
        String noteFeedbackText;

        if ("HJKLhjkl".contains(inputKey)) {
            player = 2;
            key = Player.mapPlayerKey(inputKey);
        } else {
            player = 1;
            key = inputKey;
        }

        String note = game.ph.nextNote;
        if (key.equalsIgnoreCase(note)) {
            long lag = game.ph.nextTick - tick;
            if (Math.abs(lag) > 200) {
                notePointsAmount = 1;
                noteFeedbackText = "Bad";
            } else if (Math.abs(lag) > 100) {
                notePointsAmount = 10;
                noteFeedbackText = "Okay";
            } else {
                notePointsAmount = 100;
                noteFeedbackText = "PERFECT!";
            }
            System.out.printf("Player %d score: %d (%s) %n", player, notePointsAmount, noteFeedbackText);
        } else {
            lives--;
            notePointsAmount = 0;
            noteFeedbackText = "FALSE!";
        }

        if (player == 1) {
            P1noteFeedback.setText(noteFeedbackText);
            P1PointsDisplay.setText(Integer.toString(Integer.parseInt(P1PointsDisplay.getText()) + notePointsAmount));
        }
        if (player == 2) {
            P2noteFeedback.setText(noteFeedbackText);
            P2PointsDisplay.setText(Integer.toString(Integer.parseInt(P2PointsDisplay.getText()) + notePointsAmount));
        }

        sharedLivesDisplay.setText(String.valueOf(lives));

        if (lives <= 0) {
            stopGame();
        }
    }

    public void updateUI(long tick) {
        System.out.println("tick " + tick); /* update UI here */ 
    }

    public void stopGame() {
        game.ph.stopSequencer(); // SwitchToGameOverScene
    }

    public void startGame() {

        //If user did not make speed selection, speed is 90 (=regular)
        if (BPM == 0) {
            BPM = 90;
        }

        //If user did not make difficulty selection, lives is 50 (=regular)
        if (lives == 0) {
            lives = 50;
        }

        //Instantiate and declare a new game and run it in its own thread
        game = new PatternHeroGame(BPM, lives, midiFileName, this);
        Thread gameLogicThread = new Thread(game);
        gameLogicThread.start();
    }

    public void logHistory() throws IOException {
        Files.write(Paths.get("history.csv"),
            "%s;%s;%s;%s".formatted(
                LocalDateTime.now(),
                P1PointsDisplay.getText(),
                P2PointsDisplay.getText(),
                System.lineSeparator()
            ).getBytes(), APPEND,CREATE
        );
    }

    /**
     * Function to stop playback and go back to main menu on "quit" button press
     */
    public void switchToStartScene(ActionEvent event) throws IOException {
        //stops sequencer
        game.ph.stopSequencer();


        //Load Gameplay Scene
        Parent root = FXMLLoader.load(getClass().getResource("StartScene.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        //set CSS
        String css = this.getClass().getResource("/themes/classic-theme.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.show();
    }


    /**
     * Handles click on button, alternative to keyboard input
     * @param e
     */
    public void handleButton(ActionEvent e) {
        giveFeedback(((Button)e.getSource()).getText());
    }

    /**
     * Handles keyboard input
     * @param event The event of the key that was pressed
     */
    public void handleKeyboard(KeyEvent event) {
        String key = event.getText();
        if ("asdflkjhASDFLKJH".contains(key))
            giveFeedback(key.toUpperCase());
    }

    @FXML
    private Rectangle aNoteBlock;

    @FXML
    private Rectangle sNoteBlock;

    @FXML
    private Rectangle dNoteBlock;

    @FXML
    private Rectangle fNoteBlock;



    /**
     * Marks what note to press next on screen
     * @param nextNote next note user has to press
     */
    public void markNoteBlock(String nextNote){
        String defaultStyle = "-fx-fill: black;";
        String upNextStyle = "-fx-fill:white;";

        //reset style for all blocks
        aNoteBlock.setStyle(defaultStyle);
        sNoteBlock.setStyle(defaultStyle);
        dNoteBlock.setStyle(defaultStyle);
        fNoteBlock.setStyle(defaultStyle);

        //set style for upcoming block
        switch (nextNote){
            case "A":
                aNoteBlock.setStyle(upNextStyle);
                break;
           case "S":
               sNoteBlock.setStyle(upNextStyle);
               break;
           case "D":
               dNoteBlock.setStyle(upNextStyle);
               break;
           case "F":
               fNoteBlock.setStyle(upNextStyle);
               break;
        }



    }


}
