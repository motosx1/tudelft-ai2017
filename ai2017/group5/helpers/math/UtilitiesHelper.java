package ai2017.group5.helpers.math;

import ai2017.group5.CriterionFeatures;
import ai2017.group5.UtilitySpaceSimple;
import ai2017.group5.dao.UtilityProbabilityObject;
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

public class UtilitiesHelper {

    public Map<Integer, Double> calculatePhbMap(Bid oppBid, List<UtilitySpaceSimple> hSpace, double step) {
        Map<Integer, Double> pBHMap = new HashMap<>();

        for (int i = 0; i < hSpace.size(); i++) {
            UtilitySpaceSimple hSpaceEntry = hSpace.get(i);
            pBHMap.put(i, calculatePbh(oppBid, hSpaceEntry, step));
        }

        return pBHMap;
    }

    public double calculatePhb(List<UtilitySpaceSimple> hSpace, int elementIndex, Map<Integer, Double> pBHMap, double denominator) {
        UtilitySpaceSimple hSpaceEntry = hSpace.get(elementIndex);
        Double phj = hSpaceEntry.getWeight();

        double numerator = phj * pBHMap.get(elementIndex);
        return numerator / denominator;
    }

    public double calculateDenominator(List<UtilitySpaceSimple> hSpace, Map<Integer, Double> pBHMap) {
        double result = 0;

        for (int i = 0; i < hSpace.size(); i++) {
            result += hSpace.get(i).getWeight() * pBHMap.get(i);
        }
        if (result == 0) {
            result = 0.00001;
        }

        return result;

    }

    private double calculatePbh(Bid oppBid, UtilitySpaceSimple utilitySpaceSimple, double step) {
        double sigma = 0.3;
        double c = 1 / (sigma * Math.sqrt(2 * Math.PI));

        double index = Math.pow(utilitySpaceSimple.getUtility(oppBid) - utilityB(step), 2) / (2 * Math.pow(sigma, 2));

        double exp = Math.exp(-index);
        if(exp==0){
            exp = 0.00001;
        }

        return c * exp;
    }


    public double getFeatureWeight(Value oppFeature, CriterionFeatures myCriterionFeaturesList) {
        for (Map.Entry<ValueDiscrete, Double> entry : myCriterionFeaturesList.getFeatures().entrySet()) {
            if (entry.getKey().equals(oppFeature)) {
                return entry.getValue();
            }
        }

        return 0.0;
    }

    public double getMaxFeatureWeight(CriterionFeatures myCriterionFeaturesList) {
        double max = 0;
        for (Map.Entry<ValueDiscrete, Double> entry : myCriterionFeaturesList.getFeatures().entrySet()) {
            if (max < entry.getValue()) {
                max = entry.getValue();
            }
        }

        return max;
    }

    public CriterionFeatures getCriterionFeaturesByCriterionName(List<CriterionFeatures> criterionFeaturesList, String oppCriterion) {
        for (CriterionFeatures entry : criterionFeaturesList) {
            if (oppCriterion.equalsIgnoreCase(entry.getCriterion())) {
                return entry;
            }
        }

        return null;
    }

    public Map<String, Value> getDiscreteBidMap(Bid oppBid) {
        Map<String, Value> discreteOppValues = new HashMap<>();

        for (Map.Entry<Integer, Value> oppValue : oppBid.getValues().entrySet()) {
            Integer criterionIndex = oppValue.getKey();
            String criterionName = oppBid.getIssues().get(criterionIndex - 1).getName();
            discreteOppValues.put(criterionName, oppValue.getValue());
        }
        return discreteOppValues;
    }


    private double utilityB(double step) {
        return 1 - 0.05 * step;
    }


    public UtilityProbabilityObject getMeanWeights(AbstractUtilitySpace myUtilitySpace, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap) throws Exception {
        UtilityProbabilityObject meanHSpaceWithProbability = new UtilityProbabilityObject(new UtilitySpaceSimple((AdditiveUtilitySpace) myUtilitySpace), 0);

        meanHSpaceWithProbability.getUtilitySpace().setWeight(getAverageWeight(opponentsWeightsMap));
        meanHSpaceWithProbability.setProbability(getAverageProbability(opponentsWeightsMap));

        for (Issue issue : myUtilitySpace.getDomain().getIssues()) {
            String issueName = issue.getName();
            List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();

            setCriterionMean(issueName, opponentsWeightsMap, meanHSpaceWithProbability.getUtilitySpace());
            for (ValueDiscrete value : values) {
                setFeatureMean(issueName, value, opponentsWeightsMap, meanHSpaceWithProbability.getUtilitySpace());
            }

        }


        return meanHSpaceWithProbability;
    }

    public UtilityProbabilityObject getMaxWeights(AbstractUtilitySpace myUtilitySpace, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap) throws Exception {
        UtilityProbabilityObject maxHSpaceWithProbability = new UtilityProbabilityObject(new UtilitySpaceSimple((AdditiveUtilitySpace) myUtilitySpace), 0);

        maxHSpaceWithProbability.getUtilitySpace().setWeight(getMaxWeight(opponentsWeightsMap));
        maxHSpaceWithProbability.setProbability(getAverageProbability(opponentsWeightsMap));

        for (Issue issue : myUtilitySpace.getDomain().getIssues()) {
            String issueName = issue.getName();
            List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();

            setCriterionMax(issueName, opponentsWeightsMap, maxHSpaceWithProbability.getUtilitySpace());
            for (ValueDiscrete value : values) {
                setFeatureMax(issueName, value, opponentsWeightsMap, maxHSpaceWithProbability.getUtilitySpace());
            }

        }


        return maxHSpaceWithProbability;
    }

    private double getAverageProbability(Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap) {
        double sum = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            sum += entry.getValue().getProbability();
        }

        return sum / opponentsWeightsMap.size();
    }

    private double getAverageWeight(Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap) {
        double sum = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            sum += entry.getValue().getUtilitySpace().getWeight();
        }
        double size = opponentsWeightsMap.entrySet().size();

        return sum / size;
    }

    private double getMaxWeight(Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap) {
        double max = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            if (max < entry.getValue().getUtilitySpace().getWeight())
                max = entry.getValue().getUtilitySpace().getWeight();
        }
//        double size = opponentsWeightsMap.entrySet().size();

        return max;
    }

    private void setFeatureMean(String issueName, ValueDiscrete value, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap, UtilitySpaceSimple meanHSpace) {
        double averageWeightOfFeatures = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            UtilitySpaceSimple utilitySpaceSimple = entry.getValue().getUtilitySpace();

            averageWeightOfFeatures += getAverageWeightOfFeatures(issueName, value, utilitySpaceSimple);
        }

        Map<ValueDiscrete, Double> meanFeatures = getFeaturesByName(issueName, meanHSpace);

        if (meanFeatures != null) {
            meanFeatures.put(value, averageWeightOfFeatures / opponentsWeightsMap.size());
        }

    }


    private void setFeatureMax(String issueName, ValueDiscrete value, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap, UtilitySpaceSimple meanHSpace) {
        double maxWeightOfFeatures = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            UtilitySpaceSimple utilitySpaceSimple = entry.getValue().getUtilitySpace();
            if (maxWeightOfFeatures < getMaxWeightOfFeatures(issueName, value, utilitySpaceSimple)) {
                maxWeightOfFeatures = getMaxWeightOfFeatures(issueName, value, utilitySpaceSimple);
            }
        }

        Map<ValueDiscrete, Double> meanFeatures = getFeaturesByName(issueName, meanHSpace);

        if (meanFeatures != null) {
            meanFeatures.put(value, maxWeightOfFeatures);
        }

    }

    private Map<ValueDiscrete, Double> getFeaturesByName(String issueName, UtilitySpaceSimple meanHSpace) {
        for (CriterionFeatures entry : meanHSpace.getCriterionFeatures()) {
            if (entry.getCriterion().equalsIgnoreCase(issueName)) {
                return entry.getFeatures();
            }
        }

        return null;
    }

    private double getAverageWeightOfFeatures(String issueName, ValueDiscrete value, UtilitySpaceSimple utilitySpaceSimple) {
        double sum = 0;
        double elems = 0;
        for (CriterionFeatures criterionFeatures : utilitySpaceSimple.getCriterionFeatures()) {
            if (criterionFeatures.getCriterion().equalsIgnoreCase(issueName)) {
                sum += criterionFeatures.getFeatures().get(value);
                elems++;
            }
        }

        return sum / elems;
    }

    private double getMaxWeightOfFeatures(String issueName, ValueDiscrete value, UtilitySpaceSimple utilitySpaceSimple) {
        double max = 0;
        double elems = 0;
        for (CriterionFeatures criterionFeatures : utilitySpaceSimple.getCriterionFeatures()) {
            if (criterionFeatures.getCriterion().equalsIgnoreCase(issueName)) {
                if (max < criterionFeatures.getFeatures().get(value)) {
                    max = criterionFeatures.getFeatures().get(value);
                }
                elems++;
            }
        }

        return max;
    }

    private void setCriterionMean(String issueName, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap, UtilitySpaceSimple meanHSpace) {
        double averageWeightOfCriterion = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            UtilitySpaceSimple utilitySpaceSimple = entry.getValue().getUtilitySpace();

            CriterionFeatures criterionByName = getCriterionByName(issueName, utilitySpaceSimple);
            averageWeightOfCriterion += criterionByName != null ? criterionByName.getWeight() : 0;

        }

        averageWeightOfCriterion = averageWeightOfCriterion / opponentsWeightsMap.size();

        CriterionFeatures criterion = getCriterionByName(issueName, meanHSpace);
        if (criterion != null) {
            criterion.setWeight(averageWeightOfCriterion);
        }


    }

    private void setCriterionMax(String issueName, Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap, UtilitySpaceSimple meanHSpace) {
        double maxWeightOfCriterion = 0;
        for (Map.Entry<AgentID, UtilityProbabilityObject> entry : opponentsWeightsMap.entrySet()) {
            UtilitySpaceSimple utilitySpaceSimple = entry.getValue().getUtilitySpace();

            CriterionFeatures criterionByName = getCriterionByName(issueName, utilitySpaceSimple);
//            averageWeightOfCriterion += criterionByName != null ? criterionByName.getWeight() : 0;

            if (criterionByName != null && maxWeightOfCriterion < criterionByName.getWeight()) {
                maxWeightOfCriterion = criterionByName.getWeight();
            }

        }

//        averageWeightOfCriterion = averageWeightOfCriterion / opponentsWeightsMap.size();

        CriterionFeatures criterion = getCriterionByName(issueName, meanHSpace);
        if (criterion != null) {
            criterion.setWeight(maxWeightOfCriterion);
        }


    }

    private CriterionFeatures getCriterionByName(String issueName, UtilitySpaceSimple utilitySpaceSimple) {
        for (CriterionFeatures criterionFeatures : utilitySpaceSimple.getCriterionFeatures()) {
            if (criterionFeatures.getCriterion().equalsIgnoreCase(issueName)) {
                return criterionFeatures;
            }
        }
        return null;
    }

}
