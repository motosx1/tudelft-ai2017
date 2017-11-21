package ai2017.group5;

import negotiator.Bid;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

class MySpacePreparationHelper {


    /**
     * Create a map of all possible bids to offer, which calculated utilities.
     * Takes into account reservation value, and doesn't add bids, which utility is below the reservation value.
     *
     * @param utilitySpace my utility space
     * @return Map of my all possible bids with the calculated utility for each of them
     */
    Map<Bid, Double> generateMyPossibleBids(AdditiveUtilitySpace utilitySpace) {
        List<Bid> possibleBidCombinations = generatePossibleBidCombinations(utilitySpace);

        return assignUtilities(possibleBidCombinations, utilitySpace);
    }

    private List<Bid> generatePossibleBidCombinations(AdditiveUtilitySpace utilitySpace) {
        List<List<ValueDiscreteDouble>> issueSets = generateIssueSets(utilitySpace);
        List<List<ValueDiscreteDouble>> possibleCombinations = CartesianProduct.calculate(issueSets);
        return createBids(possibleCombinations, utilitySpace);
    }

    private List<List<ValueDiscreteDouble>> generateIssueSets(AdditiveUtilitySpace utilitySpace) {
        List<List<ValueDiscreteDouble>> featuresList = new ArrayList<>();

        try {
            for (Map.Entry<Objective, Evaluator> entry : utilitySpace.getEvaluators()) {
                EvaluatorDiscrete evaluator = (EvaluatorDiscrete) entry.getValue();

                Set<ValueDiscrete> features = evaluator.getValues();
                List<ValueDiscreteDouble> hSpaceElemFeatures = new ArrayList<>();
                for (ValueDiscrete featureDiscrete : features) {
                    Double featureWeight = evaluator.getEvaluation(featureDiscrete);
                    hSpaceElemFeatures.add(new ValueDiscreteDouble(entry.getKey(), featureDiscrete, featureWeight));
                }

                featuresList.add(hSpaceElemFeatures);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return featuresList;
    }

    private List<Bid> createBids(List<List<ValueDiscreteDouble>> possibleCombinations, AdditiveUtilitySpace utilitySpace) {
        List<Bid> bids = new ArrayList<>();
        for (List<ValueDiscreteDouble> combination : possibleCombinations) {
            HashMap<Integer, Value> bidEntries = new HashMap<>();
            for (ValueDiscreteDouble entry : combination) {
                Objective criterion = entry.criterion;
                int index = utilitySpace.getDomain().getObjectives().indexOf(criterion);
                bidEntries.put(index, entry.valueDiscrete);
            }
            bids.add(new Bid(utilitySpace.getDomain(), bidEntries));
        }

        return bids;
    }

    private Map<Bid, Double> assignUtilities(List<Bid> possibleBidCombinations, AdditiveUtilitySpace utilitySpace) {
        Map<Bid, Double> result = new HashMap<>();
        for (Bid bid : possibleBidCombinations) {
            if (utilitySpace.getUtility(bid) >= utilitySpace.getReservationValueUndiscounted()) {
                result.put(bid, utilitySpace.getUtility(bid));
            }
        }

        return result;
    }


}
