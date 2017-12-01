package ai2017.group5;

import ai2017.group5.dao.Position;
import ai2017.group5.dao.UtilityProbabilityObject;
import ai2017.group5.dao.Vector;
import ai2017.group5.helpers.BidHistory;
import ai2017.group5.helpers.MoveType;
import ai2017.group5.helpers.MyNegotiationInfoEnhanced;
import ai2017.group5.helpers.RandomBidHelper;
import ai2017.group5.helpers.hspace.OpponentSpace;
import ai2017.group5.helpers.math.UtilitiesHelper;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.NegotiationInfo;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Strategy {
    private final NegotiationInfo info;
    private final AgentID myPartyId;
    private final MyNegotiationInfoEnhanced myNegotiationInfo;
    private final AdditiveUtilitySpace myUtilitySpace;
    private final OpponentSpace opponentSpace;
    private final UtilitiesHelper utilitiesHelper = new UtilitiesHelper();
    private Position opponentPreviousPosition = null;
    private Position myPreviousPosition = null;
    private Map<Bid, Bid> bidResponseMap = new HashMap<>();

    Strategy(NegotiationInfo info, MyNegotiationInfoEnhanced myNegotiationInfo, double totalTime) {
        this.info = info;
        this.myUtilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        this.myPartyId = info.getAgentID();
        this.myNegotiationInfo = myNegotiationInfo;
        this.opponentSpace = new OpponentSpace(info.getUtilitySpace().getDomain(), totalTime);
    }


    Action chooseAction(Map<AgentID, Bid> lastReceivedBids, BidHistory bidHistory, TimeLineInfo timeline, int myStepCounter, AgentID lastOpponent) throws Exception {
        updateOpponentUtilitySpace(bidHistory, myStepCounter);
        if (myStepCounter <= 5) {
            Action returnOffer = new Offer(myPartyId, RandomBidHelper.getRandomBid(info));
            return returnOffer;
        } else if (myStepCounter <= 15) {
            Action returnOffer = new Offer(myPartyId, myUtilitySpace.getMaxUtilityBid());
            return returnOffer;
        }

        Action returnOffer1 = respontToAlreadyGivenBid(lastReceivedBids);
        if (returnOffer1 != null) return returnOffer1;

        // create average opponent utility space
        UtilityProbabilityObject averageOpponentUtilitySpace = getMaxOpponentUtilitySpace(lastReceivedBids);

        if (averageOpponentUtilitySpace == null) {
            Action returnOffer = new Offer(myPartyId, myUtilitySpace.getMaxUtilityBid());
            return returnOffer;
        }

        Bid lastOpponentBid = lastReceivedBids.get(lastOpponent);
        double oppUtility = averageOpponentUtilitySpace.getUtilitySpace().getUtility(lastOpponentBid);
        double oppUtilityForMe = myUtilitySpace.getUtility(lastOpponentBid);

        Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);
        opponentPreviousPosition = getOpponentPreviousPosition(opponentCurrentPosition);

        // create a vector based on a move made
        Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);

        // classify the opponent move
        MoveType moveType = opponentVector.getMoveType();

        // create a vector, corresponding to my next move, based on the opponent move type,
        // and passed time (the longer we play, the better we know the opponent model, so our vector can be longer)
        Vector myDesiredVector = getDesiredVector(opponentVector, moveType);

        Action returnOffer;
        Bid myBid;
        if (isMyFirstBid()) {
            myBid = myUtilitySpace.getMaxUtilityBid();
            returnOffer = new Offer(myPartyId, myBid);
        } else {
            if (shouldAccept(lastReceivedBids, timeline)) {
                return new Accept(myPartyId, lastReceivedBids.get(lastOpponent));
            } else {

                Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);
                myBid = findClosestBid(myNegotiationInfo.getMyPossibleBids(), averageOpponentUtilitySpace.getUtilitySpace(), myDesiredPosition);
                returnOffer = new Offer(myPartyId, myBid);

            }
        }


        opponentPreviousPosition = opponentCurrentPosition;
        myPreviousPosition = new Position(myUtilitySpace.getUtility(myBid), averageOpponentUtilitySpace.getUtilitySpace().getUtility(myBid));

        for (Map.Entry<AgentID, Bid> entry : lastReceivedBids.entrySet()) {
            bidResponseMap.put(entry.getValue(), myBid);
        }

        return returnOffer;


    }

    private Action respontToAlreadyGivenBid(Map<AgentID, Bid> lastReceivedBids) {
        List<Bid> possibleResponses = new ArrayList<>();
        for (Map.Entry<AgentID, Bid> entry : lastReceivedBids.entrySet()) {
            if (bidResponseMap.get(entry.getValue()) != null) {
                possibleResponses.add(bidResponseMap.get(entry.getValue()));
            }
        }
        double maxUtilityResponse = 0;
        Bid finalResponse = null;
        if (possibleResponses.size() > 0) {
            for (Bid possibleResponse : possibleResponses) {
                if (myUtilitySpace.getUtility(possibleResponse) > maxUtilityResponse) {
                    maxUtilityResponse = myUtilitySpace.getUtility(possibleResponse);
                    finalResponse = possibleResponse;
                }
            }
        }

        if (finalResponse != null) {
            Action returnOffer = new Offer(myPartyId, finalResponse);
            return returnOffer;
        }
        return null;
    }

    private Vector getDesiredVector(Vector opponentVector, MoveType moveType) {

        Vector myDesiredVector;
        if (moveType == MoveType.SELFISH) {
            myDesiredVector = Vector.getMirroredVector(opponentVector).multiplyBy(2);
        } else if (moveType.equals(MoveType.CONCESSION)) {
            myDesiredVector = Vector.getMirroredVector(opponentVector).multiplyBy(0.5);
        } else if (moveType == MoveType.UNFORTUNATE) {
            myDesiredVector = Vector.getSameVector(opponentVector).multiplyBy(0.001);
        } else { //FORTUNATE
            myDesiredVector = Vector.getSameVector(opponentVector).multiplyBy(2);
        }
        return myDesiredVector;
    }

    private void updateOpponentUtilitySpace(BidHistory bidHistory, int step) {

        for (Map.Entry<AgentID, Map<Integer, Bid>> bidHistoryEntry : bidHistory.getBidHistory().entrySet()) {
            AgentID opponentId = bidHistoryEntry.getKey();
            Map<Integer, Bid> lastOpponentBids = bidHistoryEntry.getValue();

            opponentSpace.updateHSpace(opponentId, lastOpponentBids, step);
        }
    }

    private UtilityProbabilityObject getAverageOpponentUtilitySpace(Map<AgentID, Bid> lastReceivedBids) throws Exception {
        Map<AgentID, UtilityProbabilityObject> opponentsWeightsMap = new HashMap<>();

        for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
            AgentID opponentId = lastBidEntry.getKey();

            UtilityProbabilityObject utilityProbabilityObject = opponentSpace.getHSpaceElementWithBiggestWeight(opponentId);
//            UtilitySpaceSimple opponentsWeights = utilityProbabilityObject.getUtilitySpace();
            opponentsWeightsMap.put(opponentId, utilityProbabilityObject);
        }


        return utilitiesHelper.getMeanWeights(myUtilitySpace, opponentsWeightsMap);
    }

    private UtilityProbabilityObject getMaxOpponentUtilitySpace(Map<AgentID, Bid> lastReceivedBids) throws Exception {
        Double maxUtility = 0.0;
        UtilityProbabilityObject maxUtilitySpace = null;

        for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
            AgentID opponentId = lastBidEntry.getKey();
            Bid lastBid = lastBidEntry.getValue();

            UtilityProbabilityObject utilityProbabilityObject = opponentSpace.getHSpaceElementWithBiggestWeight(opponentId);
            if (utilityProbabilityObject.getUtilitySpace() != null) {
                double agentsUtility = utilityProbabilityObject.getUtilitySpace().getUtility(lastBid);
                if (maxUtility < agentsUtility) {
                    maxUtility = agentsUtility;
                    maxUtilitySpace = new UtilityProbabilityObject(utilityProbabilityObject.getUtilitySpace(), utilityProbabilityObject.getProbability());
                }
            }

        }


        return maxUtilitySpace;
    }


    private boolean shouldAccept(Map<AgentID, Bid> lastReceivedBids, TimeLineInfo timeline) {
        double timeFactor = timeline.getCurrentTime() / timeline.getTotalTime();
        double factor = 1.2 / (1 + Math.exp(7 * timeFactor - 6));
        for (Map.Entry<AgentID, Bid> entry : lastReceivedBids.entrySet()) {
            UtilityProbabilityObject oppSpace = opponentSpace.getHSpaceElementWithBiggestWeight(entry.getKey());
            Bid bidToAccept = entry.getValue();
            if (myUtilitySpace.getUtility(bidToAccept) < oppSpace.getUtilitySpace().getUtility(bidToAccept) * factor) {
                return false;
            }
        }
        return true;
    }

    private boolean isMyFirstBid() {
        return myPreviousPosition == null;
    }

    private Bid findClosestBid(Map<Bid, Double> myPossibleBids, UtilitySpaceSimple opponentUtilitySimple, Position myDesiredPosition) throws Exception {
        double minDistance = 100000;
        Bid bestBid = null;

        for (Map.Entry<Bid, Double> myBidEntry : myPossibleBids.entrySet()) {
            Bid myBid = myBidEntry.getKey();
            double myBidForOpponent = opponentUtilitySimple.getUtility(myBid);
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

    private Position getOpponentPreviousPosition(Position oppUtility) {
        if (opponentPreviousPosition == null) {
            opponentPreviousPosition = oppUtility;
        }
        return opponentPreviousPosition;

    }


}
