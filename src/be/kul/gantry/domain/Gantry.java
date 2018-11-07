package be.kul.gantry.domain;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin, xMax;
    private int startX, startY;
    private final double xSpeed, ySpeed;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    private Item item;

    public Gantry(int id,
                  int xMin, int xMax,
                  int startX, int startY,
                  double xSpeed, double ySpeed) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.startX = startX;
        this.startY = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }

    public int getId() {
        return id;
    }

    public int getXMax() {
        return xMax;
    }

    public int getXMin() {
        return xMin;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public boolean overlapsGantryArea(Gantry g) {
        return g.xMin < xMax && xMin < g.xMax;
    }

    public int[] getOverlapArea(Gantry g) {

        int maxmin = Math.max(xMin, g.xMin);
        int minmax = Math.min(xMax, g.xMax);

        if (minmax < maxmin)
            return null;
        else
            return new int[]{maxmin, minmax};
    }

    public boolean canReachSlot(Slot s) {
        return xMin <= s.getCenterX() && s.getCenterX() <= xMax;
    }

    @Override
    public String toString() {
        return "Gantry{" +
                "id=" + id +
                ", xMin=" + xMin +
                ", xMax=" + xMax +
                ", startX=" + startX +
                ", startY=" + startY +
                ", xSpeed=" + xSpeed +
                ", ySpeed=" + ySpeed +
                '}';
    }

    public double moveGantry(Coordinaat coord) {
        double movingTime = calculateTime(coord);
        this.startX = coord.getX() * 10 + 5;
        this.startY = coord.getY() * 10 + 5;
        return movingTime;
    }

    public double calculateTime(Coordinaat coord) {
        double xTime = (Math.abs(this.startX - (coord.getX() * 10 + 5)) / xSpeed);
        double yTime = (Math.abs(this.startY - (coord.getY() * 10 + 5)) / ySpeed);

        return xTime > yTime ? xTime : yTime;
    }

    public String toLog() {
        return this.id + ";" + Main.timer + ";" + this.startX + ";" + this.startY + ";"
                + ((this.item == null) ? "null" : this.item.getId());
    }
}
