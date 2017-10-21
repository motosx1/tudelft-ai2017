package src.ai2017;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.utility.EvaluatorDiscrete;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UtilitiesHelper {
//    private Map<Integer, Double> history = new HashMap<>();

    public Map<Integer, Double> calculatePhbMap(Bid oppBid, List<HSpaceElem> hSpace, int step) {
        Map<Integer, Double> pBHMap = new HashMap<>();

        for (int i = 0; i < hSpace.size(); i++) {
            HSpaceElem hSpaceEntry = hSpace.get(i);
            pBHMap.put(i, calculatePbh(oppBid, hSpaceEntry, step));
        }

        return pBHMap;
    }

    double calculatePhb(List<HSpaceElem> hSpace, int elementIndex, Map<Integer, Double> pBHMap, double denominator) {
        HSpaceElem hSpaceEntry = hSpace.get(elementIndex);
        Double phj = hSpaceEntry.getWeight();

        double numerator = phj * pBHMap.get(elementIndex);
        return numerator / denominator;
    }

    public double calculateDenominator(List<HSpaceElem> hSpace, Map<Integer, Double> pBHMap) {
        double result = 0;

        for (int i = 0; i < hSpace.size(); i++) {
            result += hSpace.get(i).getWeight() * pBHMap.get(i);
        }

        return result;

    }

    private double calculatePbh(Bid oppBid, HSpaceElem hSpaceEntry, int step) {
        double sigma = 0.3;
        double c = 1 / (sigma * Math.sqrt(2 * Math.PI));

        double index = Math.pow(calculateUtility(oppBid, hSpaceEntry) - utilityB(step), 2) / (2 * Math.pow(sigma, 2));

        return c * Math.exp(-index);
    }

    public double calculateUtility(Bid oppBid, HSpaceElem hSpaceEntry) {
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
        System.out.println("\tutility=" + utility);
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

    Double getMaxUtility(Map<Bid, Double> myPossibleBids) {
        return myPossibleBids.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getValue();
    }
}
