package ai2017.group5.dao;

import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;

public class ValueDiscreteDouble {
    Objective criterion;
    ValueDiscrete valueDiscrete;
    Double weight;

    public ValueDiscreteDouble(Objective criterion, ValueDiscrete valueDiscrete, Double weight) {
        this.criterion = criterion;
        this.valueDiscrete = valueDiscrete;
        this.weight = weight;
    }

    public Objective getCriterion() {
        return criterion;
    }

    public ValueDiscrete getValueDiscrete() {
        return valueDiscrete;
    }
}