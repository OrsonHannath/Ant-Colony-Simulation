package com.orsonhannath.antcolonysimulation;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class AntWorld{

    private AntColonyController antColonyController;

    private AntColony antColony1;
    private AntColony antColony2;
    private AntColony antColony3;
    private AntColony antColony4;

    public int worldSizeX;
    public int worldSizeY;

    private Color[][] worldMap; // World Map Produced
    private Color[][] pheromoneMap; // Pheromone Map Produced
    private Color[] colorDefinitions; // The Corresponding Definitions to each color

    private float pheromoneOpacity = 0.35f;
    private float pheromoneBlurFactor = 0.01f;

    private float colonyRedThresh = 0.01f;
    private float colonyGreenThresh = 0.01f;
    private float colonyBlueThresh = 0.01f;

    public boolean canUpdateAnts = true;
    public Thread updateThread;

    public AntWorld(int worldXSize, int worldYSize, Canvas canvas, Color[] colorDefs, int _scaleFactor, AntColonyController _antColonyController){

        this.worldMap = canvasToArr(worldXSize, worldYSize, canvas);
        this.pheromoneMap = new Color[worldXSize][worldYSize];
        setPheromoneMap(worldXSize, worldYSize);
        this.colorDefinitions = colorDefs;

        this.worldSizeX = worldXSize;
        this.worldSizeY = worldYSize;

        this.antColonyController = _antColonyController;
    }

    public void PlayLogic(Canvas canvas){

        //Do the update on a separate thread
        updateThread = new Thread(() -> {

            while(antColonyController.getPlaying()) {

                //System.out.println("a"); //Couldn't replicate pause glitch with this here
                if (!antColonyController.getPlaying()) {

                } else {

                    //Application is playing

                    if(canUpdateAnts) {

                        Platform.runLater(() -> {

                            UpdateAntGraphics(canvas, antColonyController.isShowPheromones());
                            DecrementPheromoneTrails();
                        });

                        UpdateAnts();

                        canUpdateAnts = false;
                    }
                }
            }
        });
        updateThread.start();
    }

    public void setWorldMap(int worldXSize, int worldYSize, Canvas canvas, Color[] colorDefs, int _scaleFactor){

        this.worldMap = canvasToArr(worldXSize, worldYSize, canvas);
        this.colorDefinitions = colorDefs;
        //System.out.println("r: " + (this.worldMap[0][0].getRed() * 255) + ", g: " + (this.worldMap[0][0].getGreen() * 255) + ", b: " + (this.worldMap[0][0].getBlue() * 255));

        this.worldSizeX = worldXSize;
        this.worldSizeY = worldYSize;

        //Create the colonies
        CreateColonies(_scaleFactor);

        //Update the graphics
        javafx.scene.image.Image worldMapImage;
        BufferedImage bufferedImage = new BufferedImage(worldMap.length, worldMap[0].length, BufferedImage.TYPE_INT_RGB);

        // Set each pixel of the BufferedImage to the color from the Color[][].
        for (int x = 0; x < worldMap.length; x++) {
            for (int y = 0; y < worldMap[x].length; y++) {

                java.awt.Color awtColor = new java.awt.Color((float) worldMap[x][y].getRed(),
                        (float) worldMap[x][y].getGreen(),
                        (float) worldMap[x][y].getBlue(),
                        (float) worldMap[x][y].getOpacity());

                bufferedImage.setRGB(x, y, awtColor.getRGB());
            }
        }

        worldMapImage = convertToFxImage(bufferedImage);
        canvas.getGraphicsContext2D().drawImage(worldMapImage, 0, 0, worldXSize, worldYSize);

        //Start Playing
        PlayLogic(canvas);
    }

    public void setPheromoneMap(int worldXSize, int worldYSize){

        this.pheromoneMap = new Color[worldXSize][worldYSize];
        for(int i = 0; i < worldXSize; i++){
            for(int j = 0; j < worldYSize; j++){
                this.pheromoneMap[i][j] = new Color(0,0,0,1);
            }
        }
    }

    public void SetPheromonePos(int x, int y, float pherStrength){

        Color phermColor = new Color(0,0,0,1);

        if(pheromoneMap[x][y] != null) {
            if (pheromoneMap[x][y].getRed() + pherStrength <= 1) {
                phermColor = new Color(pheromoneMap[x][y].getRed() + pherStrength, phermColor.getGreen(), phermColor.getBlue(), 1);
            } else {
                phermColor = new Color(1, phermColor.getGreen(), phermColor.getBlue(), 1);
            }

            if (pheromoneMap[x][y].getGreen() + pherStrength <= 1) {
                phermColor = new Color(phermColor.getRed(), pheromoneMap[x][y].getGreen() + pherStrength, phermColor.getBlue(), 1);
            } else {
                phermColor = new Color(phermColor.getRed(), 1, phermColor.getBlue(), 1);
            }

            if (pheromoneMap[x][y].getBlue() + pherStrength <= 1) {
                phermColor = new Color(phermColor.getRed(), phermColor.getGreen(), pheromoneMap[x][y].getBlue() + pherStrength, 1);
            } else {
                phermColor = new Color(phermColor.getRed(), phermColor.getGreen(), 1, 1);
            }

            pheromoneMap[x][y] = phermColor;
        }
    }

    private void UpdateAnts(){

        for(Ant ant : antColony1.GetAnts()){
            ant.MovementUpdate();
        }

        for(Ant ant : antColony2.GetAnts()){
            ant.MovementUpdate();
        }

        for(Ant ant : antColony3.GetAnts()){
            ant.MovementUpdate();
        }

        for(Ant ant : antColony4.GetAnts()){
            ant.MovementUpdate();
        }
    }

    private void UpdateAntGraphics(Canvas canvas, boolean showPherms){

        //Set the background to correct image
        javafx.scene.image.Image worldMapImage;
        BufferedImage bufferedImage = new BufferedImage(worldMap.length, worldMap[0].length, BufferedImage.TYPE_INT_RGB);

        // Set each pixel of the BufferedImage to the color from the Color[][].
        for (int x = 0; x < worldMap.length; x++) {
            for (int y = 0; y < worldMap[x].length; y++) {

                java.awt.Color awtColor = new java.awt.Color((float) worldMap[x][y].getRed(),
                        (float) worldMap[x][y].getGreen(),
                        (float) worldMap[x][y].getBlue(),
                        (float) worldMap[x][y].getOpacity());

                bufferedImage.setRGB(x, y, awtColor.getRGB());
            }
        }

        worldMapImage = convertToFxImage(bufferedImage);
        canvas.getGraphicsContext2D().drawImage(worldMapImage, 0, 0, worldSizeX, worldSizeY);

        //Basic graphics update
        for(Ant ant : antColony1.GetAnts()){
            ant.GraphicsUpdate(canvas.getGraphicsContext2D());
        }

        for(Ant ant : antColony2.GetAnts()){
            ant.GraphicsUpdate(canvas.getGraphicsContext2D());
        }

        for(Ant ant : antColony3.GetAnts()){
            ant.GraphicsUpdate(canvas.getGraphicsContext2D());
        }

        for(Ant ant : antColony4.GetAnts()){
            ant.GraphicsUpdate(canvas.getGraphicsContext2D());
        }

        //Check if the pheromones should be displayed
        if(showPherms){

            for(int x = 0; x < worldSizeX; x++){
                for(int y = 0; y < worldSizeY; y++){

                    Color pheromoneMapAtPos = pheromoneMap[x][y];

                    if(!Objects.equals(pheromoneMapAtPos, new Color(0, 0, 0, 1))) {
                        Color pheromoneCol = new Color(pheromoneMapAtPos.getRed(), pheromoneMapAtPos.getGreen(), pheromoneMapAtPos.getBlue(), pheromoneOpacity);
                        canvas.getGraphicsContext2D().setFill(pheromoneCol);
                        canvas.getGraphicsContext2D().fillRect(x, y, 1, 1);
                    }
                }
            }
        }

        canUpdateAnts = true;
    }

    public void DecrementPheromoneTrails(){

        //Not working perfectly

        for(int i = 0; i < worldSizeX; i++){
            for(int j = 0; j < worldSizeY; j++){

                Color currPhermCol = pheromoneMap[i][j];
                this.pheromoneMap[i][j] = new Color(currPhermCol.getRed() * (1-pheromoneBlurFactor), currPhermCol.getGreen() * (1-pheromoneBlurFactor), currPhermCol.getBlue() * (1-pheromoneBlurFactor), 1);
            }
        }
    }

    private Color[][] canvasToArr(int worldXSize, int worldYSize, Canvas canvas){

        Color[][] tempColArr = new Color[worldXSize][worldYSize];

        WritableImage writableImage = new WritableImage(worldXSize, worldYSize);
        canvas.snapshot(null, writableImage);
        PixelReader pixelReader = writableImage.getPixelReader();

        for(int x = 0; x < worldXSize; x++){
            for(int y = 0; y < worldYSize; y++){

                tempColArr[x][y] = pixelReader.getColor(x,y);
            }
        }

        return tempColArr;
    }

    private void CheckIfAnt(int x, int y, boolean remove){

        Color pixelColor = worldMap[x][y];
        float colorThreshold = 0.05f;
        //System.out.println(colorDefinitions[3].getRed() + ",   " + colorDefinitions[3].getGreen() + ",   " + colorDefinitions[3].getBlue());
        //System.out.println(pixelColor.getRed() + ",   " + pixelColor.getGreen() + ",   " + pixelColor.getBlue());

        if(ColorMatchThreshold(pixelColor, colorDefinitions[3], colorThreshold)){


            // Colony 1 - Determine if code should remove or just add ant
            if(!remove){
                antColony1.AddAnt(x, y, this);
                //System.out.println("--- Spawned Ant to Colony 1 At - x: " + x + ", y: " + y);
            }else{
                SetSurround(x,y,colorDefinitions[11]);
            }
        }else if(ColorMatchThreshold(pixelColor, colorDefinitions[4], colorThreshold)){

            // Colony 2 - Determine if code should remove or just add ant
            if(!remove){
                antColony2.AddAnt(x, y, this);
                //System.out.println("--- Spawned Ant to Colony 2 At - x: " + x + ", y: " + y);
            }else{
                SetSurround(x,y,colorDefinitions[11]);
            }
        }else if(ColorMatchThreshold(pixelColor, colorDefinitions[5], colorThreshold)){

            //Colony 3 - Determine if code should remove or just add ant
            if(!remove){
                antColony3.AddAnt(x, y, this);
                //System.out.println("--- Spawned Ant to Colony 3 At - x: " + x + ", y: " + y);
            }else{
                SetSurround(x,y,colorDefinitions[11]);
            }
        }else if(ColorMatchThreshold(pixelColor, colorDefinitions[6], colorThreshold)){

            //Colony 4 - Determine if code should remove or just add ant
            if(!remove){
                antColony4.AddAnt(x, y, this);
                //System.out.println("--- Spawned Ant to Colony 4 At - x: " + x + ", y: " + y);
            }else{
                SetSurround(x,y,colorDefinitions[11]);
            }
        }
    }

    private void CreateColonies(int _scaleFactor){

        antColony1 = new AntColony(colorDefinitions[3], "Colony 1");
        antColony2 = new AntColony(colorDefinitions[4], "Colony 2");
        antColony3 = new AntColony(colorDefinitions[5], "Colony 3");
        antColony4 = new AntColony(colorDefinitions[6], "Colony 4");

        int scaledWorldWidth = worldSizeX/_scaleFactor;
        int scaledWorldHeight = worldSizeY/_scaleFactor;

        Color[][] averagedColors = new Color[scaledWorldWidth][scaledWorldHeight];

        //loop through each of the elements in the averaged colours
        for(int i = 0; i < scaledWorldWidth; i++){
            for(int j = 0; j < scaledWorldHeight; j++){

                float avgR = 0;
                float avgG = 0;
                float avgB = 0;
                Color avgColor;

                //Loop through the true colours of the world and average them out
                for(int x = 0; x < _scaleFactor; x++){
                    for(int y = 0; y < _scaleFactor; y++){

                        avgR += worldMap[(i*_scaleFactor)+x][(j*_scaleFactor)+y].getRed();
                        avgG += worldMap[(i*_scaleFactor)+x][(j*_scaleFactor)+y].getGreen();
                        avgB += worldMap[(i*_scaleFactor)+x][(j*_scaleFactor)+y].getBlue();

                        //Check if any of these pixels should be ants (And only spawn them)
                        CheckIfAnt((i*_scaleFactor)+x, (j*_scaleFactor)+y, false);

                    }
                }

                avgR /= (_scaleFactor * _scaleFactor);
                avgG /= (_scaleFactor * _scaleFactor);
                avgB /= (_scaleFactor * _scaleFactor);

                avgColor = new Color(avgR, avgG, avgB, 1);
                averagedColors[i][j] = avgColor;

                //Check if a colony exists (4 is based on number of colonies)
                for(int z = 0; z < 4; z++) {

                    if (ColorMatchThreshold(averagedColors[i][j], colorDefinitions[7+z], colonyRedThresh, colonyGreenThresh, colonyBlueThresh)) {

                        int middleLocationX = (i*_scaleFactor) + (_scaleFactor/2);
                        int middleLocationY = (j*_scaleFactor) + (_scaleFactor/2);
                        //System.out.println("Found Colony At: x= " + middleLocationX + ", y= " + middleLocationY);

                        //Spawn the colony
                        if(z == 0){

                            //Colony 1
                            antColony1.AddLocation(middleLocationX, middleLocationY);
                            System.out.println("Added Ant Colony 1 At Location - x: " + middleLocationX + ", y: " + middleLocationY);

                        }else if(z == 1){

                            //Colony 2
                            antColony2.AddLocation(middleLocationX, middleLocationY);
                            System.out.println("Added Ant Colony 2 At Location - x: " + middleLocationX + ", y: " + middleLocationY);

                        }else if(z == 2){

                            //Colony 3
                            antColony3.AddLocation(middleLocationX, middleLocationY);
                            System.out.println("Added Ant Colony 3 At Location - x: " + middleLocationX + ", y: " + middleLocationY);

                        }else if(z == 3){

                            //Colony 4
                            antColony4.AddLocation(middleLocationX, middleLocationY);
                            System.out.println("Added Ant Colony 4 At Location - x: " + middleLocationX + ", y: " + middleLocationY);

                        }
                    }
                }
            }
        }

        //Loop through the world map and remove any traces of ant spawning
        Vector<Vector2> antLocations = new Vector<>();
        float antsThreshold = 0.1f;

        for(int x = 0; x < worldSizeX; x++){
            for(int y = 0; y < worldSizeY; y++){
                for(int z = 0; z < 4; z++) {

                    if (ColorMatchThreshold(worldMap[x][y], colorDefinitions[3+z], antsThreshold)) {

                        antLocations.add(new Vector2(x, y));
                    }
                }
            }
        }

        for (Vector2 loc : antLocations) {

            CheckIfAnt((int)loc.getXPos(), (int)loc.getYPos(), true);
        }
    }

    private boolean ColorMatchThreshold(Color col, Color match, float thresholdR, float thresholdG, float thresholdB){

        if (col.getRed() >= (match.getRed() - thresholdR) && col.getRed() <= (match.getRed() + thresholdR)) {
            if (col.getGreen() >= (match.getGreen() - thresholdG) && col.getGreen() <= (match.getGreen() + thresholdG)) {
                if (col.getBlue() >= (match.getBlue() - thresholdB) && col.getBlue() <= (match.getBlue() + thresholdB)) {

                    return true;
                }
            }
        }

        return false;
    }

    private boolean ColorMatchThreshold(Color col, Color match, float threshold){

        if (col.getRed() >= (match.getRed() - threshold) && col.getRed() <= (match.getRed() + threshold)) {
            if (col.getGreen() >= (match.getGreen() - threshold) && col.getGreen() <= (match.getGreen() + threshold)) {
                if (col.getBlue() >= (match.getBlue() - threshold) && col.getBlue() <= (match.getBlue() + threshold)) {

                    return true;
                }
            }
        }

        return false;
    }

    private static javafx.scene.image.Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr).getImage();
    }

    private void SetSurround(int x, int y, Color col){

        for(int i = 0; i < 19; i++){
            for(int j = 0; j < 19; j++){

                int xPos = (x - 9) + i;
                int yPos = (y - 9) + j;

                //This is a piece that wants to be checked
                if(WorldCoordExists(xPos, yPos) && CheckPixelNotImportant(xPos, yPos)){

                    worldMap[xPos][yPos] = col;
                }
            }
        }
    }

    private boolean WorldCoordExists(int x, int y){

        if(x >= 0 && x < worldSizeX && y >= 0 && y < worldSizeY) {

            return true;
        }

        return false;
    }

    private boolean CheckPixelNotImportant(int x, int y){

        float threshold = 0.05f;

        //Matches Obstruction
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[0], threshold)){

            return false;
        }

        //Matches Food
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[1], threshold)){

            return false;
        }

        //Matches Danger
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[2], threshold)){

            return false;
        }

        //Matches Colony 1 Location
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[7], threshold)){

            return false;
        }

        //Matches Colony 2 Location
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[8], threshold)){

            return false;
        }

        //Matches Colony 3 Location
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[9], threshold)){

            return false;
        }

        //Matches Colony 4 Location
        if(ColorMatchThreshold(worldMap[x][y], colorDefinitions[10], threshold)){

            return false;
        }

        return true;
    }
}
