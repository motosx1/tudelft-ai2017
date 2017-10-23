package src.ai2017;

import negotiator.Domain;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.math.util.MathUtils.round;

/**
 * Created by bartosz on 17.10.2017.
 */
public class HSpaceElem {
    private List<CriterionFeatures> criterionFeatures;
    private double weight;

    public HSpaceElem(List<CriterionFeatures> criterionFeatures) {
        this.criterionFeatures = criterionFeatures;
    }

    public HSpaceElem(AdditiveUtilitySpace myUtilitySpace) {
        Set<Map.Entry<Objective, Evaluator>> evaluators = myUtilitySpace.getEvaluators();
        try {
            this.criterionFeatures = new ArrayList<>();
            for (Map.Entry<Objective, Evaluator> entry : evaluators) {
                String criterionName = entry.getKey().getName();
                EvaluatorDiscrete evaluator = (EvaluatorDiscrete) entry.getValue();

                double criterionWeight = evaluator.getWeight();
                Set<ValueDiscrete> features = evaluator.getValues();
                Map<ValueDiscrete, Double> hSpaceElemFeatures = new HashMap<>();
                for (ValueDiscrete featureDiscrete : features) {
                    Double featureWeight = evaluator.getEvaluation(featureDiscrete);
                    hSpaceElemFeatures.put(featureDiscrete, featureWeight);
                }

                CriterionFeatures criterionFeatures = new CriterionFeatures(criterionName, hSpaceElemFeatures, criterionWeight);
                this.criterionFeatures.add(criterionFeatures);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<CriterionFeatures> getCriterionFeatures() {
        return criterionFeatures;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "HSpaceElem{" +
                "criteria=" + criterionFeatures +
                ", w=" + round(weight, 5) +
                '}';
    }

//    static AdditiveUtilitySpace getAdditiveUtilitySpace(Domain domain, HSpaceElem opponentsWeights) {
//        Map<Objective, Evaluator> evals = new HashMap<>();
//
//        for (CriterionFeatures criterionFeatures : opponentsWeights.getCriterionFeatures()) {
////            Objective criterion = new Objective(domain.getObjectivesRoot(), criterionFeatures.getCriterion()); //tu
//            Objective correspondingObjective = domain.getObjectives().stream().filter(objective -> objective.getName().equalsIgnoreCase(criterionFeatures.getCriterion())).findFirst().get();
////            Objective criterion = new Objective(domain.getObjectivesRoot(), criterionFeatures.getCriterion(), domain.getObjectives().indexOf(correspondingObjective)); //tu
//
//            EvaluatorDiscrete eval = new EvaluatorDiscrete();
//            eval.setWeight(criterionFeatures.getWeight());
//            for (Map.Entry<ValueDiscrete, Double> featureEntry : criterionFeatures.getFeatures().entrySet()) {
//                try {
//                    eval.setEvaluationDouble(featureEntry.getKey(), featureEntry.getValue());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            List<String> collect = criterionFeatures.getFeatures().entrySet().stream().map(entry -> entry.getKey().getValue()).collect(Collectors.toList());
//            IssueDiscrete issueDiscrete = new IssueDiscrete(criterionFeatures.getCriterion(), domain.getObjectives().indexOf(correspondingObjective), collect.toArray(new String[collect.size()]));
//
//
//            evals.put(issueDiscrete, eval);
//        }
//
//
//        return new AdditiveUtilitySpace(domain, evals);
//    }
}
