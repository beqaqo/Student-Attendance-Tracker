package com.undefinedvars.attendance;

import com.undefinedvars.attendance.controller.StudentController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
    Application entry point. Builds the object graph via AppContext,
    loads the FXML layout, and shows the window. The controllerFactory
    is what lets our constructor-injected StudentController work with
    JavaFX, which would otherwise demand a no-arg constructor.
*/
public final class AttendanceApp extends Application {

    /*
        JavaFX calls this automatically after launch(). The Stage is the
        top-level window the framework hands us.
    */
    @Override
    public void start(Stage stage) throws Exception {
        // Build the whole backend graph: repository, generator, service.
        AppContext context = AppContext.bootstrap();

        // Point the loader at the FXML file on the classpath.
        // The leading "/" means "from the resources root".
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main_view.fxml"));

        /*
            The controller factory: when the loader needs the controller
            named in the FXML, it calls this instead of new-ing a no-arg
            one. We construct StudentController with its dependency.
        */
        loader.setControllerFactory(type ->
                new StudentController(context.getStudentService()));

        // load() reads the FXML and builds the UI tree; Parent is its root.
        Parent root = loader.load();

        // A Scene wraps the UI tree; the numbers are window size in pixels.
        stage.setTitle("Student Attendance Tracker");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    /*
        Java's entry point. launch() boots JavaFX, which then calls start().
    */
    public static void main(String[] args) {
        launch(args);
    }
}