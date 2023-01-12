package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Start-Klasse.
 * @author julia
 *
 */
public class Main extends Application {
	

	static AtomicReference<String> lastInput = new AtomicReference<>("");

	static UserInputHandler userInputHandler = new UserInputHandler(lastInput);

	/**
	 * start() Method is inherited from JavaFX Application class
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			//JAVAFX STUFF
			//Create JavaFX Scene
			FXMLLoader loader = new FXMLLoader(getClass().getResource("StartScene.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);

			//set CSS
			String css = this.getClass().getResource(Settings.getTheme()).toExternalForm();
			scene.getStylesheets().add(css);

			//Set Title
			primaryStage.setTitle("PatternHero!");

			//Set Icon
			Image icon = new Image("icon.png");
			primaryStage.getIcons().add(icon);
			
			primaryStage.setScene(scene);
			primaryStage.show();
			
			//USER INPUT
			//On key input on the scene,
			scene.setOnKeyPressed(userInputHandler);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		//GAME LOGIC THREAD
		PatternHeroGame game = new PatternHeroGame(userInputHandler,lastInput);
		Thread gameLogicThread = new Thread(game);
		gameLogicThread.start();

		//GRAPHICS THREAD (JavaFX creates a Thread automatically)
		launch(args);
		
	}
}
