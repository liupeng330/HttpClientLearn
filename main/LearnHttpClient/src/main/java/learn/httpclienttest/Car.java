package learn.httpclienttest;

/**
 * Created by PLiu on 2017/4/5.
 */
public class Car {

    private String vin;
    private String color;
    private Integer miles;

    public String getVIN() {
        return vin;
    }

    public void setVIN(String VIN) {
        this.vin = VIN;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getMiles() {
        return miles;
    }

    public void setMiles(Integer miles) {
        this.miles = miles;
    }

    @Override
    public String toString() {
        return "Car{" + "vin='" + vin + '\'' + ", color='" + color + '\'' + ", miles=" + miles + '}';
    }
}
