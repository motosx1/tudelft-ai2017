package ai2017.group5;

import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;


/**
 * Created by bartosz on 17.10.2017.
 */
public class HSpaceElem {
    private List<CriterionFeatures> criterionFeatures = new ArrayList<>();
    private double weight;

    public HSpaceElem() {
    }

    public HSpaceElem(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;
    }

    // Creates clean HSpace, without weights - AdditiveUtilitySpace agrument should be changed to Domain
    public HSpaceElem(AdditiveUtilitySpace myUtilitySpace) {
        Set<Map.Entry<Objective, Evaluator>> evaluators = myUtilitySpace.getEvaluators();
        try {
            this.criterionFeatures = new ArrayList<>();
            for (Map.Entry<Objective, Evaluator> entry : evaluators) {
                String criterionName = entry.getKey().getName();
                EvaluatorDiscrete evaluator = (EvaluatorDiscrete) entry.getValue();

                Set<ValueDiscrete> features = evaluator.getValues();
                Map<ValueDiscrete, Double> hSpaceElemFeatures = new HashMap<>();
                for (ValueDiscrete featureDiscrete : features) {
                    Double featureWeight = evaluator.getEvaluation(featureDiscrete);
                    hSpaceElemFeatures.put(featureDiscrete, featureWeight);
                }

                CriterionFeatures criterionFeatures = new CriterionFeatures(criterionName, hSpaceElemFeatures);
                cleanWeights(criterionFeatures);
                this.criterionFeatures.add(criterionFeatures);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanWeights(CriterionFeatures criterionFeatures) {
        criterionFeatures.setWeight(0.0);
        for (Map.Entry<ValueDiscrete, Double> entry : criterionFeatures.getFeatures().entrySet()) {
            entry.setValue(0.0);
        }
    }


    public List<CriterionFeatures> getCriterionFeatures() {
        return criterionFeatures;
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
                ", w=" + Math.round(weight) +
                '}';
    }

}
