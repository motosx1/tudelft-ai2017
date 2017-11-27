package ai2017.group5.dao;

import ai2017.group5.UtilitySpaceSimple;

public class UtilityProbabilityObject {
    private final UtilitySpaceSimple utilitySpace;
    private double probability;

    public UtilityProbabilityObject(UtilitySpaceSimple utilitySpace, double probability) {
        this.utilitySpace = utilitySpace;
        this.probability = probability;
    }

    public UtilitySpaceSimple getUtilitySpace() {
        return utilitySpace;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
