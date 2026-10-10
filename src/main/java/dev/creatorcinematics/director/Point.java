package dev.creatorcinematics.director;

public record Point(double x, double y, double z) {
    public Point add(Point p) { return new Point(x+p.x,y+p.y,z+p.z); }
    public Point subtract(Point p) { return new Point(x-p.x,y-p.y,z-p.z); }
    public Point scale(double s) { return new Point(x*s,y*s,z*s); }
    public double length() { return Math.sqrt(x*x+y*y+z*z); }
    public double distance(Point p) { return subtract(p).length(); }
    public Point lerp(Point p, double t) { return scale(1-t).add(p.scale(t)); }
}
