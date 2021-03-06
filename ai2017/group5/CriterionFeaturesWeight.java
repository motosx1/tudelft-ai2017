package ai2017.group5;

import negotiator.Bid;
import negotiator.issue.Issue;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by bartosz on 17.10.2017.
 */
public class CriterionFeaturesWeight {
    private List<CriterionFeatures> criterionFeatures;
    private double weight;

    public CriterionFeaturesWeight(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;
    }

    public List<CriterionFeatures> getCriterionFeatures() {
        return criterionFeatures;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return
                "criterion=" + criterionFeatures +
                        ", w=" + Math.round(weight) +
                        '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriterionFeaturesWeight that = (CriterionFeaturesWeight) o;

        if (Double.compare(that.weight, weight) != 0) return false;
        return criterionFeatures != null ? criterionFeatures.equals(that.criterionFeatures) : that.criterionFeatures == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = criterionFeatures != null ? criterionFeatures.hashCode() : 0;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public void sortByOtherBid(Bid bestOppBid) {
        List<CriterionFeatures> sorted = new LinkedList<>();
        for (Issue issue : bestOppBid.getIssues()) {
            CriterionFeatures correspondingCriterionFeature = getCorrespondingCriterionFeatures(issue);
            sorted.add(correspondingCriterionFeature);
        }
        criterionFeatures = sorted;
    }

    private CriterionFeatures getCorrespondingCriterionFeatures(Issue issue) {
        for (CriterionFeatures entry : this.criterionFeatures) {
            if (entry.getCriterion().equalsIgnoreCase(issue.getName())) {
                return entry;
            }
        }
        return null;
    }
}
