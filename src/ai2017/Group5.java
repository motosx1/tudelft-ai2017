package src.ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is your negotiation party.
 */
public class Group5 extends AbstractNegotiationParty {

    private Bid lastReceivedBid = null;

    private Map<String, EvaluatorDiscrete> dummyUtilitySpace = new HashMap<>(); // == fevaluators

    private Map<Map<Map.Entry<String, EvaluatorDiscrete>, Double>, Double> hSpace = new HashMap<>();
    ;

    @Override
    public void init(NegotiationInfo info) {

        super.init(info);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
    }

    public void initForTest() {

        prepareDummy();

        List<Map.Entry<String, EvaluatorDiscrete>> utilitySpaceList = new ArrayList<>(dummyUtilitySpace.entrySet());

        double a = 0.5;

        List<List<Map.Entry<String, EvaluatorDiscrete>>> setPermutations = SetPermutations.getSetPermutations(utilitySpaceList);

        for (List<Map.Entry<String, EvaluatorDiscrete>> setPermutation : setPermutations) {
//            System.out.println(setPermutation);
            Map<Map.Entry<String, EvaluatorDiscrete>, Double> hElem = new HashMap<>();
            int n = setPermutation.size();
            double sn = (1 - Math.pow(a, n)) / (1 - a);
            double cwn = 1 / sn;
            for (Map.Entry<String, EvaluatorDiscrete> entry1 : setPermutation) {
                hElem.put(entry1, cwn);
//                System.out.println(entry1 + " " + entry1.getValue().getWeight() + " | " + "w = " + cwn);
                cwn = cwn * a;
            }
            hSpace.put(hElem, 1 / (double) setPermutations.size());
            System.out.println("-----------");
        }

        System.out.println("end");

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
            eval.setEvaluationDouble(new ValueDiscrete("Your dorm"), 0.2);
            dummyUtilitySpace.put("Location", eval);

            eval = new EvaluatorDiscrete();
            eval.setWeight(0.4);
            eval.setEvaluationDouble(new ValueDiscrete("Beer"), 0.8);
            eval.setEvaluationDouble(new ValueDiscrete("Wine"), 0.2);
            dummyUtilitySpace.put("Drinks", eval);
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
        if (lastReceivedBid == null || !validActions.contains(Accept.class) || Math.random() > 0.5) {
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
