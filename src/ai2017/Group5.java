package src.ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

/**
 * This is your negotiation party.
 */
public class Group5 extends AbstractNegotiationParty {
    private Bid lastReceivedBid = null;
    private UtilitiesHelper utilitiesHelper = new UtilitiesHelper();
    private AdditiveUtilitySpace myUtilitySpace = null;
    private List<HSpaceElem> hSpace = new ArrayList<>();
    private Map<Bid, Double> myPossibleBids = new HashMap<>();
    private int step = 0;

    private Position myPreviousPosition = null;
    private Position opponentPreviousPosition = null;
    private NegotiationInfo info = null;

    @Override
    public void init(NegotiationInfo info) {

        super.init(info);
        this.info = info;
        myUtilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        step = 0;
        SpacePreparationHelper spacePreparationHelper = new SpacePreparationHelper();
        Map<String, EvaluatorDiscrete> oppUtilitySpace = prepareOpponentUtilitySpace(info.getUtilitySpace());
        hSpace = spacePreparationHelper.prepareHSpace(oppUtilitySpace);
        myPossibleBids = spacePreparationHelper.generateMyPossibleBids(myUtilitySpace);
        myPreviousPosition = new Position(utilitiesHelper.getMaxUtility(myPossibleBids), 0.0);


        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
    }

    private Map<String, EvaluatorDiscrete> prepareOpponentUtilitySpace(AbstractUtilitySpace utilitySpace) {
        Domain domain = utilitySpace.getDomain();
        Map<String, EvaluatorDiscrete> result = new HashMap<>();
        for (int i = 0; i < domain.getIssues().size(); i++) {
            String criterionName = domain.getIssues().get(i).getName();
            IssueDiscrete issueDiscrete = (IssueDiscrete) domain.getObjectives().get(i + 1);

            EvaluatorDiscrete eval = new EvaluatorDiscrete();
            try {

                for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                    eval.setEvaluationDouble(valueDiscrete, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            result.put(criterionName, eval);

        }

        return result;
    }

    public void initForTest() {
//
//        try {
////            fight();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


//    private void fight() throws IOException {
//        List<Bid> oppBids = Arrays.asList(DummyBids.generateDummyBid1(),
//                DummyBids.generateDummyBid2(),
//                DummyBids.generateDummyBid3(),
//                DummyBids.generateDummyBid4());
//
//        int steps = oppBids.size();
//
//        HSpaceElem myWeights = new HSpaceElem(myUtilitySpace);
//
//
//        for (int step = 0; step < steps; step++) {
//            Bid oppBid = oppBids.get(step);
//            recalculateHSpace(oppBid, step);
//            HSpaceElem opponentsWeights = hSpace.stream().max(Comparator.comparingDouble(HSpaceElem::getWeight)).get();
//            double oppUtility = utilitiesHelper.calculateUtility(oppBid, opponentsWeights);
//            double oppUtilityForMe = utilitiesHelper.calculateUtility(oppBid, myWeights);
//
//            opponentPreviousPosition = getOrInitOpponentPreviousPosition(oppUtility);
//            Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);
//
//            Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);
//            Vector myDesiredVector = Vector.getMirroredVector(opponentVector);
//
//            Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);
//
//
//            System.out.println("\n=============== NEXT STEP ===============\n");
//        }
//
//        System.out.println('x');
//
//    }

    private Position getOrInitOpponentPreviousPosition(double oppUtility) {
        if (opponentPreviousPosition == null) {
            opponentPreviousPosition = new Position(0.0, oppUtility);
        }
        return opponentPreviousPosition;

    }


    private void recalculateHSpace(Bid oppBid, int step) {
        Map<Integer, Double> pBHMap = utilitiesHelper.calculatePhbMap(oppBid, hSpace, step);
        double denominator = utilitiesHelper.calculateDenominator(hSpace, pBHMap);
        for (int i = 0; i < hSpace.size(); i++) {
            HSpaceElem hSpaceElem = hSpace.get(i);
            double newPhb = utilitiesHelper.calculatePhb(hSpace, i, pBHMap, denominator);
            hSpaceElem.setWeight(newPhb);
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
        try {

            recalculateHSpace(lastReceivedBid, step);
            HSpaceElem opponentsWeights = hSpace.stream().max(Comparator.comparingDouble(HSpaceElem::getWeight)).get();
            double oppUtility = utilitiesHelper.calculateUtility(lastReceivedBid, opponentsWeights);
            double oppUtilityForMe = myUtilitySpace.getUtility(lastReceivedBid);

            opponentPreviousPosition = getOrInitOpponentPreviousPosition(oppUtility);
            Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);

            Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);
            Vector myDesiredVector = Vector.getMirroredVector(opponentVector);

            Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);

            Bid myClosestBid = findClosestBid(myPossibleBids, opponentsWeights, myDesiredPosition);

            step++;


            return new Offer(getPartyId(), myClosestBid);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Offer(getPartyId(), generateRandomBid());

        // with 50% chance, counter offer
        // if we are the first party, also offer.
//        if (lastReceivedBid == null || !validActions.contains(Accept.class) || Math.random() > 0.5) {
//            return new Offer(getPartyId(), generateRandomBid());
//        } else {
//            return new Accept(getPartyId(), lastReceivedBid);
//        }
    }

    private Bid findClosestBid(Map<Bid, Double> myPossibleBids, HSpaceElem opponentsWeights, Position myDesiredPosition) throws Exception {
        double minDistance = 100000;
        Bid bestBid = null;

//        AdditiveUtilitySpace oppUtilitySpace = HSpaceElem.getAdditiveUtilitySpace(info.getUtilitySpace().getDomain(), opponentsWeights);
        for (Map.Entry<Bid, Double> myBidEntry : myPossibleBids.entrySet()) {
            Bid myBid = myBidEntry.getKey();
            double myBidForOpponent = utilitiesHelper.calculateUtility(myBid, opponentsWeights);
            double myBidForMe = myBidEntry.getValue();

            double distance = getDistance(myDesiredPosition, new Position(myBidForMe, myBidForOpponent));
            if (distance < minDistance) {
                minDistance = distance;
                bestBid = myBid;
            }
        }

        if (bestBid == null) {
            throw new Exception("Error while getting closest bid");
        }

        return bestBid;
    }

    private double getDistance(Position myDesiredPosition, Position position) {
        double x = Math.pow(myDesiredPosition.getMyUtility() - position.getMyUtility(), 2);
        double y = Math.pow(myDesiredPosition.getHisUtility() - position.getHisUtility(), 2);

        return Math.sqrt(x + y);
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
