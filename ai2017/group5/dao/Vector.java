package ai2017.group5.dao;

import ai2017.group5.helpers.MoveType;

import static ai2017.group5.helpers.MoveType.*;

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

    public static Vector getSameVector(Vector opponentsVector) {
        return new Vector(opponentsVector.x, opponentsVector.y);
    }

    public MoveType getMoveType() {
        if (x < 0 && y < 0) {
            return UNFORTUNATE;
        } else if (x < 0 && y > 0) {
            return SELFISH;
        } else if (x >= 0 && y <= 0) {
            return CONCESSION;
        } else {
            return FORTUNATE;
        }
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Vector divideBy(double divideBy) {
        this.x = this.x / divideBy;
        this.y = this.y / divideBy;
        return this;
    }

    public Vector multiplyBy(double multiplyBy) {
        this.x = this.x * multiplyBy;
        this.y = this.y * multiplyBy;
        return this;
    }
}
