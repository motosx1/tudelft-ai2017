package ai2017.group5;

import ai2017.group5.dao.Position;
import ai2017.group5.dao.Vector;
import ai2017.group5.helpers.BidHistory;
import ai2017.group5.helpers.MoveType;
import ai2017.group5.helpers.MyNegotiationInfoEnhanced;
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

import java.util.HashMap;
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

    Strategy(NegotiationInfo info, MyNegotiationInfoEnhanced myNegotiationInfo) {
        this.info = info;
        this.myUtilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        this.myPartyId = info.getAgentID();
        this.myNegotiationInfo = myNegotiationInfo;
        this.opponentSpace = new OpponentSpace(info.getUtilitySpace().getDomain());
    }


    Action chooseAction(Map<AgentID, Bid> lastReceivedBids, BidHistory bidHistory, TimeLineInfo timeline, AgentID lastOpponent) throws Exception {

        updateOpponentUtilitySpace(bidHistory, timeline);

        // create average opponent utility space
        UtilitySpaceSimple averageOpponentUtilitySpace = getAverageOpponentUtilitySpace(lastReceivedBids);

        Bid lastOpponentBid = lastReceivedBids.get(lastOpponent);
        double oppUtility = averageOpponentUtilitySpace.getUtility(lastOpponentBid);
        double oppUtilityForMe = myUtilitySpace.getUtility(lastOpponentBid);

        Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);
        opponentPreviousPosition = getOpponentPreviousPosition(opponentCurrentPosition);

        // create a vector based on a move made
        Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);

        // classify the opponent move
        MoveType moveType = opponentVector.getMoveType();

        // create a vector, corresponding to my next move, based on the opponent move type
        Vector myDesiredVector = getDesiredVector(opponentVector, moveType);

        Action returnOffer;
        Bid myBid;
        if (isMyFirstBid()) {
            myBid = myUtilitySpace.getMaxUtilityBid();
            returnOffer = new Offer(myPartyId, myBid);
        } else {
            if (shouldAccept(lastOpponentBid)) {
                return new Accept(myPartyId, lastReceivedBids.get(lastOpponent));
            } else {

                Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);
                myBid = findClosestBid(myNegotiationInfo.getMyPossibleBids(), averageOpponentUtilitySpace, myDesiredPosition);
                returnOffer = new Offer(myPartyId, myBid);

            }
        }


        opponentPreviousPosition = opponentCurrentPosition;
        myPreviousPosition = new Position(myUtilitySpace.getUtility(myBid), averageOpponentUtilitySpace.getUtility(myBid));


        return returnOffer;


    }

    private Vector getDesiredVector(Vector opponentVector, MoveType moveType) {
        Vector myDesiredVector;
        if (moveType == MoveType.SELFISH || moveType.equals(MoveType.CONCESSION)) {
            myDesiredVector = Vector.getMirroredVector(opponentVector);
        } else { //if( moveType == MoveType.FORTUNATE || moveType == MoveType.UNFORTUNATE){
            myDesiredVector = Vector.getSameVector(opponentVector);
        }
        return myDesiredVector;
    }

    private void updateOpponentUtilitySpace(BidHistory bidHistory, TimeLineInfo timeline) {

        for (Map.Entry<AgentID, Map<Double, Bid>> bidHistoryEntry : bidHistory.getBidHistory().entrySet()) {
            AgentID opponentId = bidHistoryEntry.getKey();
            Map<Double, Bid> lastOpponentBids = bidHistoryEntry.getValue();

            opponentSpace.updateHSpace(opponentId, lastOpponentBids, timeline);
        }
    }

    private UtilitySpaceSimple getAverageOpponentUtilitySpace(Map<AgentID, Bid> lastReceivedBids) throws Exception {
        Map<AgentID, UtilitySpaceSimple> opponentsWeightsMap = new HashMap<>();

        for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
            AgentID opponentId = lastBidEntry.getKey();

            UtilitySpaceSimple opponentsWeights = opponentSpace.getHSpaceElementWithBiggestWeight(opponentId);
            opponentsWeightsMap.put(opponentId, opponentsWeights);
        }


        return utilitiesHelper.getMeanWeights(myUtilitySpace, opponentsWeightsMap);
    }


    private boolean shouldAccept(Bid lastOpponentBid) {
        return myUtilitySpace.getUtility(lastOpponentBid) >= myPreviousPosition.getMyUtility();
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
