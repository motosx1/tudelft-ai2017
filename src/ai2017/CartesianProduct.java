package src.ai2017;

import java.util.*;

class CartesianProduct {

    static <T> List<List<T>> calculate(List<List<T>> input) {
        List<List<T>> res = new ArrayList<>();
        if (input.isEmpty()) { // if no more elements to process
            res.add(new ArrayList<>()); // then add empty list and return
            return res;
        } else {
            process(input, res); // we need to calculate the cartesian product of input and store it in res variable
        }
        return res; // method completes , return result
    }

    static <T, E> List<Map<T, E>> calculateMap(List<Map<T, E>> input) {
        List<Map<T, E>> res = new ArrayList<>();
        if (input.isEmpty()) { // if no more elements to process
            res.add(new HashMap<>()); // then add empty list and return
            return res;
        } else {
            processMap(input, res); // we need to calculate the cartesian product of input and store it in res variable
        }
        return res; // method completes , return result
    }

    private static <T, E> void processMap(List<Map<T, E>> lists, List<Map<T, E>> res) {
        Map<T, E> head = lists.get(0); //take first element of the list
        List<Map<T, E>> tail = calculateMap(lists.subList(1, lists.size())); //invoke calculate on remaining element, here is recursion

        for (Map.Entry<T, E> entry : head.entrySet()) {
            for (Map<T, E> t : tail) { //iterate over the tail
                Map<T, E> tmp = new HashMap<>(t.size());
                tmp.put(entry.getKey(), entry.getValue()); // add the head
                tmp.putAll(t); // and current tail element
                res.add(tmp);
            }
        }
    }


    private static <T> void process(List<List<T>> lists, List<List<T>> res) {
        List<T> head = lists.get(0); //take first element of the list
        List<List<T>> tail = calculate(lists.subList(1, lists.size())); //invoke calculate on remaining element, here is recursion

        for (T h : head) { // for each head
            for (List<T> t : tail) { //iterate over the tail
                List<T> tmp = new ArrayList<>(t.size());
                tmp.add(h); // add the head
                tmp.addAll(t); // and current tail element
                res.add(tmp);
            }
        }
    }

    public static void main(String[] args) {
        //we invoke the calculate method
//        System.out.println(calculate(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6))));
    }
}
