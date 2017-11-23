package ai2017.group5;

import negotiator.issue.ValueDiscrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bartosz on 17.10.2017.
 */
public class CriterionFeatures {
    private String criterion;
    private Map<ValueDiscrete, Double> features = new HashMap<>();
    private double weight;

    public CriterionFeatures(String criterion, Map<ValueDiscrete, Double> features) {
        this.criterion = criterion;
        this.features = features;
    }

    public CriterionFeatures(String criterion, Map<ValueDiscrete, Double> features, double criterionWeight) {
        this.criterion = criterion;
        this.features = features;
        this.weight = criterionWeight;
    }

    public CriterionFeatures(CriterionFeatures criterionFeatures) {
        this.criterion = criterionFeatures.getCriterion();
        this.features = new HashMap<>(criterionFeatures.getFeatures());
        this.weight = criterionFeatures.getWeight();
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public Map<ValueDiscrete, Double> getFeatures() {
        return features;
    }

    public void setFeatures(Map<ValueDiscrete, Double> features) {
        this.features = features;
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
                "criterion='" + criterion + '\'' +
                        ", features=" + features +
                        ", weight=" + weight +
                        '}';
    }

    public static List<List<CriterionFeatures>> fixCriterionFeaturesPermutations(List<List<CriterionFeatures>> criterionPermutations) {
        List<List<CriterionFeatures>> result = new ArrayList<>();
        for (List<CriterionFeatures> criterionPermutation : criterionPermutations) {
            List<CriterionFeatures> result2 = new ArrayList<>();
            for (CriterionFeatures criterionFeatures : criterionPermutation) {
                CriterionFeatures c = new CriterionFeatures(criterionFeatures);
                result2.add(c);
            }
            result.add(result2);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriterionFeatures that = (CriterionFeatures) o;

        if (Double.compare(that.weight, weight) != 0) return false;
        if (criterion != null ? !criterion.equals(that.criterion) : that.criterion != null) return false;
        return features != null ? features.equals(that.features) : that.features == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = criterion != null ? criterion.hashCode() : 0;
        result = 31 * result + (features != null ? features.hashCode() : 0);
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
