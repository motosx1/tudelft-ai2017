package src.ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DomainImpl;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.EvaluatorDiscrete;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This is your negotiation party.
 */
public class Group5 extends AbstractNegotiationParty {
    private Random rand = new Random(200);
    private Bid lastReceivedBid = null;
    private int step = 1;

    private Map<String, EvaluatorDiscrete> dummyUtilitySpace = new HashMap<>(); // == fevaluators

    private List<List<CriterionFeatures>> hSpace = new ArrayList<>();

    double a = 0.5;//0.2 + Math.sin(Math.PI / 6);

    @Override
    public void init(NegotiationInfo info) {

        super.init(info);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
    }

    public void initForTest() {

        prepareDummy();

        prepareHSpace();


        System.out.println("end");

        try {
            fight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareHSpace() {
        Map<String, List<Map<ValueDiscrete, Double>>> featuresPermutationsMap = new HashMap<>();
        for (Map.Entry<String, EvaluatorDiscrete> entry : dummyUtilitySpace.entrySet()) {
            Set<ValueDiscrete> features = entry.getValue().getValues();
            List<List<ValueDiscrete>> featuresPermutations = SetPermutations.getSetPermutations(new ArrayList<>(features));
            List<Map<ValueDiscrete, Double>> featuresPermutationsWithWeights = new ArrayList<>();

            // assign weights
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


            featuresPermutationsMap.put(entry.getKey(), featuresPermutationsWithWeights);

        }

//        Set<Map.Entry<String, List<Map<ValueDiscrete, Double>>>> featuresPermutationsMapEntries = featuresPermutationsMap.entrySet();
//        List<List<Map.Entry<String, List<Map<ValueDiscrete, Double>>>>> setPermutations = SetPermutations.getSetPermutations(new ArrayList<>(featuresPermutationsMapEntries));

        System.out.println(featuresPermutationsMap);


        List<List<CriterionFeatures>> criterionFeaturesList = new ArrayList<>();

        for (Map.Entry<String, List<Map<ValueDiscrete, Double>>> entry1 : featuresPermutationsMap.entrySet()) {
            List<CriterionFeatures> criterionFeaturesList1 = new ArrayList<>();
            for (Map<ValueDiscrete, Double> entry2 : entry1.getValue()) {
                System.out.println(entry1.getKey() + " -> " + entry2.entrySet());
                CriterionFeatures criterionFeatures = new CriterionFeatures(entry1.getKey(), entry2);
                criterionFeaturesList1.add(criterionFeatures);
            }
            criterionFeaturesList.add(criterionFeaturesList1);
        }


        List<List<CriterionFeatures>> cartesianProduct = CartesianProduct.calculate(criterionFeaturesList);

//        System.out.println(cartesianProduct);

        List<CriterionFeaturesWeight> criterionFeaturesWeightList = new ArrayList<>();
        for (List<CriterionFeatures> criterionFeatures : cartesianProduct) {
            criterionFeaturesWeightList.add(new CriterionFeaturesWeight(criterionFeatures));
        }


        for (CriterionFeaturesWeight criterionFeaturesWeight : criterionFeaturesWeightList) {
            List<List<CriterionFeatures>> criterionPermutations = SetPermutations.getSetPermutations(criterionFeaturesWeight.getCriterionFeatures());
            criterionPermutations = CriterionFeatures.fixCriterionFeaturesPermutations(criterionPermutations);


            for (List<CriterionFeatures> criterionPermutation : criterionPermutations) {
                int n = criterionPermutation.size();
                double sn = (1 - Math.pow(a, n)) / (1 - a);
                double cwn = 1 / sn;
                for (CriterionFeatures criterionFeatures : criterionPermutation) {
                    criterionFeatures.setWeight(cwn);
                    cwn = cwn * a;
                }
                hSpace.add(criterionPermutation);

            }


        }

//        assignProbabilitiesToHSpace();
    }

    private void assignProbabilitiesToHSpace() {

    }

    private void fight() throws IOException {
        Bid oppBid = generateDummyBid();

//        System.out.println(Arrays.toString(hSpace.entrySet().toArray()));


//        for (Map.Entry<Map<Map.Entry<String, List<Map<ValueDiscrete, Double>>>, Double>, Double> hSpaceEntry : hSpace.entrySet()) {
//            System.out.println(hSpaceEntry.getKey());
//            for (Map.Entry<Map.Entry<String, List<Map<ValueDiscrete, Double>>>, Double> entry1 : hSpaceEntry.getKey().entrySet()) {
//                System.out.println("\t" + entry1);
//                for (Map<ValueDiscrete, Double> entry2 : entry1.getKey().getValue()) {
//                    System.out.println("\t\t" + entry2);
//
//                    for (Map.Entry<ValueDiscrete, Double> entry3 : entry2.entrySet()) {
////                        System.out.println("->" + hSpaceEntry + "(" + entry1.getValue() + "){" + entry3.getKey() + "(" + entry3.getValue() + ")" + "}");
//                    }
//
//
//                }
//            }
//        }

//        System.out.println("end");

//        for (Map.Entry<Map<Map.Entry<String, List<Map<ValueDiscrete, Double>>>, Double>, Double> hSpaceEntry : hSpace.entrySet()) {
//
//            double newPhb = calculatePhb(oppBid, hSpaceEntry);
//        }

    }

    private double calculatePhb(Bid oppBid, Map.Entry<Map<Map.Entry<String, List<Map<ValueDiscrete, Double>>>, Double>, Double> hSpaceEntry) {
        Map<Map.Entry<String, List<Map<ValueDiscrete, Double>>>, Double> hj = hSpaceEntry.getKey();
        Double phj = hSpaceEntry.getValue();

        double numerator = phj * calculatePbh();
        double denominator = 1;

        return numerator / denominator;
    }

    private double calculatePbh() {
        double sigma = 0.4;
        double c = 1 / (sigma * Math.sqrt(2 * Math.PI));

        double index = Math.pow(utilityBgivenH() - utilityB(), 2) / (2 * Math.pow(sigma, 2));

        return c * Math.exp(index);
    }

    private double utilityBgivenH() {
        return 1;
    }

    private double utilityB() {
        return 1 - 0.05 * step++;  //TODO change to consider time horizon
    }


    private Bid generateDummyBid() throws IOException {
        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
        HashMap values = new HashMap();

        for (Issue issue : domain.getIssues()) {
            IssueDiscrete discreteIssue = (IssueDiscrete) issue;
            int index = 0;//rand.nextInt(discreteIssue.getNumberOfValues());
            ValueDiscrete value = discreteIssue.getValue(index);
            values.put(issue.getNumber(), value);

        }

        return new Bid(domain, values);
    }

    private void prepareDummy() {
        try {
            EvaluatorDiscrete eval = new EvaluatorDiscrete();
            eval.setWeight(0.5);
            eval.setEvaluationDouble(new ValueDiscrete("Plain"), 0.6);
            eval.setEvaluationDouble(new ValueDiscrete("Photo"), 0.4);
            dummyUtilitySpace.put("Invitations", eval);


            eval = new EvaluatorDiscrete();
            eval.setWeight(0.1);
            eval.setEvaluationDouble(new ValueDiscrete("Party Room"), 0.8);
            eval.setEvaluationDouble(new ValueDiscrete("Your Dorm"), 0.2);
            dummyUtilitySpace.put("Location", eval);

//            eval = new EvaluatorDiscrete();
//            eval.setWeight(0.4);
//            eval.setEvaluationDouble(new ValueDiscrete("Beer"), 0.8);
//            eval.setEvaluationDouble(new ValueDiscrete("Wine"), 0.2);
//            dummyUtilitySpace.put("Drinks", eval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Each round this method gets called and ask you to accept or offer. The
     * first party in the first round is a bit different, it can only propose an
     * offer.
     *
     * @param validActions Either a list containing both accept and offer or only offer.
     * @return The chosen action.
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {

        // with 50% chance, counter offer
        // if we are the first party, also offer.
        if (lastReceivedBid == null || !validActions.contains(Accept.class) || Math.random() > 0.1) {
            return new Offer(getPartyId(), generateRandomBid());
        } else {
            return new Accept(getPartyId(), lastReceivedBid);
        }
    }

    /**
     * All offers proposed by the other parties will be received as a message.
     * You can use this information to your advantage, for example to predict
     * their utility.
     *
     * @param sender The party that did the action. Can be null.
     * @param action The action that party did.
     */
    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        if (action instanceof Offer) {
            lastReceivedBid = ((Offer) action).getBid();
        }
    }

    @Override
    public String getDescription() {
        return "example party group 5";
    }

}
