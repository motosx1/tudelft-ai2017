package ai2017.group5;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
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
    private Map<AgentID, Bid> lastReceivedBids = new HashMap<>();
    private UtilitiesHelper utilitiesHelper = new UtilitiesHelper();
    private AdditiveUtilitySpace myUtilitySpace = null;
    private HashMap<AgentID, List<HSpaceElem>> hSpace = null;
    private Map<Bid, Double> myPossibleBids = new HashMap<>();
    private AgentID lastOpponent;
    private int step = 0;
    private SpacePreparationHelper spacePreparationHelper = new SpacePreparationHelper();
    private Map<String, EvaluatorDiscrete> oppUtilitySpace = new HashMap<>();

    private Position myPreviousPosition = null;
    private Position opponentPreviousPosition = null;
    private NegotiationInfo info = null;
    private final double BATNA_FACTOR = 0.75;


    @Override
    public void init(NegotiationInfo info) {

        super.init(info);
        this.info = info;
        myUtilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        step = 0;
        oppUtilitySpace = prepareOpponentUtilitySpace(info.getUtilitySpace());
        try {
            info.getUtilitySpace().setReservationValue(BATNA_FACTOR * myUtilitySpace.getUtility(myUtilitySpace.getMaxUtilityBid()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        myPossibleBids = spacePreparationHelper.generateMyPossibleBids(myUtilitySpace, info.getUtilitySpace().getReservationValueUndiscounted());

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
    }

    private Map<String, EvaluatorDiscrete> prepareOpponentUtilitySpace(AbstractUtilitySpace utilitySpace) {
        Domain domain = utilitySpace.getDomain();
        Map<String, EvaluatorDiscrete> result = new HashMap<>();
        for (Issue issue : domain.getIssues()) {
            String criterionName = issue.getName();
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;

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

    private Position getOrInitOpponentPreviousPosition(Position oppUtility) {
        if (opponentPreviousPosition == null) {
            opponentPreviousPosition = oppUtility;
        }
        return opponentPreviousPosition;

    }


    private void recalculateHSpace(AgentID agentId, Bid oppBid, int step) {
        if (hSpace == null || hSpace.isEmpty()) {
            hSpace = new HashMap<>();
            hSpace.put(agentId, new ArrayList<HSpaceElem>());
        }

        if (hSpace.get(agentId) == null || hSpace.get(agentId).isEmpty()) {
            hSpace.put(agentId, spacePreparationHelper.prepareHSpace(oppUtilitySpace, oppBid));
        }

        List<HSpaceElem> hSpaceForAgents = hSpace.get(agentId);

        Map<Integer, Double> pBHMap = utilitiesHelper.calculatePhbMap(oppBid, hSpaceForAgents, step);
        double denominator = utilitiesHelper.calculateDenominator(hSpaceForAgents, pBHMap);
        for (int i = 0; i < hSpaceForAgents.size(); i++) {
            HSpaceElem hSpaceElem = hSpaceForAgents.get(i);
            double newPhb = utilitiesHelper.calculatePhb(hSpaceForAgents, i, pBHMap, denominator);
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
        System.out.println("My turn start");
        Bid myMaxBid = null;
        try {
            myMaxBid = myUtilitySpace.getMaxUtilityBid();
            // if we do the first move
            if (lastReceivedBids == null || lastReceivedBids.isEmpty()) {
                return new Offer(getPartyId(), myUtilitySpace.getMaxUtilityBid());
            }

            Map<AgentID, HSpaceElem> opponentsWeightsMap = new HashMap<>();

            for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
                AgentID agentId = lastBidEntry.getKey();

                Bid lastOpponentBid = lastBidEntry.getValue();

                recalculateHSpace(agentId, lastOpponentBid, step);
                HSpaceElem opponentsWeights = getHSpaceElemWithBiggestWeight(agentId);
                opponentsWeightsMap.put(agentId, opponentsWeights);
            }


            Bid lastOpponentBid = lastReceivedBids.get(lastOpponent);
            HSpaceElem meanOpponentsWeights = utilitiesHelper.getMeanWeights(info.getUtilitySpace(), opponentsWeightsMap);

            double oppUtility = utilitiesHelper.calculateUtility(lastOpponentBid, meanOpponentsWeights);
            double oppUtilityForMe = myUtilitySpace.getUtility(lastOpponentBid);

            Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);
            opponentPreviousPosition = getOrInitOpponentPreviousPosition(opponentCurrentPosition);

            Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);

            Vector myDesiredVector = Vector.getMirroredVector(opponentVector);

            Action returnOffer;
            Bid myBid;
            if (isMyFirstBid()) {
                myBid = myUtilitySpace.getMaxUtilityBid();
                returnOffer = new Offer(getPartyId(), myBid);
            } else {
                if (shouldAccept(lastOpponentBid)) {
                    return new Accept(getPartyId(), lastReceivedBids.get(lastOpponent));
                } else {

                    Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);
                    myBid = findClosestBid(myPossibleBids, meanOpponentsWeights, myDesiredPosition);
                    returnOffer = new Offer(getPartyId(), myBid);

                }
            }


            opponentPreviousPosition = opponentCurrentPosition;
            myPreviousPosition = new Position(myUtilitySpace.getUtility(myBid), utilitiesHelper.calculateUtility(myBid, meanOpponentsWeights));

            step++;

//            cutHSpace();

            System.out.println("My turn end");
            return returnOffer;


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (myMaxBid != null) {
            return new Offer(getPartyId(), myMaxBid);
        } else {
            return new Offer(getPartyId(), generateRandomBid());
        }


    }

    private HSpaceElem getHSpaceElemWithBiggestWeight(AgentID agentId) {
        double max = 0;
        HSpaceElem maxHSpaceElem = null;

        for (HSpaceElem hSpaceElem : hSpace.get(agentId)) {
            if (hSpaceElem.getWeight() > max) {
                max = hSpaceElem.getWeight();
                maxHSpaceElem = hSpaceElem;
            }
        }

        return maxHSpaceElem;
    }

//    private void cutHSpace() {
//        hSpace.entrySet().forEach(entry -> {
//            int size = entry.getValue().size();
//            int cutLimit = size <= 20 ? size : size / 2;
//            List<HSpaceElem> newList = entry.getValue().stream()
//                    .sorted(Comparator.comparingDouble(HSpaceElem::getWeight))
//                    .limit(cutLimit)
//                    .collect(Collectors.toList());
//
//            entry.setValue(newList);
//        });
//    }


    private boolean shouldAccept(Bid lastOpponentBid) {
        return myUtilitySpace.getUtility(lastOpponentBid) >= myPreviousPosition.getMyUtility();
    }

    private boolean isMyFirstBid() {
        return myPreviousPosition == null;
    }

    private Bid findClosestBid(Map<Bid, Double> myPossibleBids, HSpaceElem opponentsWeights, Position myDesiredPosition) throws Exception {
        double minDistance = 100000;
        Bid bestBid = null;

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
            lastReceivedBids.put(sender, ((Offer) action).getBid());
            lastOpponent = sender;
        }
    }

    @Override
    public String getDescription() {
        return "example party group 5";
    }

}
