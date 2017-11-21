package ai2017.group5;

import ai2017.group5.dao.HSpaceElement;
import ai2017.group5.helpers.structure.MyNegotiationInfoEnhanced;
import ai2017.group5.helpers.structure.RandomBidHelper;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Strategy {
    private final NegotiationInfo info;
    private final AgentID myPartyId;
    private final MyNegotiationInfoEnhanced myNegotiationInfo;
    private final AdditiveUtilitySpace myUtilitySpace;
    private final UtilitiesHelper utilitiesHelper;
    private Position opponentPreviousPosition = null;
    private Position myPreviousPosition = null;
    private HashMap<AgentID, List<HSpaceElement>> hSpace = null;
    private SpacePreparationHelper spacePreparationHelper = new SpacePreparationHelper();

    public Strategy(NegotiationInfo info, MyNegotiationInfoEnhanced myNegotiationInfo) {
        this.info = info;
        this.myUtilitySpace = (AdditiveUtilitySpace) info.getUtilitySpace();
        this.myPartyId = info.getAgentID();
        this.myNegotiationInfo = myNegotiationInfo;
        this.utilitiesHelper = new UtilitiesHelper();
    }


    public Action chooseAction(Map<AgentID, Bid> lastReceivedBids, int step, AgentID lastOpponent) {
        try {

            Map<AgentID, HSpaceElement> opponentsWeightsMap = new HashMap<>();

            for (Map.Entry<AgentID, Bid> lastBidEntry : lastReceivedBids.entrySet()) {
                AgentID agentId = lastBidEntry.getKey();

                Bid lastOpponentBid = lastBidEntry.getValue();

                recalculateHSpace(agentId, lastOpponentBid, step);
                HSpaceElement opponentsWeights = getHSpaceElemWithBiggestWeight(agentId);
                opponentsWeightsMap.put(agentId, opponentsWeights);
            }


            Bid lastOpponentBid = lastReceivedBids.get(lastOpponent);
            HSpaceElement meanOpponentsWeights = utilitiesHelper.getMeanWeights(info.getUtilitySpace(), opponentsWeightsMap);

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
                returnOffer = new Offer(myPartyId, myBid);
            } else {
                if (shouldAccept(lastOpponentBid)) {
                    return new Accept(myPartyId, lastReceivedBids.get(lastOpponent));
                } else {

                    Position myDesiredPosition = myPreviousPosition.add(myDesiredVector);
                    myBid = findClosestBid(myNegotiationInfo.getMyPossibleBids(), meanOpponentsWeights, myDesiredPosition);
                    returnOffer = new Offer(myPartyId, myBid);

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


        Bid maxUtilityBid = myNegotiationInfo.getMaxUtilityBid();
        if (maxUtilityBid != null) {
            return new Offer(myPartyId, maxUtilityBid);
        } else {
            return new Offer(myPartyId, RandomBidHelper.generateRandomBid(info));
        }

    }


    private HSpaceElement getHSpaceElemWithBiggestWeight(AgentID agentId) {
        double max = 0;
        HSpaceElement maxHSpaceElement = null;

        for (HSpaceElement hSpaceElement : hSpace.get(agentId)) {
            if (hSpaceElement.getWeight() > max) {
                max = hSpaceElement.getWeight();
                maxHSpaceElement = hSpaceElement;
            }
        }

        return maxHSpaceElement;
    }


    private boolean shouldAccept(Bid lastOpponentBid) {
        return myUtilitySpace.getUtility(lastOpponentBid) >= myPreviousPosition.getMyUtility();
    }

    private boolean isMyFirstBid() {
        return myPreviousPosition == null;
    }

    private Bid findClosestBid(Map<Bid, Double> myPossibleBids, HSpaceElement opponentsWeights, Position myDesiredPosition) throws Exception {
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


    private void recalculateHSpace(AgentID agentId, Bid oppBid, int step) {
        if (hSpace == null || hSpace.isEmpty()) {
            hSpace = new HashMap<>();
            hSpace.put(agentId, new ArrayList<HSpaceElement>());
        }

        if (hSpace.get(agentId) == null || hSpace.get(agentId).isEmpty()) {
            hSpace.put(agentId, spacePreparationHelper.prepareHSpace(myNegotiationInfo.getOpponentsUtilitySpace(), oppBid));
        }

        List<HSpaceElement> hSpaceForAgents = hSpace.get(agentId);

        Map<Integer, Double> pBHMap = utilitiesHelper.calculatePhbMap(oppBid, hSpaceForAgents, step);
        double denominator = utilitiesHelper.calculateDenominator(hSpaceForAgents, pBHMap);
        for (int i = 0; i < hSpaceForAgents.size(); i++) {
            HSpaceElement hSpaceElement = hSpaceForAgents.get(i);
            double newPhb = utilitiesHelper.calculatePhb(hSpaceForAgents, i, pBHMap, denominator);
            hSpaceElement.setWeight(newPhb);
        }
    }


}
