package ai2017.group5.helpers;

import negotiator.Bid;
import negotiator.issue.*;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RandomBidHelper {

//    public static Bid generateRandomBid(NegotiationInfo info) {
//        AbstractUtilitySpace myUtilitySpace = info.getUtilitySpace();
//        try {
//
//            HashMap<Integer, Value> values = new HashMap<>();
//
//            for (Object currentIssue : myUtilitySpace.getDomain().getIssues()) {
//                Issue currentIssue1 = (Issue) currentIssue;
//                values.put((currentIssue1).getNumber(), getRandomValue(currentIssue1, info));
//            }
//
//            return new Bid(myUtilitySpace.getDomain(), values);
//        } catch (Exception var4) {
//            return new Bid(myUtilitySpace.getDomain());
//        }
//    }

    /**
     * @return a random bid with high enough utility value.
     * @throws Exception
     *             if we can't compute the utility (eg no evaluators have been
     *             set) or when other evaluators than a DiscreteEvaluator are
     *             present in the util space.
     */
    public static Bid getRandomBid(NegotiationInfo info) {
        try {
            AbstractUtilitySpace utilitySpace = info.getUtilitySpace();

            HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs
            // <issuenumber,chosen
            // value
            // string>
            List<Issue> issues = utilitySpace.getDomain().getIssues();
            Random randomnr = new Random();

            // create a random bid with utility>MINIMUM_BID_UTIL.
            // note that this may never succeed if you set MINIMUM too high!!!
            // in that case we will search for a bid till the time is up (3 minutes)
            // but this is just a simple agent.
            Bid bid = null;
            do {
                for (Issue lIssue : issues) {
                    switch (lIssue.getType()) {
                        case DISCRETE:
                            IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
                            int optionIndex = randomnr.nextInt(lIssueDiscrete
                                    .getNumberOfValues());
                            values.put(lIssue.getNumber(),
                                    lIssueDiscrete.getValue(optionIndex));
                            break;
                        case REAL:
                            IssueReal lIssueReal = (IssueReal) lIssue;
                            int optionInd = randomnr.nextInt(lIssueReal
                                    .getNumberOfDiscretizationSteps() - 1);
                            values.put(
                                    lIssueReal.getNumber(),
                                    new ValueReal(lIssueReal.getLowerBound()
                                            + (lIssueReal.getUpperBound() - lIssueReal
                                            .getLowerBound())
                                            * (double) (optionInd)
                                            / (double) (lIssueReal
                                            .getNumberOfDiscretizationSteps())));
                            break;
                        case INTEGER:
                            IssueInteger lIssueInteger = (IssueInteger) lIssue;
                            int optionIndex2 = lIssueInteger.getLowerBound()
                                    + randomnr.nextInt(lIssueInteger.getUpperBound()
                                    - lIssueInteger.getLowerBound());
                            values.put(lIssueInteger.getNumber(), new ValueInteger(
                                    optionIndex2));
                            break;
                        default:
                            throw new Exception("issue type " + lIssue.getType()
                                    + " not supported by Group5");
                    }
                }
                bid = new Bid(utilitySpace.getDomain(), values);
            } while (utilitySpace.getUtility(bid) < 1 - utilitySpace.getReservationValue());

            return bid;
        } catch (Exception e ){
            e.printStackTrace();
        }

        return null;
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
