package src.ai2017;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DomainImpl;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.EvaluatorDiscrete;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This is your negotiation party.
 */
public class Group5 extends AbstractNegotiationParty {
    private Random rand = new Random(200);
    private Bid lastReceivedBid = null;

    private Map<String, EvaluatorDiscrete> dummyUtilitySpace = new HashMap<>(); // == fevaluators

    //    private List<List<CriterionFeatures>> hSpace = new ArrayList<>();
    private List<HSpaceElem> hSpace = new ArrayList<>();


    @Override
    public void init(NegotiationInfo info) {

        super.init(info);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());
    }

    public void initForTest() {

        prepareDummy();

        HSpacePreparationHelper hSpacePreparationHelper = new HSpacePreparationHelper(dummyUtilitySpace);
        hSpace = hSpacePreparationHelper.prepareHSpace();

        try {
            fight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fight() throws IOException {
        MathHelper mathHelper = new MathHelper();
        Bid oppBid1 = generateDummyBid1();
        Bid oppBid2 = generateDummyBid2();
        Bid oppBid3 = generateDummyBid3();
        Bid oppBid4 = generateDummyBid4();

        List<Bid> bids = new ArrayList<>(Arrays.asList(oppBid1, oppBid2, oppBid3, oppBid4));

        int steps = bids.size();

        for (int step = 0; step < steps; step++) {

            Map<Integer, Double> pBHMap = mathHelper.calculatePhbMap(bids.get(step), hSpace, step);
            double denominator = mathHelper.calculateDenominator(hSpace, pBHMap);
            for (int i = 0; i < hSpace.size(); i++) {
                HSpaceElem hSpaceElem = hSpace.get(i);
                double newPhb = mathHelper.calculatePhb(bids.get(step), hSpace, i, step, pBHMap, denominator);
                hSpaceElem.setWeight(newPhb);
            }
            System.out.println("\n=============== NEXT STEP ===============\n");
        }


        System.out.println('x');

    }


    private Bid generateDummyBid1() throws IOException {
        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
        HashMap values = new HashMap();

        putBidToValues(0, 0, domain, values);
        putBidToValues(1, 0, domain, values);

        return new Bid(domain, values);
    }

    private Bid generateDummyBid2() throws IOException {
        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
        HashMap values = new HashMap();

        putBidToValues(0, 0, domain, values);
        putBidToValues(1, 1, domain, values);

        return new Bid(domain, values);
    }

    private Bid generateDummyBid3() throws IOException {
        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
        HashMap values = new HashMap();

        putBidToValues(0, 1, domain, values);
        putBidToValues(1, 0, domain, values);

        return new Bid(domain, values);
    }

    private Bid generateDummyBid4() throws IOException {
        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
        HashMap values = new HashMap();

        putBidToValues(0, 1, domain, values);
        putBidToValues(1, 1, domain, values);

        return new Bid(domain, values);
    }

    private void putBidToValues(int issueNumber, int valueNumber, DomainImpl domain, HashMap values) {
        Issue issue = domain.getIssues().get(issueNumber);
        IssueDiscrete discreteIssue = (IssueDiscrete) issue;
        ValueDiscrete value = discreteIssue.getValue(valueNumber);
        values.put(issue.getNumber(), value);
    }

    private void prepareDummy() {
        try {
            EvaluatorDiscrete eval = new EvaluatorDiscrete();
            eval.setWeight(0.33);
            eval.setEvaluationDouble(new ValueDiscrete("Photo"), 0.33);
            eval.setEvaluationDouble(new ValueDiscrete("Plain"), 0.66);
            dummyUtilitySpace.put("Invitations", eval);


            eval = new EvaluatorDiscrete();
            eval.setWeight(0.66);
            eval.setEvaluationDouble(new ValueDiscrete("Your Dorm"), 0.66);
            eval.setEvaluationDouble(new ValueDiscrete("Party Room"), 0.33);
            dummyUtilitySpace.put("Location", eval);

//            eval = new EvaluatorDiscrete();
//            eval.setWeight(0.4);
//            eval.setEvaluationDouble(new ValueDiscrete("Beer"), 0.8);
//            eval.setEvaluationDouble(new ValueDiscrete("Wine"), 0.2);
//            utilitySpace.put("Drinks", eval);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // with 50% chance, counter offer
        // if we are the first party, also offer.
        if (lastReceivedBid == null || !validActions.contains(Accept.class) || Math.random() > 0.1) {
            return new Offer(getPartyId(), generateRandomBid());
        } else {
            return new Accept(getPartyId(), lastReceivedBid);
        }
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
        if (action instanceof Offer) {
            lastReceivedBid = ((Offer) action).getBid();
        }
    }

    @Override
    public String getDescription() {
        return "example party group 5";
    }

}
