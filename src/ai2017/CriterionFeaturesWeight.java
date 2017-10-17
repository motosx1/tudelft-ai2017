package src.ai2017;

import java.util.List;

import static org.apache.commons.math.util.MathUtils.round;

/**
 * Created by bartosz on 17.10.2017.
 */
public class CriterionFeaturesWeight {
    private List<CriterionFeatures> criterionFeatures;
    private double weight;

    public CriterionFeaturesWeight(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;
    }

    public List<CriterionFeatures> getCriterionFeatures() {
        return criterionFeatures;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return
                "criterion=" + criterionFeatures +
                ", w=" + round(weight, 2) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriterionFeaturesWeight that = (CriterionFeaturesWeight) o;

        if (Double.compare(that.weight, weight) != 0) return false;
        return criterionFeatures != null ? criterionFeatures.equals(that.criterionFeatures) : that.criterionFeatures == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = criterionFeatures != null ? criterionFeatures.hashCode() : 0;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
