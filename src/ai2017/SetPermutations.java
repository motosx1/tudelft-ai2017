package src.ai2017;

import java.util.ArrayList;
import java.util.List;

public class SetPermutations<E> {

    private final List<E> input;
    private final List<List<E>> result;

    public static <E> List<List<E>> getSetPermutations(List<E> input) {
        SetPermutations<E> setPermutations = new SetPermutations<>(input);
        return setPermutations.build();
    }


    private SetPermutations(List<E> input) {
        this.input = input;
        result = new ArrayList<>();
    }


    private List<List<E>> build() {
        permute(new ArrayList<>(), input);
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
                newPrefix.add(suffix.get(i));
                List<E> newStr = new ArrayList<>();

                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        newStr.add(suffix.get(j));
                    }
                }

                permute(newPrefix, newStr);
            }
        }
    }


}