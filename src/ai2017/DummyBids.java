//package src.ai2017;
//
//import negotiator.Bid;
//import negotiator.DomainImpl;
//import negotiator.issue.Issue;
//import negotiator.issue.IssueDiscrete;
//import negotiator.issue.ValueDiscrete;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//
///**
// * Created by bartosz on 20.10.2017.
// */
//public class DummyBids {
//
//    static Bid generateDummyBid1() throws IOException {
//        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
//        HashMap values = new HashMap();
//
//        putBidToValues(0, 0, domain, values);
//        putBidToValues(1, 0, domain, values);
//
//        return new Bid(domain, values);
//    }
//
//    static Bid generateDummyBid2() throws IOException {
//        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
//        HashMap values = new HashMap();
//
//        putBidToValues(0, 0, domain, values);
//        putBidToValues(1, 1, domain, values);
//
//        return new Bid(domain, values);
//    }
//
//    static Bid generateDummyBid3() throws IOException {
//        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
//        HashMap values = new HashMap();
//
//        putBidToValues(0, 1, domain, values);
//        putBidToValues(1, 0, domain, values);
//
//        return new Bid(domain, values);
//    }
//
//    static Bid generateDummyBid4() throws IOException {
//        DomainImpl domain = new DomainImpl(new File("etc/templates/partydomain/simple/party_domain.xml"));
//        HashMap values = new HashMap();
//
//        putBidToValues(0, 1, domain, values);
//        putBidToValues(1, 1, domain, values);
//
//        return new Bid(domain, values);
//    }
//
//    private static void putBidToValues(int issueNumber, int valueNumber, DomainImpl domain, HashMap values) {
//        Issue issue = domain.getIssues().get(issueNumber);
//        IssueDiscrete discreteIssue = (IssueDiscrete) issue;
//        ValueDiscrete value = discreteIssue.getValue(valueNumber);
//        values.put(issue.getNumber(), value);
//    }
//
//}
