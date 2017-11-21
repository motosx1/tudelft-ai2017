package ai2017.group5.helpers.structure;

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
    private double reservationValue;
    private Map<Bid, Double> myPossibleBids = new HashMap<>();
    private Map<Issue, EvaluatorDiscrete> opponentsUtilitySpace = new HashMap<>();


    public MyNegotiationInfoEnhanced(AdditiveUtilitySpace myUtilitySpace) {
        //initialize opponent utility space, based on our domain - setting all the weights to 0;
        this.opponentsUtilitySpace = prepareOpponentUtilitySpace(myUtilitySpace.getDomain());

        //initialize my all possible bids, with the calculated utility
        MySpacePreparationHelper mySpacePreparationHelper = new MySpacePreparationHelper();
        this.myPossibleBids = mySpacePreparationHelper.generateMyPossibleBids(myUtilitySpace);

    }

    private Map<Issue, EvaluatorDiscrete> prepareOpponentUtilitySpace(Domain domain) {
        Map<Issue, EvaluatorDiscrete> result = new HashMap<>();
        for (Issue issue : domain.getIssues()) {
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete eval = new EvaluatorDiscrete();
            try {
                for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                    eval.setEvaluationDouble(valueDiscrete, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            result.put(issue, eval);
        }

        return result;
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

    public Map<Issue, EvaluatorDiscrete> getOpponentsUtilitySpace() {
        return opponentsUtilitySpace;
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
