package src.ai2017.group5;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;

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

    double calculateUtility(Bid oppBid, HSpaceElem hSpaceEntry) {
        Map<String, Value> discreteOppValues = getDiscreteBidMap(oppBid);

        double utility = 0;
        for (Map.Entry<String, Value> oppBidDiscrete : discreteOppValues.entrySet()) {
            String oppCriterion = oppBidDiscrete.getKey();
            Value oppFeature = oppBidDiscrete.getValue();

            // criterion weight * feature weight + ....
            CriterionFeatures myCriterionFeaturesList = getCriterionFeaturesByCriterionName(hSpaceEntry, oppCriterion);

            double probableCriterionWeight = myCriterionFeaturesList != null ? myCriterionFeaturesList.getWeight() : 0.0;
            double probableFeatureWeight = getFeatureWeight(oppFeature, myCriterionFeaturesList);

            utility += probableCriterionWeight * probableFeatureWeight;
        }

        return utility;
    }

    private double getFeatureWeight(Value oppFeature, CriterionFeatures myCriterionFeaturesList) {
        for (Map.Entry<ValueDiscrete, Double> entry : myCriterionFeaturesList.getFeatures().entrySet()) {
            if (entry.getKey().equals(oppFeature)) {
                return entry.getValue();
            }
        }

        return 0.0;
    }

    private CriterionFeatures getCriterionFeaturesByCriterionName(HSpaceElem hSpaceEntry, String oppCriterion) {
        for (CriterionFeatures entry : hSpaceEntry.getCriterionFeatures()) {
            if (oppCriterion.equalsIgnoreCase(entry.getCriterion())) {
                return entry;
            }
        }

        return null;
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


    HSpaceElem getMeanWeights(AbstractUtilitySpace myUtilitySpace, Map<AgentID, HSpaceElem> opponentsWeightsMap) {
        HSpaceElem meanHSpace = new HSpaceElem((AdditiveUtilitySpace) myUtilitySpace);

        meanHSpace.setWeight(getAverageWeight(opponentsWeightsMap));

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

    private double getAverageWeight(Map<AgentID, HSpaceElem> opponentsWeightsMap) {
        double sum = 0;
        for (Map.Entry<AgentID, HSpaceElem> entry : opponentsWeightsMap.entrySet()) {
            sum += entry.getValue().getWeight();
        }
        double size = opponentsWeightsMap.entrySet().size();

        return sum / size;
    }

    private void setFeatureMean(String issueName, ValueDiscrete value, Map<AgentID, HSpaceElem> opponentsWeightsMap, HSpaceElem meanHSpace) {
        double averageWeightOfFeatures = 0;
        for (Map.Entry<AgentID, HSpaceElem> entry : opponentsWeightsMap.entrySet()) {
            HSpaceElem hSpaceElem = entry.getValue();

            averageWeightOfFeatures += getAverageWeightOfFeatures(issueName, value, hSpaceElem);
        }

        Map<ValueDiscrete, Double> meanFeatures = getFeaturesByName(issueName, meanHSpace);

        if (meanFeatures != null) {
            meanFeatures.put(value, averageWeightOfFeatures / opponentsWeightsMap.size());
        }

    }

    private Map<ValueDiscrete, Double> getFeaturesByName(String issueName, HSpaceElem meanHSpace) {
        for (CriterionFeatures entry : meanHSpace.getCriterionFeatures()) {
            if (entry.getCriterion().equalsIgnoreCase(issueName)) {
                return entry.getFeatures();
            }
        }

        return null;
    }

    private double getAverageWeightOfFeatures(String issueName, ValueDiscrete value, HSpaceElem hSpaceElem) {
        double sum = 0;
        double elems = 0;
        for (CriterionFeatures criterionFeatures : hSpaceElem.getCriterionFeatures()) {
            if (criterionFeatures.getCriterion().equalsIgnoreCase(issueName)) {
                sum += criterionFeatures.getFeatures().get(value);
                elems++;
            }
        }

        return sum / elems;
    }

    private void setCriterionMean(String issueName, Map<AgentID, HSpaceElem> opponentsWeightsMap, HSpaceElem meanHSpace) {
        double averageWeightOfCriterion = 0;
        for (Map.Entry<AgentID, HSpaceElem> entry : opponentsWeightsMap.entrySet()) {
            HSpaceElem hSpaceElem = entry.getValue();

            CriterionFeatures criterionByName = getCriterionByName(issueName, hSpaceElem);
            averageWeightOfCriterion += criterionByName != null ? criterionByName.getWeight() : 0;

        }

        averageWeightOfCriterion = averageWeightOfCriterion / opponentsWeightsMap.size();

        CriterionFeatures criterion = getCriterionByName(issueName, meanHSpace);
        if (criterion != null) {
            criterion.setWeight(averageWeightOfCriterion);
        }


    }

    private CriterionFeatures getCriterionByName(String issueName, HSpaceElem hSpaceElem) {
        for (CriterionFeatures criterionFeatures : hSpaceElem.getCriterionFeatures()) {
            if (criterionFeatures.getCriterion().equalsIgnoreCase(issueName)) {
                return criterionFeatures;
            }
        }
        return null;
    }

}
