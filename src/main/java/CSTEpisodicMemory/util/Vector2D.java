package CSTEpisodicMemory.util;

import org.sat4j.core.Vec;

public class Vector2D {

    private double x = 0;
    private double y = 0;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v){
        this.x = v.getX();
        this.y = v.getY();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector2D add(Vector2D v){
        double x_ = this.x + v.getX();
        double y_ = this.y + v.getY();
        return new Vector2D(x_, y_);
    }

    public Vector2D sub(Vector2D v){
        double x_ = this.x - v.getX();
        double y_ = this.y - v.getY();
        return new Vector2D(x_, y_);
    }

    public Vector2D normalize(){
        double mag = this.magnitude();
        double x_ = this.x / mag;
        double y_ = this.y / mag;
        return new Vector2D(x_, y_);
    }

    public double magnitude(){
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double angle(Vector2D v){
        double prod = this.x * v.getX() + this.y * v.getY();
        double cos = prod / (this.magnitude() * v.magnitude());
        return Math.acos(cos);
    }

    public boolean isSameQuadrant(Vector2D v) {
        boolean checkX = this.x >= 0 && v.getX() >= 0 || this.x <= 0 && v.getX() <= 0;
        boolean checkY = this.y >= 0 && v.getY() >= 0 || this.y <= 0 && v.getY() <= 0;
        return checkX && checkY;
    }

    @Override
    public String toString() {
        return "X: " + this.x + " - Y: " + this.y;
    }
}
