package com.orsonhannath.antcolonysimulation;

import javafx.scene.paint.Color;

import java.util.Random;
import java.util.Vector;

// An ant colony is a base for a breed of ant
// Each pixel that is of one type of ant will spawn into an ant at runtime

public class AntColony {

    private Color colonyColor;
    private String colonyName;

    private Vector<Ant> colonyAnts = new Vector<>();
    private Vector<ColonyLocation> colonyLocations = new Vector<>();

    //Colony Main Stats
    private float maxSpeedCol = 5f;
    private float maxRangeCol = 1;
    private int maxStrengthCol = 5;
    private float maxPickupRangeCol = 1;
    private float steerStrengthCol = 0.5f;
    private float wanderStrengthCol = 0.1f;
    private float phermStrength = 0.1f;

    public AntColony(Color _colonyColor, String _colonyName){

        this.colonyColor = _colonyColor;
        this.colonyName = _colonyName;
    }

    public void AddLocation(float x, float y){

        colonyLocations.add(new ColonyLocation(x, y));
    }

    public Vector<ColonyLocation> GetLocations(){

        return colonyLocations;
    }

    public void AddAnt(int x, int y, AntWorld antWorld){

        Random rand = new Random();
        colonyAnts.add(new Ant(antWorld, this, this.colonyColor, x + rand.nextFloat(3), y + rand.nextFloat(3), maxSpeedCol + rand.nextFloat(2), maxRangeCol, maxStrengthCol + rand.nextInt(2), maxPickupRangeCol, steerStrengthCol + rand.nextFloat(2), wanderStrengthCol + rand.nextFloat(2), colonyAnts.size() + 1, phermStrength + rand.nextFloat(0.2f)));
    }

    public Vector<Ant> GetAnts(){

        return this.colonyAnts;
    }
}
