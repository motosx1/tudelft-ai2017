package src.ai2017;

import negotiator.Bid;
import negotiator.issue.Value;

import java.util.HashMap;
import java.util.Map;

public class MathHelper {

    public double calculatePhb(Bid oppBid, HSpaceElem hSpaceEntry, int step) {
        Double phj = hSpaceEntry.getWeight();

        double numerator = phj * calculatePbh(oppBid, hSpaceEntry, step);
        double denominator = 1;

        return numerator / denominator;
    }

    private double calculatePbh(Bid oppBid, HSpaceElem hSpaceEntry, int step) {
        double sigma = 0.3;
        double c = 1 / (sigma * Math.sqrt(2 * Math.PI));

        double index = Math.pow(utilityBgivenH(oppBid, hSpaceEntry) - utilityB(step), 2) / (2 * Math.pow(sigma, 2));

        return c * Math.exp(index);
    }

    private double utilityBgivenH(Bid oppBid, HSpaceElem hSpaceEntry) {
        Map<String, Value> discreteOppValues = getDiscreteBidMap(oppBid);

        double utility = 0;
        for (Map.Entry<String, Value> oppBidDiscrete : discreteOppValues.entrySet()) {
            String oppCriterion = oppBidDiscrete.getKey();
            Value oppFeature = oppBidDiscrete.getValue();

            // criterion weight * feature weight + ....
            CriterionFeatures myCriterionFeaturesList = hSpaceEntry.getCriterionFeatures().stream()
                    .filter(criterionFeatures -> oppCriterion.equalsIgnoreCase(criterionFeatures.getCriterion()))
                    .findFirst().get();

            Double probableCriterionWeight = myCriterionFeaturesList.getWeight();

            Double probableFeatureWeight = myCriterionFeaturesList.getFeatures().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(oppFeature)).findFirst().get().getValue();


            System.out.println(oppCriterion + "->" + probableCriterionWeight + "; " + oppFeature + "->" + probableFeatureWeight + " = " + (probableCriterionWeight * probableFeatureWeight));

            utility += probableCriterionWeight * probableFeatureWeight;
        }
        System.out.println("\tutility="+utility);
        System.out.println("---");

        return utility;
    }

    private Map<String, Value> getDiscreteBidMap(Bid oppBid) {
        Map<String, Value> discreteOppValues = new HashMap<>();

        for (Map.Entry<Integer, Value> oppValue : oppBid.getValues().entrySet()) {
            Integer criterionIndex = oppValue.getKey();
            String criterionName = oppBid.getIssues().get(criterionIndex - 1).getName();
            discreteOppValues.put(criterionName, oppValue.getValue());
        }
        return discreteOppValues;
    }

    private double utilityB(int step) {
        return 1 - 0.05 * step;  //TODO change to consider time horizon
    }


}
