package com.orsonhannath.antcolonysimulation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class AntColonyController implements Initializable {

    //World Setup
    private int worldSizeX = 960; //If changed here will need to adjust inside the antsimulation-view.fxml aswell
    private int worldSizeY = 540;
    private int scaleFactor = 10;
    private AntWorld _antWorld;

    //JavaFX Stuff
    private Color bgColor = Color.web("#404040");

    private int currentBrush = -1;
    private boolean playing = false;
    private boolean started = false;
    private boolean showPheromones = false;

    private GraphicsContext brushTool;

    @FXML
    private Canvas canvas;

    @FXML
    private Button play_button;

    @FXML
    private Button pause_button;

    @FXML
    private Button brush_obstruction;

    @FXML
    private Button brush_food;

    @FXML
    private Button brush_danger;

    @FXML
    private Button brush_colony1;

    @FXML
    private Button col1_home_btn;

    @FXML
    private Button brush_colony2;

    @FXML
    private Button col2_home_btn;

    @FXML
    private Button brush_colony3;

    @FXML
    private Button col3_home_btn;

    @FXML
    private Button brush_colony4;

    @FXML
    private Button col4_home_btn;

    @FXML
    private TextField brush_size;

    @FXML
    private ColorPicker obstruction_color;

    @FXML
    private ColorPicker food_color;

    @FXML
    private ColorPicker danger_color;

    @FXML
    private ColorPicker colony1_color;

    @FXML
    private ColorPicker colony2_color;

    @FXML
    private ColorPicker colony3_color;

    @FXML
    private ColorPicker colony4_color;

    @FXML
    private Button eraser_button;

    @FXML
    private Button nobrush_button;

    @FXML
    private RadioButton pheromones_radio_button;

    @FXML
    public void setBrush_obstruction(ActionEvent e){
        currentBrush = 0;
    }

    @FXML
    public void setBrush_food(ActionEvent e){
        currentBrush = 1;
    }

    @FXML
    public void setBrush_danger(ActionEvent e){
        currentBrush = 2;
    }

    @FXML
    public void setBrush_colony1(ActionEvent e){
        currentBrush = 3;
    }

    @FXML
    public void setBrush_colony2(ActionEvent e){
        currentBrush = 4;
    }

    @FXML
    public void setBrush_colony3(ActionEvent e){
        currentBrush = 5;
    }

    @FXML
    public void setBrush_colony4(ActionEvent e){
        currentBrush = 6;
    }

    @FXML
    public void setBrush_eraser(ActionEvent e){
        currentBrush = 7;
    }

    @FXML
    public void setBrush_colony1_home(ActionEvent e){
        currentBrush = 8;
    }

    @FXML
    public void setBrush_colony2_home(ActionEvent e){
        currentBrush = 9;
    }

    @FXML
    public void setBrush_colony3_home(ActionEvent e){
        currentBrush = 10;
    }

    @FXML
    public void setBrush_colony4_home(ActionEvent e){
        currentBrush = 11;
    }

    @FXML
    public void setBrush_none(ActionEvent e){
        currentBrush = -1;
    }

    @FXML
    public void reset_action(ActionEvent e){

        // Set main params
        started = false;
        playing = false;

        //Reset the canvas
        brushTool.setFill(bgColor);
        brushTool.fillRect(0,0, worldSizeX,worldSizeY);

        //Set the antWorld
        _antWorld = new AntWorld(worldSizeX,worldSizeY, canvas, colorDefinitions(), scaleFactor, this);

        ShowEditingTools();
    }

    @FXML
    public void play_selected(ActionEvent e){

        if(!playing) {
            playing = true;
            currentBrush = -1;

            if (!started) {

                //Start the ants
                started = true;
                HideEditingTools();

                // Process the graphics into an array
                _antWorld.setWorldMap(worldSizeX, worldSizeY, canvas, colorDefinitions(), scaleFactor);
            } else {

                _antWorld.PlayLogic(canvas);
            }
        }
    }

    @FXML
    public void pause_selected(ActionEvent e){

        playing = false;
    }

    @FXML
    public void pheromones_radio_button_updated(ActionEvent e){

        showPheromones = pheromones_radio_button.isSelected();
        //System.out.println("Toggled");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Set the antWorld
        _antWorld = new AntWorld(worldSizeX,worldSizeY, canvas, colorDefinitions(), scaleFactor, this);

        brushTool = canvas.getGraphicsContext2D();

        //Set the background
        brushTool.setFill(bgColor);
        brushTool.fillRect(0,0, worldSizeX,worldSizeY);

        //Placing Locations In
        canvas.setOnMousePressed(e ->{

            //Check if the user is trying to draw
            if(currentBrush != -1 && !brush_size.getText().isEmpty() && !started){

                //Make sure not drawing the location
                if(currentBrush == 8 || currentBrush == 9 || currentBrush == 10 || currentBrush == 11){

                    //Should be drawing location
                    //double size = 20;

                    //double x = e.getX() - size / 2;
                    //double y = e.getY() - size / 2;

                    //Determine the closest scaleFactor grid position
                    double mouseX = e.getX();
                    double mouseY = e.getY();

                    float snappedXPos = scaleFactor*(Math.round(mouseX/scaleFactor));
                    float snappedYPos = scaleFactor*(Math.round(mouseY/scaleFactor));

                    snappedXPos -= (scaleFactor/2);
                    snappedYPos -= (scaleFactor/2);

                    brushTool.setFill(currentBrushColor());
                    brushTool.fillRect(snappedXPos, snappedYPos, scaleFactor*2, scaleFactor*2);
                }
            }
        });

        //Drawing Stuff In
        canvas.setOnMouseDragged(e -> {

            //Check if the user is trying to draw
            if(currentBrush != -1 && !brush_size.getText().isEmpty() && !started){


                //Make sure not drawing the location
                if(currentBrush == 8 || currentBrush == 9 || currentBrush == 10 || currentBrush == 11){

                    //Should not be drawing because location is selected

                }else{

                    //Should be drawing normal
                    double size = Double.parseDouble(brush_size.getText());

                    double x = e.getX() - size / 2;
                    double y = e.getY() - size / 2;

                    brushTool.setFill(currentBrushColor());
                    brushTool.fillRoundRect(x, y, size, size, size, size);
                }
            }
        });
    }

    public boolean isShowPheromones(){

        return showPheromones;
    }

    private Color currentBrushColor(){

        if (currentBrush == 0){

            return obstruction_color.getValue();
        }else if (currentBrush == 1){

            return food_color.getValue();
        }else if (currentBrush == 2){

            return danger_color.getValue();
        }else if (currentBrush == 3){

            return colony1_color.getValue();
        }else if (currentBrush == 4){

            return colony2_color.getValue();
        }else if (currentBrush == 5){

            return colony3_color.getValue();
        }else if (currentBrush == 6){

            return colony4_color.getValue();
        }else if (currentBrush == 7){

            return bgColor;
        }else if (currentBrush == 8){

            return colony1_color.getValue().invert();
        }else if (currentBrush == 9){

            return colony2_color.getValue().invert();
        }else if (currentBrush == 10){

            return colony3_color.getValue().invert();
        }else if (currentBrush == 11){

            return colony4_color.getValue().invert();
        }

        return null;
    }

    public boolean getPlaying(){

        return this.playing;
    }

    private Color[] colorDefinitions(){

        //Set the color array
        Color[] _colorDefinitions = new Color[12];
        _colorDefinitions[0] = obstruction_color.getValue();
        _colorDefinitions[1] = food_color.getValue();
        _colorDefinitions[2] = danger_color.getValue();
        _colorDefinitions[3] = colony1_color.getValue();
        _colorDefinitions[4] = colony2_color.getValue();
        _colorDefinitions[5] = colony3_color.getValue();
        _colorDefinitions[6] = colony4_color.getValue();
        _colorDefinitions[7] = colony1_color.getValue().invert();
        _colorDefinitions[8] = colony2_color.getValue().invert();
        _colorDefinitions[9] = colony3_color.getValue().invert();
        _colorDefinitions[10] = colony4_color.getValue().invert();
        _colorDefinitions[11] = bgColor;
        return _colorDefinitions;
    }

    private void HideEditingTools(){

        brush_obstruction.setVisible(false);
        brush_food.setVisible(false);
        brush_danger.setVisible(false);
        brush_colony1.setVisible(false);
        col1_home_btn.setVisible(false);
        brush_colony2.setVisible(false);
        col2_home_btn.setVisible(false);
        brush_colony3.setVisible(false);
        col3_home_btn.setVisible(false);
        brush_colony4.setVisible(false);
        col4_home_btn.setVisible(false);
        brush_size.setVisible(false);
        obstruction_color.setVisible(false);
        food_color.setVisible(false);
        danger_color.setVisible(false);
        colony1_color.setVisible(false);
        colony2_color.setVisible(false);
        colony3_color.setVisible(false);
        colony4_color.setVisible(false);
        eraser_button.setVisible(false);
        nobrush_button.setVisible(false);
    }

    private void ShowEditingTools(){

        brush_obstruction.setVisible(true);
        brush_food.setVisible(true);
        brush_danger.setVisible(true);
        brush_colony1.setVisible(true);
        col1_home_btn.setVisible(true);
        brush_colony2.setVisible(true);
        col2_home_btn.setVisible(true);
        brush_colony3.setVisible(true);
        col3_home_btn.setVisible(true);
        brush_colony4.setVisible(true);
        col4_home_btn.setVisible(true);
        brush_size.setVisible(true);
        obstruction_color.setVisible(true);
        food_color.setVisible(true);
        danger_color.setVisible(true);
        colony1_color.setVisible(true);
        colony2_color.setVisible(true);
        colony3_color.setVisible(true);
        colony4_color.setVisible(true);
        eraser_button.setVisible(true);
        nobrush_button.setVisible(true);
    }
}