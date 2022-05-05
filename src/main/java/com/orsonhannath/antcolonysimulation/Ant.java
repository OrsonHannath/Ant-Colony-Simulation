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

    //Food Stats
    private boolean _hasFoodTarget;
    private Vector2 foodTargetPos;

    private boolean hasFood;
    private int hasFoodAmount;

    private boolean inDanger;

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
        position = position.Plus(velocity.Multiply(0.016f));

        double angle = Math.toDegrees(Math.atan2(velocity.getYPos(), velocity.getXPos()));
        this.rotation = angle;

        //Check if the ant is colliding with something
        CheckCollisions();

        //Update Pheromone Trail
        PlacePheromones(Math.round(position.getXPos()), Math.round(position.getYPos()));

    }

    public void GraphicsUpdate(GraphicsContext gfx){

        gfx.setFill(this.colonyColor);
        gfx.fillRect(this.position.getXPos(), this.position.getYPos(), 2, 2);
    }

    private void CheckCollisions(){

        //Check Front Left
        // frontPixel = antWorld.getPixelAtPoint(Math.round(position.getXPos()), Math.round(position.getYPos()));

        //Check Front Middle

        //Check Front Right

        //Check Under Ant
        WorldObjectTypes underPixel = antWorld.getPixelAtPoint(Math.round(position.getXPos()), Math.round(position.getYPos()));
        PixelEventHandling(underPixel);

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

    }

    private void HandleFood(){

        if(!hasFood){

            hasFoodAmount = maxStrength;
            hasFood = true;
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

    private void PlacePheromones(int x, int y){

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
