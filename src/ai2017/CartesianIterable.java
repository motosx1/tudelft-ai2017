package src.ai2017;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CartesianIterable<T> implements Iterable<List<T>> {

    private List<List<T>> lilio;

    public CartesianIterable(List<List<T>> llo) {
        lilio = llo;
    }

    public Iterator<List<T>> iterator() {
        return new CartesianIterator<T>(lilio);
    }

    List<List<T>> getList(){
        List<List<T>> result = new ArrayList<>();
        for (List<T> ts : this) {
            result.add(ts);
        }
        return result;
    }



}