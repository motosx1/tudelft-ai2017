package src.ai2017;

import java.util.List;

import static org.apache.commons.math.util.MathUtils.round;

/**
 * Created by bartosz on 17.10.2017.
 */
public class HSpaceElem {
    List<CriterionFeatures> criterionFeatures;
    double weight;

    public HSpaceElem(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;

    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "HSpaceElem{" +
                "criteria=" + criterionFeatures +
                ", w=" + round(weight, 2) +
                '}';
    }
}
