package ai2017.group5;

import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;

class ValueDiscreteDouble {
    Objective criterion;
    ValueDiscrete valueDiscrete;
    Double weight;

    ValueDiscreteDouble(Objective criterion, ValueDiscrete valueDiscrete, Double weight) {
        this.criterion = criterion;
        this.valueDiscrete = valueDiscrete;
        this.weight = weight;
    }
}