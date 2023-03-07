package com.orsonhannath.antcolonysimulation;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.Vector;

public class Ant {

    private AntWorld antWorld;

    private AntColony antsColony;
    private Color colonyColor;
    private int antID;

    //Main Stats
    private float maxSpeed;
    private float maxRange;
    private int maxStrength;
    private float maxPickupRange;
    private float phermStrength;

    //Wander/Movement Stats
    private float steerStrength;
    private float wanderStrength;

    private Vector2 position;
    private Vector2 desiredDirection;
    private Vector2 velocity;
    private double rotation;

    private Vector2 prevPosition;

    //Food Stats
    private boolean _hasFoodTarget;
    private Vector2 foodTargetPos;

    private boolean hasFood;
    private int hasFoodAmount;

    private boolean inDanger;

    //Sensors
    private float viewDistance = 1;
    private int showSensorSize = 1;
    private Vector2 fLeftPosition = new Vector2(0,0);
    private Vector2 fPosition = new Vector2(0,0);
    private Vector2 fRightPosition = new Vector2(0,0);


    public Ant(AntWorld _antWorld, AntColony _antsColony, Color _colonyColor, float _xPos, float _yPos, float _maxSpeed, float _maxRange, int _maxStrength, float _maxPickupRange, float _steerStrength, float _wanderStrength, int _id, float _phermStrength){

        this.antWorld = _antWorld;

        //Set from instantiation
        this.antsColony = _antsColony;
        this.colonyColor = _colonyColor;
        this.antID = _id;
        this.maxSpeed = _maxSpeed;
        this.maxRange = _maxRange;
        this.maxStrength = _maxStrength;
        this.maxPickupRange = _maxPickupRange;
        this.steerStrength = _steerStrength;
        this.wanderStrength = _wanderStrength;
        this.phermStrength = _phermStrength;

        //Set from here (Not really that necessary)
        this.position = new Vector2(_xPos, _yPos);
        this.velocity = new Vector2(0, 0);
        this._hasFoodTarget = false;
        this.foodTargetPos = new Vector2(0, 0);
        this.hasFood = false;
        this.hasFoodAmount = 0;

        this.desiredDirection = new Vector2(0,0);
    }

    public void MovementUpdate(){

        // *** Make the ant wonder around the map ***

        // Determine a direction
        this.desiredDirection = desiredDirection.Plus(RandomInsideCircle().Multiply(wanderStrength)).Normalized();

        // Setup Steering Direction
        Vector2 desiredVelocity = this.desiredDirection.Multiply(this.maxSpeed);
        Vector2 desiredSteeringForce = desiredVelocity.Minus(this.velocity).Multiply(this.steerStrength);
        Vector2 acceleration = desiredSteeringForce.ClampMagnitude(steerStrength).Divide(1);

        velocity = velocity.Plus(acceleration.Multiply(0.016f)).ClampMagnitude(maxSpeed);
        prevPosition = position; //Set the previous position (position before the ant moves)
        position = position.Plus(velocity.Multiply(0.016f));

        double angle = Math.toDegrees(Math.atan2(velocity.getYPos(), velocity.getXPos()));
        this.rotation = angle;

        //Check if the ant is colliding with something
        DetermineMovement();

        //Update Pheromone Trail
        PlacePheromones(Math.round(position.getXPos()), Math.round(position.getYPos()));

    }

    public void GraphicsUpdate(GraphicsContext gfx, boolean showSensors){

        gfx.setFill(this.colonyColor);
        gfx.fillRect(this.position.getXPos(), this.position.getYPos(), 2, 2);

        //Display the ants sensors
        if(showSensors){

            if(fLeftPosition != null && fPosition != null && fRightPosition != null){

                gfx.setFill(Color.RED);
                gfx.fillRect(this.fLeftPosition.getXPos(), this.fLeftPosition.getYPos(), showSensorSize, showSensorSize);
                //System.out.println("Red: " + "x: " + Math.round(this.fLeftPosition.getXPos()) + ", y: " + Math.round(this.fLeftPosition.getYPos()));

                gfx.setFill(Color.GREEN);
                gfx.fillRect(this.fPosition.getXPos(), this.fPosition.getYPos(), showSensorSize, showSensorSize);
                //System.out.println("Green: " + "x: " + Math.round(this.fPosition.getXPos()) + ", y: " + Math.round(this.fPosition.getYPos()));

                gfx.setFill(Color.BLUE);
                gfx.fillRect(this.fRightPosition.getXPos(), this.fRightPosition.getYPos(), showSensorSize, showSensorSize);
                //System.out.println("Blue: " + "x: " + Math.round(this.fRightPosition.getXPos()) + ", y: " + Math.round(this.fRightPosition.getYPos()));

            }
        }
    }

    private void DetermineMovement(){

        //Check Front Left
        double fLeftAngle = Math.toRadians(this.rotation - 45f);
        this.fLeftPosition = new Vector2((float)(position.getXPos() + Math.cos(fLeftAngle) * this.viewDistance), (float)(position.getYPos() + Math.sin(fLeftAngle) * this.viewDistance));
        WorldObjectTypes fLeftPixel = antWorld.getPixelAtPoint(Math.round(fLeftPosition.getXPos()), Math.round(fLeftPosition.getYPos()));

        //Check Front Middle
        double frontAngle = Math.toRadians(this.rotation);
        this.fPosition = new Vector2((float)(position.getXPos() + Math.cos(frontAngle) * this.viewDistance), (float)(position.getYPos() + Math.sin(frontAngle) * this.viewDistance));
        WorldObjectTypes frontPixel = antWorld.getPixelAtPoint(Math.round(fPosition.getXPos()), Math.round(fPosition.getYPos()));

        //Check Front Right
        double fRightAngle = Math.toRadians(this.rotation + 45f);
        this.fRightPosition = new Vector2((float)(position.getXPos() + Math.cos(fRightAngle) * this.viewDistance), (float)(position.getYPos() + Math.sin(fRightAngle) * this.viewDistance));
        WorldObjectTypes fRightPixel = antWorld.getPixelAtPoint(Math.round(fRightPosition.getXPos()), Math.round(fRightPosition.getYPos()));

        //Check Under Ant
        WorldObjectTypes underPixel = antWorld.getPixelAtPoint(Math.round(position.getXPos()), Math.round(position.getYPos()));
        PixelEventHandling(underPixel);

        //Maybe useful ---
        //fLeftPixel != WorldObjectTypes.Obstruction && fLeftPixel != WorldObjectTypes.OutOfBounds && fLeftPixel != WorldObjectTypes.Danger && fRightPixel != WorldObjectTypes.Obstruction && fRightPixel != WorldObjectTypes.OutOfBounds && fRightPixel != WorldObjectTypes.Danger && frontPixel != WorldObjectTypes.Obstruction && frontPixel != WorldObjectTypes.OutOfBounds && frontPixel != WorldObjectTypes.Danger && frontPixel != WorldObjectTypes.Food
        //Maybe useful ---

        // *** Determine what the ant should try to follow ***
        //This checks to see if any of the sensors are sensing either Obstruction, OutOfBounds or Danger
        if(fLeftPixel != WorldObjectTypes.Background && frontPixel != WorldObjectTypes.Background && fRightPixel != WorldObjectTypes.Background){

            //Check what is being sensed

        }else{

            //Follow Pheromones
            FollowPheromones();
        }

    }

    private void FollowPheromones(){

        Color fLeftPixelColor = antWorld.getPheromoneAt(Math.round(fLeftPosition.getXPos()), Math.round(fLeftPosition.getYPos()));
        Color frontPixelColor = antWorld.getPheromoneAt(Math.round(fPosition.getXPos()), Math.round(fPosition.getYPos()));
        Color fRightPixelColor = antWorld.getPheromoneAt(Math.round(fRightPosition.getXPos()), Math.round(fRightPosition.getYPos()));

        double fl = 0;
        double f = 0;
        double fr = 0;

        // Search for food follow blue if possible turn around if at home
        // Locate food and follow a green pheromone trail home while dropping a blue pheromone trail

        if(this.hasFood){

            //Follow Home (Blue)
            fl = fLeftPixelColor.getBlue() * 0.8f;
            f = frontPixelColor.getBlue() * 0.8f;
            fr = fRightPixelColor.getBlue() * 0.8f;

            fl += fLeftPixelColor.getGreen() * 0.2f;
            f += frontPixelColor.getGreen() * 0.2f;
            fr += fRightPixelColor.getGreen() * 0.2f;

        }else {

            // Maybe dont all follow green maybe green should only be placed when an ant is on its way back to base with food

            //Follow Food trail if you can find it (Blue)
            fl = fLeftPixelColor.getBlue();
            f = frontPixelColor.getBlue();
            fr = fRightPixelColor.getBlue();
        }

        //Determine which way to go
        if(f > Math.max(fl, fr)){

            //Go forward
            desiredDirection = desiredDirection;

        }else if(fl > fr){

            //Go left
            this.desiredDirection = desiredDirection.Plus(fLeftPosition.Minus(desiredDirection)).Normalized();

        }else if(fr > fl){

            //Go right
            this.desiredDirection = desiredDirection.Plus(fRightPosition.Minus(desiredDirection)).Normalized();

        }
    }

    private void PixelEventHandling(WorldObjectTypes pixelObject){

        if(pixelObject == WorldObjectTypes.Obstruction) {
            HandleObstruction();
        }else if(pixelObject == WorldObjectTypes.Food){
            HandleFood();
        }else if(pixelObject == WorldObjectTypes.Danger){
            HandleDanger();
        }else if(pixelObject == WorldObjectTypes.Colony1Location){
            HandleColony(1);
        }else if(pixelObject == WorldObjectTypes.Colony2Location){
            HandleColony(2);
        }else if(pixelObject == WorldObjectTypes.Colony3Location){
            HandleColony(3);
        }else if(pixelObject == WorldObjectTypes.Colony4Location){
            HandleColony(4);
        }else if(pixelObject == WorldObjectTypes.Background){
            HandleBackground();
        }
    }

    private void HandleBackground(){

        if(inDanger){

            inDanger = false;
        }
    }

    private void HandleObstruction(){


        //Ant needs to adjust its direction and cannot move until they're no longer in an obstruction
        position = prevPosition;
    }

    private void HandleFood(){

        if(!hasFood){

            hasFoodAmount = maxStrength;
            hasFood = true;

            // Flip the ant's direction 180 degrees
            this.velocity = new Vector2(0, 0);
            this.desiredDirection = this.desiredDirection.Multiply(-1);
        }
    }

    private void HandleDanger(){

        if(!inDanger){

            inDanger = true;
            if(hasFood){

                //If have food when going into danger drop it
                hasFood = false;
                hasFoodAmount = 0;
            }
        }
    }

    private void HandleColony(int _colonyNumber){

    }

    private void HandleSteering(){

    }

    private void PlacePheromones(int x, int y){

        // Blue has food
        // Green doesn't have food
        // Red is danger

        if(x < antWorld.worldSizeX && x >= 0 && y < antWorld.worldSizeY && y >= 0){

            antWorld.SetPheromonePos(x, y, this.phermStrength, inDanger, hasFood);
        }
    }

    //Not exactly performant but should work
    private Vector2 RandomInsideCircle(){

        Random rand = new Random();
        Vector2 circlePoints = new Vector2(0,0);

        //Find a random point within a unit circle
        double magnitude = 2;
        while (magnitude > 1){

            //Create two random points in a 2x2 square (cartesian coordinate from -1 to 1)
            float xPoint = rand.nextFloat(2) - 1;
            float yPoint = rand.nextFloat(2) - 1;

            //If their magnitude is less than or equal to 1 then they're within a circle
            magnitude = Math.sqrt((xPoint*xPoint) + (yPoint*yPoint));

            if(magnitude <= 1){

                //Point is within a circle
                circlePoints.setPos(xPoint, yPoint);
                return circlePoints;
            }
        }

        return circlePoints;
    }
}
