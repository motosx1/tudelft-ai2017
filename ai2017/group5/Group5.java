package ai2017.group5;

import ai2017.group5.helpers.BidHistory;
import ai2017.group5.helpers.MyNegotiationInfoEnhanced;
import ai2017.group5.helpers.RandomBidHelper;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is your negotiation party.
 */
public class Group5 extends AbstractNegotiationParty {
    private Map<AgentID, Bid> lastReceivedBids = new HashMap<>();
    private BidHistory bidHistory = new BidHistory();
    private AgentID lastOpponent;
    private int step = 0;
    private MyNegotiationInfoEnhanced myNegotiationInfo;

    private Strategy strategy;
    private NegotiationInfo info;


    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        this.step = 0;
        this.info = info;
        this.myNegotiationInfo = new MyNegotiationInfoEnhanced((AdditiveUtilitySpace) info.getUtilitySpace());
        this.strategy = new Strategy(info, myNegotiationInfo);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
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
        step++;
        timeline.getTotalTime();
        if (isFirstMove()) {
            return new Offer(getPartyId(), myNegotiationInfo.getMaxUtilityBid());
        } else {
            try {
                return this.strategy.chooseAction(lastReceivedBids, bidHistory, timeline, lastOpponent);

            } catch (Exception e) {
                e.printStackTrace();
            }


            Bid maxUtilityBid = myNegotiationInfo.getMaxUtilityBid();
            if (maxUtilityBid != null) {
                return new Offer(getPartyId(), maxUtilityBid);
            } else {
                return new Offer(getPartyId(), RandomBidHelper.generateRandomBid(info));
            }

        }
    }

    private boolean isFirstMove() {
        return lastReceivedBids == null || lastReceivedBids.isEmpty();
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
        if (action != null) {
            if (action instanceof Inform && "NumberOfAgents".equals(((Inform) action).getName()) && ((Inform) action).getValue() instanceof Integer) {
                Integer opponentsNum = (Integer) ((Inform) action).getValue();
                this.myNegotiationInfo.setOpponentsNum(opponentsNum);
            } else if (action instanceof Offer) {
                lastReceivedBids.put(sender, ((Offer) action).getBid());
                bidHistory.putBid(sender, ((Offer) action).getBid(), timeline.getCurrentTime());
                lastOpponent = sender;
            } else if (action instanceof Accept) {
                lastReceivedBids.put(sender, ((Accept) action).getBid());
                bidHistory.putBid(sender, ((Accept) action).getBid(), timeline.getCurrentTime());
//                lastOpponent = sender;
            }
        }
    }

    @Override
    public String getDescription() {
        return "example party group 5";
    }

}
