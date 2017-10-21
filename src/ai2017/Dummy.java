//package src.ai2017;
//
//import negotiator.issue.ValueDiscrete;
//import negotiator.utility.EvaluatorDiscrete;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by bartosz on 20.10.2017.
// */
//public class Dummy {
//
//    static Map<String, EvaluatorDiscrete> prepareDummyOpponent() {
//        try {
//            Map<String, EvaluatorDiscrete> result = new HashMap<>(); // == fevaluators
//            EvaluatorDiscrete eval = new EvaluatorDiscrete();
//            eval.setWeight(0.33);
//            eval.setEvaluationDouble(new ValueDiscrete("Photo"), 0.33);
//            eval.setEvaluationDouble(new ValueDiscrete("Plain"), 0.66);
//            result.put("Invitations", eval);
//
//
//            eval = new EvaluatorDiscrete();
//            eval.setWeight(0.66);
//            eval.setEvaluationDouble(new ValueDiscrete("Your Dorm"), 0.66);
//            eval.setEvaluationDouble(new ValueDiscrete("Party Room"), 0.33);
//            result.put("Location", eval);
//
//            return result;
//
////            eval = new EvaluatorDiscrete();
////            eval.setWeight(0.4);
////            eval.setEvaluationDouble(new ValueDiscrete("Beer"), 0.8);
////            eval.setEvaluationDouble(new ValueDiscrete("Wine"), 0.2);
////            utilitySpace.put("Drinks", eval);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    static Map<String, EvaluatorDiscrete> prepareMyUtility() {
//        try {
//            Map<String, EvaluatorDiscrete> result = new HashMap<>(); // == fevaluators
//            EvaluatorDiscrete eval = new EvaluatorDiscrete();
//            eval.setWeight(0.66);
//            eval.setEvaluationDouble(new ValueDiscrete("Photo"), 0.66);
//            eval.setEvaluationDouble(new ValueDiscrete("Plain"), 0.33);
//            result.put("Invitations", eval);
//
//
//            eval = new EvaluatorDiscrete();
//            eval.setWeight(0.33);
//            eval.setEvaluationDouble(new ValueDiscrete("Your Dorm"), 0.66);
//            eval.setEvaluationDouble(new ValueDiscrete("Party Room"), 0.33);
//            result.put("Location", eval);
//
//            return result;
//
////            eval = new EvaluatorDiscrete();
////            eval.setWeight(0.4);
////            eval.setEvaluationDouble(new ValueDiscrete("Beer"), 0.8);
////            eval.setEvaluationDouble(new ValueDiscrete("Wine"), 0.2);
////            utilitySpace.put("Drinks", eval);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
