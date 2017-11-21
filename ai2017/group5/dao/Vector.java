package ai2017.group5.dao;

/**
 * Created by bartosz on 20.10.2017.
 */
public class Vector {
    private Double x;
    private Double y;

    public Vector(Position startPosition, Position endPosition) {
        this.x = endPosition.getMyUtility() - startPosition.getMyUtility();
        this.y = endPosition.getHisUtility() - startPosition.getHisUtility();
    }

    private Vector(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static Vector getMirroredVector(Vector opponentsVector) {
        return new Vector(opponentsVector.y, opponentsVector.x);
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }
}
