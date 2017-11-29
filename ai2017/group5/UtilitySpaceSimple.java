package ai2017.group5;

import ai2017.group5.helpers.math.UtilitiesHelper;
import negotiator.Bid;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;


public class UtilitySpaceSimple {
    private List<CriterionFeatures> criterionFeatures = new ArrayList<>();
    private final UtilitiesHelper utilitiesHelper = new UtilitiesHelper();
    private double weight;


    public UtilitySpaceSimple(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;
    }

    // Creates clean HSpace, without weights - AdditiveUtilitySpace argument should be changed to Domain
    public UtilitySpaceSimple(AdditiveUtilitySpace myUtilitySpace) throws Exception {
        Set<Map.Entry<Objective, Evaluator>> evaluators = myUtilitySpace.getEvaluators();
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
    }

    private void cleanWeights(CriterionFeatures criterionFeatures) {
        criterionFeatures.setWeight(0.0);
        for (Map.Entry<ValueDiscrete, Double> entry : criterionFeatures.getFeatures().entrySet()) {
            entry.setValue(0.0);
        }
    }


    //ćheck the access methods, should be a simpler weight to get the values
    public double getUtility(Bid oppBid) {
        Map<String, Value> discreteOppValues = utilitiesHelper.getDiscreteBidMap(oppBid);

        double utility = 0;
        for (Map.Entry<String, Value> oppBidDiscrete : discreteOppValues.entrySet()) {
            String oppCriterion = oppBidDiscrete.getKey();
            Value oppFeature = oppBidDiscrete.getValue();

            // criterion weight * feature weight + ....
            CriterionFeatures myCriterionFeaturesList = utilitiesHelper.getCriterionFeaturesByCriterionName(criterionFeatures, oppCriterion);

            double probableCriterionWeight = myCriterionFeaturesList != null ? myCriterionFeaturesList.getWeight() : 0.0;
            double probableFeatureWeight = utilitiesHelper.getFeatureWeight(oppFeature, myCriterionFeaturesList);
            double probableMaxFeatureWeight = utilitiesHelper.getMaxFeatureWeight(myCriterionFeaturesList);

            utility += probableCriterionWeight * (probableFeatureWeight/probableMaxFeatureWeight);
        }

        return utility;
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
        return "UtilitySpaceSimple{" +
                "criteria=" + criterionFeatures +
                ", w=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UtilitySpaceSimple that = (UtilitySpaceSimple) o;

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
