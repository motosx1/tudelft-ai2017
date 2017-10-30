package src.ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UtilitiesHelper {

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

            utility += probableCriterionWeight * probableFeatureWeight;
        }

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


    HSpaceElem getMeanWeights(AbstractUtilitySpace myUtilitySpace, Map<AgentID, HSpaceElem> opponentsWeightsMap) {
        HSpaceElem meanHSpace = new HSpaceElem((AdditiveUtilitySpace) myUtilitySpace);

        meanHSpace.setWeight(opponentsWeightsMap.entrySet().stream()
                .mapToDouble(a -> a.getValue().getWeight())
                .average().getAsDouble());

        for (Issue issue : myUtilitySpace.getDomain().getIssues()) {
            String issueName = issue.getName();
            List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();

            setCriterionMean(issueName, opponentsWeightsMap, meanHSpace);
            for (ValueDiscrete value : values) {
                setFeatureMean(issueName, value, opponentsWeightsMap, meanHSpace);
            }

        }


        return meanHSpace;
    }

    private void setFeatureMean(String issueName, ValueDiscrete value, Map<AgentID, HSpaceElem> opponentsWeightsMap, HSpaceElem meanHSpace) {
        double averageWeightOfFeatures = 0;
        for (Map.Entry<AgentID, HSpaceElem> entry : opponentsWeightsMap.entrySet()) {
            HSpaceElem hSpaceElem = entry.getValue();

            averageWeightOfFeatures += hSpaceElem.getCriterionFeatures().stream()
                    .filter(criterionFeatures -> criterionFeatures.getCriterion().equalsIgnoreCase(issueName))
                    .map(CriterionFeatures::getFeatures)
                    .mapToDouble(map -> map.get(value))
                    .average().getAsDouble();
        }

        Map<ValueDiscrete, Double> meanFeatures = meanHSpace.getCriterionFeatures().stream()
                .filter(entry1 -> entry1.getCriterion().equalsIgnoreCase(issueName))
                .map(CriterionFeatures::getFeatures)
                .findFirst().get();

        meanFeatures.put(value, averageWeightOfFeatures / opponentsWeightsMap.size());

    }

    private void setCriterionMean(String issueName, Map<AgentID, HSpaceElem> opponentsWeightsMap, HSpaceElem meanHSpace) {
        double averageWeightOfCriterion = 0;
        for (Map.Entry<AgentID, HSpaceElem> entry : opponentsWeightsMap.entrySet()) {
            HSpaceElem hSpaceElem = entry.getValue();

            averageWeightOfCriterion += hSpaceElem.getCriterionFeatures().stream()
                    .filter(criterionFeatures -> criterionFeatures.getCriterion().equalsIgnoreCase(issueName))
                    .findFirst().get().getWeight();

        }

        averageWeightOfCriterion = averageWeightOfCriterion / opponentsWeightsMap.size();

        CriterionFeatures criterion = meanHSpace.getCriterionFeatures().stream().filter(entry1 -> entry1.getCriterion().equalsIgnoreCase(issueName)).findFirst().get();
        criterion.setWeight(averageWeightOfCriterion);


    }

}
