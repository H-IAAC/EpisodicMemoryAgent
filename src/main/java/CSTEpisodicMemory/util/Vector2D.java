package CSTEpisodicMemory.util;

import org.sat4j.core.Vec;

public class Vector2D implements Cloneable{

    private double x = 0;
    private double y = 0;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
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
        this.x += v.getX();
        this.y += v.getY();
        return this;
    }

    public Vector2D sub(Vector2D v){
        this.x -= v.getX();
        this.y -= v.getY();
        return this;
    }

    public Vector2D normalize(){
        double mag = this.magnitude();
        this.x = this.x / mag;
        this.y = this.y / mag;
        return this;
    }

    public double magnitude(){
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double angle(Vector2D v){
        Vector2D a = null;
        try {
            a = ((Vector2D) this.clone()).sub(v);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return Math.atan2(a.getX(), a.getY());
    }
}
