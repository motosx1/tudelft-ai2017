package ai2017.group5.helpers;

import negotiator.Bid;
import negotiator.issue.*;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.util.HashMap;
import java.util.Random;

public class RandomBidHelper {

    public static Bid generateRandomBid(NegotiationInfo info) {
        AbstractUtilitySpace myUtilitySpace = info.getUtilitySpace();
        try {

            HashMap<Integer, Value> values = new HashMap<>();

            for (Object currentIssue : myUtilitySpace.getDomain().getIssues()) {
                Issue currentIssue1 = (Issue) currentIssue;
                values.put((currentIssue1).getNumber(), getRandomValue(currentIssue1, info));
            }

            return new Bid(myUtilitySpace.getDomain(), values);
        } catch (Exception var4) {
            return new Bid(myUtilitySpace.getDomain());
        }
    }

    private static Value getRandomValue(Issue currentIssue, NegotiationInfo info) throws Exception {
        Random rand = new Random(info.getRandomSeed());
        Object currentValue;
        int index;
        switch (currentIssue.getType().ordinal()) {
            case 2:
                IssueDiscrete discreteIssue = (IssueDiscrete) currentIssue;
                index = rand.nextInt(discreteIssue.getNumberOfValues());
                currentValue = discreteIssue.getValue(index);
                break;
            case 3:
                IssueInteger integerIssue = (IssueInteger) currentIssue;
                index = rand.nextInt(integerIssue.getUpperBound() - integerIssue.getLowerBound() + 1);
                currentValue = new ValueInteger(integerIssue.getLowerBound() + index);
                break;
            case 4:
                IssueReal realIss = (IssueReal) currentIssue;
                index = rand.nextInt(realIss.getNumberOfDiscretizationSteps());
                currentValue = new ValueReal(realIss.getLowerBound() + (realIss.getUpperBound() - realIss.getLowerBound()) / (double) realIss.getNumberOfDiscretizationSteps() * (double) index);
                break;
            default:
                throw new Exception("issue type " + currentIssue.getType() + " not supported");
        }

        return (Value) currentValue;
    }

}
