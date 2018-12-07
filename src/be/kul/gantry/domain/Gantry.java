package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin, xMax;
    private int startX, startY;
    private final double xSpeed, ySpeed;
    private HashMap<Double, Coordinaat> logfile = new HashMap<>();
    private ArrayList<MovementLog> movementLogArrayList = new ArrayList();
    Gantry otherGantry;

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
      /*  if (otherGantry == null) {
            if (this.equals(Main.prob.getGantries().get(0))) {
                otherGantry = Main.prob.getGantries().get(1);
            } else {
                otherGantry = Main.prob.getGantries().get(0);
            }
        }**/
        double startTime = Main.timer;
        double movingTime = calculateTime(coord);
        double endTime = startTime + movingTime;
       /* MovementLog moveLog = new MovementLog(startTime, endTime, new Coordinaat(this.startX, this.startY), coord);
        for (int i = 0; i < otherGantry.movementLogArrayList.size(); i++) {
            int result;
            if(this.id==0){
                result=moveLog.compareTo(otherGantry.movementLogArrayList.get(i));
            }else{
                result=otherGantry.movementLogArrayList.get(i).compareTo(moveLog);

            }

        }
        for (MovementLog log : otherGantry.movementLogArrayList) {

        }
        movementLogArrayList.add(moveLog);*/
        this.startX = coord.xValue();
        this.startY = coord.yValue();
        return movingTime;
    }

    public double calculateTime(Coordinaat coord) {
        double xTime = (Math.abs(this.startX - (coord.xValue())) / xSpeed);
        double yTime = (Math.abs(this.startY - (coord.yValue())) / ySpeed);

        return xTime > yTime ? xTime : yTime;
    }

    public String toLog() {
        logfile.put(Main.timer, new Coordinaat(this.startX, this.startY));
        return this.id + ";" + Main.timer + ";" + this.startX + ";" + this.startY + ";"
                + ((this.item == null) ? "null" : this.item.getId());
    }
}
