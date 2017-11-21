package ai2017.group5.dao;


public class Position {

    private Double hisUtility; //y
    private Double myUtility; //x

    public Position(Double myUtility, Double hisUtility) {
        this.myUtility = myUtility;
        this.hisUtility = hisUtility;
    }

    public Double getHisUtility() {
        return hisUtility;
    }

    void setHisUtility(Double hisUtility) {
        this.hisUtility = hisUtility;
    }

    public Double getMyUtility() {
        return myUtility;
    }

    void setMyUtility(Double myUtility) {
        this.myUtility = myUtility;
    }

    public Position add(Vector vector) {
        return new Position(myUtility + vector.getX(), hisUtility + vector.getY());
    }
}
