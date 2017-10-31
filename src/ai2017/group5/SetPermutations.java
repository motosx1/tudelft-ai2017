package src.ai2017.group5;

import java.util.ArrayList;
import java.util.List;

class SetPermutations<E> {

    private final List<E> input;
    private final List<List<E>> result;

    static <E> List<List<E>> getSetPermutations(List<E> input) {
        SetPermutations<E> setPermutations = new SetPermutations<>(input);
        return setPermutations.build();
    }


    private SetPermutations(List<E> input) {
        this.input = input;
        result = new ArrayList<>();
    }


    private List<List<E>> build() {
        permute(new ArrayList<E>(), input);
        return result;
    }

    private void permute(List<E> prefix, List<E> suffix) {
        int n = suffix.size();
        if (n == 0) {
            List<E> newPrefix = new ArrayList<>(prefix);
            result.add(newPrefix);
        } else {
            for (int i = 0; i < n; i++) {
                List<E> newPrefix = new ArrayList<>(prefix);

                E elemi = suffix.get(i);
                if( !(elemi instanceof CriterionFeatures) ) {
                    newPrefix.add(suffix.get(i));
                } else {
                    CriterionFeatures cf = new CriterionFeatures((CriterionFeatures)elemi);
                    newPrefix.add((E) cf);
                }
                List<E> newStr = new ArrayList<>();

                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        E elemj = suffix.get(j);
                        if( !(elemj instanceof CriterionFeatures) ) {
                            newStr.add(elemj);
                        } else {
                            CriterionFeatures cf = new CriterionFeatures((CriterionFeatures)elemj);
                            newStr.add((E) cf);
                        }
                    }
                }

                permute(newPrefix, newStr);
            }
        }
    }


}