package com.orsonhannath.antcolonysimulation;

public class Vector2 {

    private float xPos;
    private float yPos;

    public Vector2(float x, float y){

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

    public Vector2 Multiply(float scalar){

        return new Vector2(this.xPos * scalar, this.yPos * scalar);
    }

    public Vector2 Divide(float scalar){

        return new Vector2(this.xPos / scalar, this.yPos / scalar);
    }

    public Vector2 Minus(Vector2 vector){

        return new Vector2(this.getXPos() - vector.getXPos(), this.yPos - vector.getYPos());
    }

    public Vector2 Plus(Vector2 vector){

        return new Vector2(this.getXPos() + vector.getXPos(), this.yPos + vector.getYPos());
    }

    public Vector2 ClampMagnitude(float maxLength){

        Vector2 returnVect = this;

        if(((this.xPos * this.xPos) + (this.yPos * this.yPos)) > (maxLength * maxLength)){

            return this.Normalized().Multiply(maxLength);
        }

        return returnVect;
    }

    public Vector2 Normalized(){

        double magnitude = Math.sqrt((this.xPos * this.xPos) + (this.yPos * this.yPos));
        return new Vector2((float)(this.xPos / magnitude), (float)(this.yPos / magnitude));
    }

    public String VectorString(){

        return "x: " + this.xPos + ", y: " + this.yPos;
    }
}
