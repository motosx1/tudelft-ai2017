package ai2017.group5;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.HashMap;
import java.util.Map;

class MyNegotiationInfoEnhanced {
    private int opponentsNum;
    private double reservationValue;
    private Map<Bid, Double> myPossibleBids = new HashMap<>();
    private SpacePreparationHelper spacePreparationHelper;
    private MySpacePreparationHelper mySpacePreparationHelper;
    private Map<Issue, EvaluatorDiscrete> opponentsUtilitySpace = new HashMap<>();


    public MyNegotiationInfoEnhanced(AdditiveUtilitySpace myUtilitySpace) {
        this.spacePreparationHelper = new SpacePreparationHelper();
        this.mySpacePreparationHelper = new MySpacePreparationHelper();

        //initialize opponent utility space, based on our domain - setting all the weights to 0;
        this.opponentsUtilitySpace = prepareOpponentUtilitySpace(myUtilitySpace.getDomain());

        //initialize my all possible bids, with the evaluation assigned
        this.myPossibleBids = this.mySpacePreparationHelper.generateMyPossibleBids(myUtilitySpace);

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

    public Map<Issue, EvaluatorDiscrete> getOpponentsUtilitySpace() {
        return opponentsUtilitySpace;
    }

    void setOpponentsNum(int opponentsNum) {
        this.opponentsNum = opponentsNum;
    }

    int getOpponentsNum() {
        return opponentsNum;
    }

    public double getReservationValue() {
        return reservationValue;
    }

    public void setReservationValue(double reservationValue) {
        this.reservationValue = reservationValue;
    }

    public Map<Bid, Double> getMyPossibleBids() {
        return myPossibleBids;
    }
}
