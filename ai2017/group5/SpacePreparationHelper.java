package ai2017.group5;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

class SpacePreparationHelper {
    private double a = 0.5;//0.2 + Math.sin(Math.PI / 6);

    List<HSpaceElem> prepareHSpace(Map<Issue, EvaluatorDiscrete> utilitySpace, Bid bestOppBid) {

        List<HSpaceElem> hSpace = new ArrayList<>();

        Map<String, List<Map<ValueDiscrete, Double>>> featuresPermutationsMap = new HashMap<>();
        for (Map.Entry<Issue, EvaluatorDiscrete> entry : utilitySpace.entrySet()) {
            Set<ValueDiscrete> features = new HashSet<>(entry.getValue().getValues());
            Issue featuresKey = entry.getKey();

            int issueNumber = getIssueNumberByKey(bestOppBid, featuresKey.getName());
            Value value = bestOppBid.getValues().get(issueNumber);

            //features list without best option from bid;
            features.remove(value);

            List<List<ValueDiscrete>> featuresPermutations = SetPermutations.getSetPermutations(new ArrayList<>(features));


            for (List<ValueDiscrete> featuresPermutation : featuresPermutations) {
                featuresPermutation.add(0, (ValueDiscrete) value);
            }

            List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = assignWeightsToFeatures(featuresPermutations);

            featuresPermutationsMap.put(entry.getKey().getName(), featuresPermutationsWithWeights);
        }

        List<List<CriterionFeatures>> criterionFeaturesList = new ArrayList<>();

        for (Map.Entry<String, List<Map<ValueDiscrete, Double>>> entry1 : featuresPermutationsMap.entrySet()) {
            List<CriterionFeatures> criterionFeaturesList1 = new ArrayList<>();
            for (Map<ValueDiscrete, Double> entry2 : entry1.getValue()) {
                CriterionFeatures criterionFeatures = new CriterionFeatures(entry1.getKey(), entry2);
                criterionFeaturesList1.add(criterionFeatures);
            }
            criterionFeaturesList.add(criterionFeaturesList1);
        }


        List<List<CriterionFeatures>> cartesianProduct = CartesianProduct.calculate(criterionFeaturesList);

        List<CriterionFeaturesWeight> criterionFeaturesWeightList = new ArrayList<>();
        for (List<CriterionFeatures> criterionFeatures : cartesianProduct) {
            criterionFeaturesWeightList.add(new CriterionFeaturesWeight(criterionFeatures));
        }

        // assign weights to criteria
        for (CriterionFeaturesWeight criterionFeaturesWeight : criterionFeaturesWeightList) {
            criterionFeaturesWeight.sortByOtherBid(bestOppBid);
            assignWeightsToCriteria(criterionFeaturesWeight.getCriterionFeatures(), hSpace);
        }

        assignProbabilitiesToHSpace(hSpace);


        return hSpace;
    }

    private int getIssueNumberByKey(Bid bestOppBid, String featuresKey) {

        for (Issue issue : bestOppBid.getIssues()) {
            if (issue.getName().equalsIgnoreCase(featuresKey)) {
                return issue.getNumber();
            }
        }

        return -1;
    }


    private Value getBestOppValue(Set<ValueDiscrete> features, List<Value> bidValues) {
        List<ValueDiscrete> allFeatures = new ArrayList<>(features);
        allFeatures.retainAll(bidValues);
        return allFeatures.get(0);
    }

    private List<Map<ValueDiscrete, Double>> assignWeightsToFeatures(List<List<ValueDiscrete>> featuresPermutations) {
        List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = new ArrayList<>();

        // assign weights to features
        for (List<ValueDiscrete> permutation : featuresPermutations) {
            Map<ValueDiscrete, Double> featureWeightMap = new HashMap<>();
            int n = permutation.size();
            double sn = (1 - Math.pow(a, n)) / (1 - a);
            double cwn = 1 / sn;

            for (ValueDiscrete feature : permutation) {
                featureWeightMap.put(feature, cwn);
                cwn = cwn * a;
            }

            featuresPermutationsWithWeights.add(featureWeightMap);
        }
        return featuresPermutationsWithWeights;
    }


    private void assignWeightsToCriteria(List<CriterionFeatures> criterionPermutation, List<HSpaceElem> hSpace) {
        int n = criterionPermutation.size();
        double sn = (1 - Math.pow(a, n)) / (1 - a);
        double cwn = 1 / sn;
        for (CriterionFeatures criterionFeatures : criterionPermutation) {
            criterionFeatures.setWeight(cwn);
            cwn = cwn * a;
        }
        hSpace.add(new HSpaceElem(criterionPermutation));

    }

    private void assignProbabilitiesToHSpace(List<HSpaceElem> hSpace) {
        for (HSpaceElem hSpaceElem : hSpace) {
            hSpaceElem.setWeight(1 / (double) hSpace.size());
        }
    }

}
