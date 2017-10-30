package src.ai2017;

import negotiator.Bid;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bartosz on 17.10.2017.
 */
class SpacePreparationHelper {
    private double a = 0.5;//0.2 + Math.sin(Math.PI / 6);

    List<HSpaceElem> prepareHSpace(Map<String, EvaluatorDiscrete> utilitySpace, Bid bestOppBid) {

        List<HSpaceElem> hSpace = new ArrayList<>();

        Map<String, List<Map<ValueDiscrete, Double>>> featuresPermutationsMap = new HashMap<>();
        for (Map.Entry<String, EvaluatorDiscrete> entry : utilitySpace.entrySet()) {
            Set<ValueDiscrete> features = new HashSet<>(entry.getValue().getValues());

            List<Value> bidValues = bestOppBid.getValues().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());

            ValueDiscrete bestOpponentValue = (ValueDiscrete) getBestOppValue(features, bidValues);
            //features list without best option from bid;
            features.removeAll(bidValues);


            List<List<ValueDiscrete>> featuresPermutations = SetPermutations.getSetPermutations(new ArrayList<>(features));


            for (List<ValueDiscrete> featuresPermutation : featuresPermutations) {
                featuresPermutation.add(0, bestOpponentValue);
            }

            List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = assignWeightsToFeatures(featuresPermutations);

            featuresPermutationsMap.put(entry.getKey(), featuresPermutationsWithWeights);
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


//        cleanHSpace = new ArrayList<>(hSpace);
        return hSpace;
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

    Map<Bid, Double> generateMyPossibleBids(AdditiveUtilitySpace utilitySpace, double reservationValueUndiscounted) {
        List<List<ValueDiscreteDouble>> possibleCombinations = generatePossibleCombinations(utilitySpace);

        return generateBids(utilitySpace, possibleCombinations, reservationValueUndiscounted);
    }

    private Map<Bid, Double> generateBids(AdditiveUtilitySpace utilitySpace, List<List<ValueDiscreteDouble>> possibleCombinations, double reservationValueUndiscounted) {
        Map<Bid, Double> result = new HashMap<>();

        for (List<ValueDiscreteDouble> combination : possibleCombinations) {
            HashMap<Integer, Value> bidEntries = new HashMap<>();

            for (ValueDiscreteDouble entry : combination) {
                Objective criterion = entry.criterion;
                int index = utilitySpace.getDomain().getObjectives().indexOf(criterion);

                bidEntries.put(index, entry.valueDiscrete);
            }

            Bid bid = new Bid(utilitySpace.getDomain(), bidEntries);
            if (utilitySpace.getUtility(bid) > reservationValueUndiscounted) {
                result.put(bid, utilitySpace.getUtility(bid));
            }
        }

        return result;
    }

    private List<List<ValueDiscreteDouble>> generatePossibleCombinations(AdditiveUtilitySpace utilitySpace) {
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

        return CartesianProduct.calculate(featuresList);


    }

    class ValueDiscreteDouble {
        Objective criterion;
        ValueDiscrete valueDiscrete;
        Double weight;

        public ValueDiscreteDouble(Objective criterion, ValueDiscrete valueDiscrete, Double weight) {
            this.criterion = criterion;
            this.valueDiscrete = valueDiscrete;
            this.weight = weight;
        }
    }
}
