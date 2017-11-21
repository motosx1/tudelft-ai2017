package ai2017.group5.helpers;

import ai2017.group5.helpers.hspace.MySpacePreparationHelper;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.HashMap;
import java.util.Map;

public class MyNegotiationInfoEnhanced {
    private int opponentsNum;
    private Map<Bid, Double> myPossibleBids = new HashMap<>();


    public MyNegotiationInfoEnhanced(AdditiveUtilitySpace myUtilitySpace) {

        //initialize my all possible bids, with the calculated utility
        MySpacePreparationHelper mySpacePreparationHelper = new MySpacePreparationHelper();
        this.myPossibleBids = mySpacePreparationHelper.generateMyPossibleBids(myUtilitySpace);

    }


    public Bid getMaxUtilityBid() {
        Map.Entry<Bid, Double> maxEntry = null;
        Bid maxBid = null;

        for (Map.Entry<Bid, Double> entry : myPossibleBids.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
                maxBid = entry.getKey();
            }
        }

        return maxBid;
    }

    public void setOpponentsNum(int opponentsNum) {
        this.opponentsNum = opponentsNum;
    }

    int getOpponentsNum() {
        return opponentsNum;
    }

    public Map<Bid, Double> getMyPossibleBids() {
        return myPossibleBids;
    }


}
