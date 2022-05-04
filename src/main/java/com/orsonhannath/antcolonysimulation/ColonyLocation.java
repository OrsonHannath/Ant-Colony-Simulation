package com.orsonhannath.antcolonysimulation;

public class ColonyLocation {

    private float xPos;
    private float yPos;

    public ColonyLocation(float x, float y){

        this.xPos = x;
        this.yPos = y;
    }

    public float getXPos(){

        return this.xPos;
    }

    public float getYPos(){

        return this.yPos;
    }

    public void setXPos(float x){

        this.xPos = x;
    }

    public void setYPos(float y){

        this.yPos = y;
    }

    public void setPos(float x, float y){

        this.xPos = x;
        this.yPos = y;
    }
}
