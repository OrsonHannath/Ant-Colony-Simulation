module com.orsonhannath.antcolonysimulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.orsonhannath.antcolonysimulation to javafx.fxml;
    exports com.orsonhannath.antcolonysimulation;
}