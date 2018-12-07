package be.kul.gantry.domain;

import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;

import java.util.Objects;

public class MovementLog implements Comparable {
    private final double starttime;
    private final double endtime;
    private final Coordinaat startCoordinaat;
    private final Coordinaat eindCoordinaat;

    public MovementLog(double starttime, double endtime, Coordinaat startCoordinaat, Coordinaat eindCoordinaat) {
        this.starttime = starttime;
        this.endtime = endtime;
        this.startCoordinaat = startCoordinaat;
        this.eindCoordinaat = eindCoordinaat;
    }

    public double getStarttime() {
        return starttime;
    }

    public double getEndtime() {
        return endtime;
    }

    public Coordinaat getStartCoordinaat() {
        return startCoordinaat;
    }

    public Coordinaat getEindCoordinaat() {
        return eindCoordinaat;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        MovementLog that = (MovementLog) o;
        if (this.starttime > that.endtime)
            return -1;
        if (that.starttime > this.endtime)
            return -1;
        if (this.eindCoordinaat.getX()>that.eindCoordinaat.getX()){
            return 0;
        }
        return 10;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovementLog)) return false;
        MovementLog that = (MovementLog) o;
        return Double.compare(that.starttime, starttime) == 0 &&
                Double.compare(that.endtime, endtime) == 0 &&
                Objects.equals(startCoordinaat, that.startCoordinaat) &&
                Objects.equals(eindCoordinaat, that.eindCoordinaat);
    }

    @Override
    public int hashCode() {

        return Objects.hash(starttime, endtime, startCoordinaat, eindCoordinaat);
    }

    @Override
    public String toString() {
        return "MovementLog{" +
                "starttime=" + starttime +
                ", endtime=" + endtime +
                ", startCoordinaat=" + startCoordinaat +
                ", eindCoordinaat=" + eindCoordinaat +
                '}';
    }
}
