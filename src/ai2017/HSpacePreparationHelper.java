package src.ai2017;

import negotiator.issue.ValueDiscrete;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

/**
 * Created by bartosz on 17.10.2017.
 */
public class HSpacePreparationHelper {
    Map<String, EvaluatorDiscrete> utilitySpace;
    double a = 0.5;//0.2 + Math.sin(Math.PI / 6);


    public HSpacePreparationHelper(Map<String, EvaluatorDiscrete> utilitySpace) {
        this.utilitySpace = utilitySpace;
    }

    public List<HSpaceElem> prepareHSpace() {
        List<HSpaceElem> hSpace = new ArrayList<>();



        Map<String, List<Map<ValueDiscrete, Double>>> featuresPermutationsMap = new HashMap<>();
        for (Map.Entry<String, EvaluatorDiscrete> entry : this.utilitySpace.entrySet()) {
            Set<ValueDiscrete> features = entry.getValue().getValues();
            List<List<ValueDiscrete>> featuresPermutations = SetPermutations.getSetPermutations(new ArrayList<>(features));

            List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = assignWeightsToFeatures(featuresPermutations);

            featuresPermutationsMap.put(entry.getKey(), featuresPermutationsWithWeights);
        }


//        System.out.println(featuresPermutationsMap);


        List<List<CriterionFeatures>> criterionFeaturesList = new ArrayList<>();

        for (Map.Entry<String, List<Map<ValueDiscrete, Double>>> entry1 : featuresPermutationsMap.entrySet()) {
            List<CriterionFeatures> criterionFeaturesList1 = new ArrayList<>();
            for (Map<ValueDiscrete, Double> entry2 : entry1.getValue()) {
//                System.out.println(entry1.getKey() + " -> " + entry2.entrySet());
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
            List<List<CriterionFeatures>> criterionPermutations = SetPermutations.getSetPermutations(criterionFeaturesWeight.getCriterionFeatures());
            criterionPermutations = CriterionFeatures.fixCriterionFeaturesPermutations(criterionPermutations);
            assignWeightsToCriteria(criterionPermutations, hSpace);
        }

        assignProbabilitiesToHSpace(hSpace);

        System.out.println("------- hSpace ------");
        System.out.println(hSpace);
        System.out.println("--------------------");

        return hSpace;
    }

    private List<Map<ValueDiscrete, Double>> assignWeightsToFeatures(List<List<ValueDiscrete>> featuresPermutations) {
        List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = new ArrayList<>();

        // assign weights to features
        for (List<ValueDiscrete> permutation : featuresPermutations) {
            Map<ValueDiscrete, Double> featureWeightMap = new HashMap<>();
            int n = featuresPermutations.size();
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


    private void assignWeightsToCriteria(List<List<CriterionFeatures>> criterionPermutations, List<HSpaceElem> hSpace) {
        for (List<CriterionFeatures> criterionPermutation : criterionPermutations) {
            int n = criterionPermutation.size();
            double sn = (1 - Math.pow(a, n)) / (1 - a);
            double cwn = 1 / sn;
            for (CriterionFeatures criterionFeatures : criterionPermutation) {
                criterionFeatures.setWeight(cwn);
                cwn = cwn * a;
            }
            hSpace.add(new HSpaceElem(criterionPermutation));

        }
    }

    private void assignProbabilitiesToHSpace(List<HSpaceElem> hSpace) {
        for (HSpaceElem hSpaceElem : hSpace) {
            hSpaceElem.setWeight(1/(double)hSpace.size());
        }
    }

}
