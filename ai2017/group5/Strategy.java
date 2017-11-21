package ai2017.group5;

import ai2017.group5.dao.Position;
import ai2017.group5.dao.Vector;
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


    Action chooseAction(Map<AgentID, Bid> lastReceivedBids, int step, AgentID lastOpponent) {
        try {
            // create average opponent utility
            UtilitySpaceSimple meanOpponentsUtilitySpaceSimple = getMeanOpponentsHSpaceElement(lastReceivedBids, step);

            Bid lastOpponentBid = lastReceivedBids.get(lastOpponent);
            double oppUtility = utilitiesHelper.calculateUtility(lastOpponentBid, meanOpponentsUtilitySpaceSimple);
            double oppUtilityForMe = myUtilitySpace.getUtility(lastOpponentBid);

            Position opponentCurrentPosition = new Position(oppUtilityForMe, oppUtility);
            opponentPreviousPosition = getOrInitOpponentPreviousPosition(opponentCurrentPosition);

            Vector opponentVector = new Vector(opponentPreviousPosition, opponentCurrentPosition);

            Vector myDesiredVector = Vector.getMirroredVector(opponentVector);

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
                    myBid = findClosestBid(myNegotiationInfo.getMyPossibleBids(), meanOpponentsUtilitySpaceSimple, myDesiredPosition);
                    returnOffer = new Offer(myPartyId, myBid);

                }
            }


            opponentPreviousPosition = opponentCurrentPosition;
            myPreviousPosition = new Position(myUtilitySpace.getUtility(myBid), utilitiesHelper.calculateUtility(myBid, meanOpponentsUtilitySpaceSimple));


            return returnOffer;


        } catch (Exception e) {
            e.printStackTrace();
        }


        Bid maxUtilityBid = myNegotiationInfo.getMaxUtilityBid();
        if (maxUtilityBid != null) {
            return new Offer(myPartyId, maxUtilityBid);
        } else {
            return new Offer(myPartyId, RandomBidHelper.generateRandomBid(info));
        }

    }

    private UtilitySpaceSimple getMeanOpponentsHSpaceElement(Map<AgentID, Bid> lastReceivedBids, int step) {
        Map<AgentID, UtilitySpaceSimple> opponentsWeightsMap = new HashMap<>();

        for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
            AgentID opponentId = lastBidEntry.getKey();
            Bid lastOpponentBid = lastBidEntry.getValue();

            opponentSpace.updateHSpace(opponentId, lastOpponentBid, step);
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

    private Bid findClosestBid(Map<Bid, Double> myPossibleBids, UtilitySpaceSimple opponentsWeights, Position myDesiredPosition) throws Exception {
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

    private Position getOrInitOpponentPreviousPosition(Position oppUtility) {
        if (opponentPreviousPosition == null) {
            opponentPreviousPosition = oppUtility;
        }
        return opponentPreviousPosition;

    }


}
