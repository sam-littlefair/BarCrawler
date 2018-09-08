package uk.co.squaredsoftware.barcrawler;

public class Point {
    private final double x, y;
    private double key, price;
    private final String name;

    //This is the constructor method for the object Point
    public Point(double givenX, double givenY, String givenName, double givenKey, double givenprice) {
        x = givenX;
        y = givenY;
        name = givenName;
        key = givenKey;
        price = givenprice;
    }// Point

    /*This allows a reference to the object outside of the point class as it
    makes the reference public */
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public double getKey() {
        return key;
    }

    public void setKey(double p) {
        this.key = p;
    }

    public double getPrice() {
        return this.price;
    }

    public String toString() {
        String output = "Lat " + this.x + " Log" + this.y + " name" + this.name;
        return output;
    }

}// Point