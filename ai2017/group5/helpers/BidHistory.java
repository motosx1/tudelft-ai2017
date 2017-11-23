package ai2017.group5.helpers;


import negotiator.AgentID;
import negotiator.Bid;

import java.util.HashMap;
import java.util.Map;

public class BidHistory {
    private final Map<AgentID, Map<Double, Bid>> bidHistory = new HashMap<>();

    public Map<AgentID, Map<Double, Bid>> getBidHistory() {
        return bidHistory;
    }

    public Map<Double, Bid> getBidHistory(AgentID agentId) {
        if (bidHistory.get(agentId) == null) {
            bidHistory.put(agentId, new HashMap<Double, Bid>());
        }

        return bidHistory.get(agentId);
    }

    public void putBid(AgentID agentId, Bid bid, Double time) {
        if (bidHistory.get(agentId) == null) {
            bidHistory.put(agentId, new HashMap<Double, Bid>());
        }
        bidHistory.get(agentId).put(time, bid);
    }


}
