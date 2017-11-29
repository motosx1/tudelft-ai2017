package ai2017.group5.helpers.hspace;

import ai2017.group5.CriterionFeatures;
import ai2017.group5.CriterionFeaturesWeight;
import ai2017.group5.UtilitySpaceSimple;
import ai2017.group5.dao.UtilityProbabilityObject;
import ai2017.group5.helpers.math.CartesianProduct;
import ai2017.group5.helpers.math.SetPermutations;
import ai2017.group5.helpers.math.UtilitiesHelper;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

public class OpponentSpace {
    private final UtilitiesHelper utilitiesHelper = new UtilitiesHelper();
    private final Map<AgentID, List<UtilitySpaceSimple>> opponentUtilitySpaceMap = new HashMap<>();
    private final Map<AgentID, Integer> opponentRecalculateTimesNumber = new HashMap<>();
    private final Domain domain;
    private final double a = 0.5; //0.2 + Math.sin(Math.PI / 6);
    private final int SNIFFING_MAX_NUMBER;
    private final int SNIFFING_MAX_NEGO_TIME;


    public OpponentSpace(Domain domain, double totalTime) {
        this.domain = domain;

        this.SNIFFING_MAX_NEGO_TIME = (int)(0.25 * totalTime);
        this.SNIFFING_MAX_NUMBER = 5;
    }

    /**
     * check if in the past the bid was given, if not
     * create hSpace based on this bid
     * combine with existing hSpace,
     * recalculate the hSpace from the beginning
     *
     * @param agentId             agentId
     * @param lastOpponentBidsMap lastOpponentBidsMap
     * @param timeline            timeline
     */
    public void updateHSpace(AgentID agentId, Map<Double, Bid> lastOpponentBidsMap, TimeLineInfo timeline) {
        if (opponentRecalculateTimesNumber.get(agentId) == null) {
            opponentRecalculateTimesNumber.put(agentId, 0);
        }

        List<Bid> lastOpponentBidsList = new ArrayList<>(lastOpponentBidsMap.values());
        Bid newestBid = lastOpponentBidsList.get(lastOpponentBidsList.size() - 1);
        List<Bid> oldBids = lastOpponentBidsList.subList(0, lastOpponentBidsList.size() - 1);

        initializeUtilitySpace(agentId, newestBid);

        if (true || wasAlreadyPlaced(newestBid, oldBids) || lastOpponentBidsList.size() == 1 || !isSniffingTime(agentId, timeline)) {
            List<UtilitySpaceSimple> plausibleExistingUtilitySpacesForTheAgent = opponentUtilitySpaceMap.get(agentId);
            updateIssueWeights(newestBid, timeline.getCurrentTime(), timeline.getTotalTime(), plausibleExistingUtilitySpacesForTheAgent);
        } else {

            // increment number of newly placed bids
            opponentRecalculateTimesNumber.put(agentId, opponentRecalculateTimesNumber.get(agentId) + 1);

            //get old utility space
            List<UtilitySpaceSimple> plausibleExistingUtilitySpacesForTheAgent = opponentUtilitySpaceMap.get(agentId);

            //create new utility space based on the newest bid
            List<UtilitySpaceSimple> newHSpaceExistingUtilitySpacesForTheAgent = prepareHSpace(newestBid);

            //combine old and new space
            List<UtilitySpaceSimple> combinedUtilitySpace = combine(plausibleExistingUtilitySpacesForTheAgent, newHSpaceExistingUtilitySpacesForTheAgent);
            opponentUtilitySpaceMap.put(agentId, combinedUtilitySpace);

            recalculateWeights(lastOpponentBidsMap, opponentUtilitySpaceMap.get(agentId), timeline.getTotalTime());

        }

    }

    private boolean isSniffingTime(AgentID agentId, TimeLineInfo timeline) {
        return opponentRecalculateTimesNumber.get(agentId) < SNIFFING_MAX_NUMBER && timeline.getCurrentTime() < SNIFFING_MAX_NEGO_TIME;
    }


    private List<UtilitySpaceSimple> combine(List<UtilitySpaceSimple> oldSpace, List<UtilitySpaceSimple> newSpace) {
        //clean weights
        for (UtilitySpaceSimple utilitySpaceSimple : oldSpace) {
            utilitySpaceSimple.setWeight(0.0);
        }

        List<UtilitySpaceSimple> resultList = new ArrayList<>(oldSpace);

        // if entry from new utility space is not in old utility space, then add
        for (UtilitySpaceSimple newUtilitySpaceEntry : newSpace) {
            newUtilitySpaceEntry.setWeight(0.0);

            if (oldSpace.indexOf(newUtilitySpaceEntry) == -1) {
                resultList.add(newUtilitySpaceEntry);
            }
        }

        return oldSpace;
    }

    private boolean wasAlreadyPlaced(Bid newestBid, List<Bid> oldBids) {
        return oldBids.indexOf(newestBid) != -1;
    }

    private void initializeUtilitySpace(AgentID agentId, Bid oppBid) {
        if (opponentUtilitySpaceMap.isEmpty()) {
            opponentUtilitySpaceMap.put(agentId, new ArrayList<UtilitySpaceSimple>());
        }

        if (opponentUtilitySpaceMap.get(agentId) == null || opponentUtilitySpaceMap.get(agentId).isEmpty()) {
            opponentUtilitySpaceMap.put(agentId, prepareHSpace(oppBid));
        }
    }

    /**
     * updates probabilities of given utility spaces after every received bid
     * @param oppBid                            new opponent bid
     * @param step                              current step
     * @param totalTime
     * @param plausibleUtilitySpacesForTheAgent plausible Utility Spaces For The Agent
     */
    private void updateIssueWeights(Bid oppBid, double step, double totalTime, List<UtilitySpaceSimple> plausibleUtilitySpacesForTheAgent) {
        Map<Integer, Double> pBHMap = utilitiesHelper.calculatePhbMap(oppBid, plausibleUtilitySpacesForTheAgent, step, totalTime);
        double denominator = utilitiesHelper.calculateDenominator(plausibleUtilitySpacesForTheAgent, pBHMap);
        for (int i = 0; i < plausibleUtilitySpacesForTheAgent.size(); i++) {
            UtilitySpaceSimple utilitySpaceSimple = plausibleUtilitySpacesForTheAgent.get(i);
            double newPhb = utilitiesHelper.calculatePhb(plausibleUtilitySpacesForTheAgent, i, pBHMap, denominator);
            utilitySpaceSimple.setWeight(newPhb);
        }
    }

    /**
     * Recalculate bid from scratch
     * @param bidsHistory bids with the timestamp
     * @param plausibleUtilitySpacesForTheAgent clean utility space to be updated
     * @param totalTime
     */
    private void recalculateWeights(Map<Double, Bid> bidsHistory, List<UtilitySpaceSimple> plausibleUtilitySpacesForTheAgent, double totalTime) {

        for (UtilitySpaceSimple utilitySpaceSimple : plausibleUtilitySpacesForTheAgent) {
            utilitySpaceSimple.setWeight(1 / (double) plausibleUtilitySpacesForTheAgent.size());
        }

        TreeMap<Double, Bid> bidsHistorySorted = new TreeMap<>(bidsHistory);

        for (Map.Entry<Double, Bid> entry : bidsHistorySorted.entrySet()) {
            Map<Integer, Double> pBHMap = utilitiesHelper.calculatePhbMap(entry.getValue(), plausibleUtilitySpacesForTheAgent, entry.getKey(), totalTime);
            double denominator = utilitiesHelper.calculateDenominator(plausibleUtilitySpacesForTheAgent, pBHMap);
            for (int i = 0; i < plausibleUtilitySpacesForTheAgent.size(); i++) {
                UtilitySpaceSimple utilitySpaceSimple = plausibleUtilitySpacesForTheAgent.get(i);
                double newPhb = utilitiesHelper.calculatePhb(plausibleUtilitySpacesForTheAgent, i, pBHMap, denominator);
                utilitySpaceSimple.setWeight(newPhb);
            }
        }


    }

    private List<UtilitySpaceSimple> prepareHSpace(Bid bestOppBid) {

        List<UtilitySpaceSimple> hSpace = new ArrayList<>();

        Map<String, List<Map<ValueDiscrete, Double>>> featuresPermutationsMap = new HashMap<>();
        Map<Issue, EvaluatorDiscrete> opponentsUtilitySpaceDomain = prepareOpponentUtilitySpace(this.domain);
        for (Map.Entry<Issue, EvaluatorDiscrete> entry : opponentsUtilitySpaceDomain.entrySet()) {
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
        assignWeightsToCriteria(bestOppBid, hSpace, criterionFeaturesWeightList);
        assignProbabilitiesToHSpace(hSpace);


        return hSpace;
    }

    private void assignWeightsToCriteria(Bid bestOppBid, List<UtilitySpaceSimple> hSpace, List<CriterionFeaturesWeight> criterionFeaturesWeightList) {
        for (CriterionFeaturesWeight criterionFeaturesWeight : criterionFeaturesWeightList) {
            criterionFeaturesWeight.sortByOtherBid(bestOppBid);
            assignWeightsValuesToCriteria(criterionFeaturesWeight.getCriterionFeatures(), hSpace);
        }
    }

    private void assignWeightsValuesToCriteria(List<CriterionFeatures> criterionPermutation, List<UtilitySpaceSimple> hSpace) {
        int n = criterionPermutation.size();
        double sn = (1 - Math.pow(a, n)) / (1 - a);
        double cwn = 1 / sn;
        for (CriterionFeatures criterionFeatures : criterionPermutation) {
            criterionFeatures.setWeight(cwn);
            cwn = cwn * a;
        }
        hSpace.add(new UtilitySpaceSimple(criterionPermutation));

    }


    private Map<Issue, EvaluatorDiscrete> prepareOpponentUtilitySpace(Domain domain) {
        Map<Issue, EvaluatorDiscrete> result = new HashMap<>();
        for (Issue issue : domain.getIssues()) {
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete eval = new EvaluatorDiscrete();
            try {
                for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                    eval.setEvaluationDouble(valueDiscrete, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            result.put(issue, eval);
        }

        return result;
    }

    private int getIssueNumberByKey(Bid bestOppBid, String featuresKey) {

        for (Issue issue : bestOppBid.getIssues()) {
            if (issue.getName().equalsIgnoreCase(featuresKey)) {
                return issue.getNumber();
            }
        }

        return -1;
    }


    public Value getBestOppValue(Set<ValueDiscrete> features, List<Value> bidValues) {
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

    private void assignProbabilitiesToHSpace(List<UtilitySpaceSimple> hSpace) {
        for (UtilitySpaceSimple utilitySpaceSimple : hSpace) {
            utilitySpaceSimple.setWeight(1 / (double) hSpace.size());
        }
    }

    public UtilityProbabilityObject getHSpaceElementWithBiggestWeight(AgentID agentId) {
        double max = 0;
        UtilitySpaceSimple maxUtilitySpaceSimple = null;

        for (UtilitySpaceSimple utilitySpaceSimple : opponentUtilitySpaceMap.get(agentId)) {
            if (utilitySpaceSimple.getWeight() > max) {
                max = utilitySpaceSimple.getWeight();
                maxUtilitySpaceSimple = utilitySpaceSimple;
            }
        }

        return new UtilityProbabilityObject(maxUtilitySpaceSimple, max);
    }

    public Map<AgentID, List<UtilitySpaceSimple>> getOpponentUtilitySpaceMap() {
        return opponentUtilitySpaceMap;
    }
}
