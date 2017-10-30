package src.ai2017.group5;

class Position {

    private Double hisUtility; //y
    private Double myUtility; //x

    Position(Double myUtility, Double hisUtility) {
        this.myUtility = myUtility;
        this.hisUtility = hisUtility;
    }

    Double getHisUtility() {
        return hisUtility;
    }

    void setHisUtility(Double hisUtility) {
        this.hisUtility = hisUtility;
    }

    Double getMyUtility() {
        return myUtility;
    }

    void setMyUtility(Double myUtility) {
        this.myUtility = myUtility;
    }

    public Position add(Vector vector) {
        return new Position(myUtility + vector.getX(), hisUtility + vector.getY());
    }
}
