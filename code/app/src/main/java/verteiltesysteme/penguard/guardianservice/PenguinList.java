package verteiltesysteme.penguard.guardianservice;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class PenguinList implements Iterable<Penguin>{
    // For proper information hiding, never expose the penguins vector, not even via a get method!
    private final Vector<Penguin> penguins = new Vector<>();
    private GuardService guardService;

    public PenguinList(GuardService guardService) {
        this.guardService = guardService;
    }

    boolean contains(Penguin p){
        return penguins.contains(p);
    }

    boolean isEmpty(){
        return penguins.isEmpty();
    }

    void add(Penguin p){
        penguins.add(p);
    }

    void remove(Penguin p){
        // Be careful when reordering this remove call, penguinHasBeenRemoved in GuardService may depend on this execution order!
        penguins.remove(p);
        guardService.cancelAlarmAndNotificationForPenguin(p);
    }

    void removeAllElements(){
        while (!penguins.isEmpty()) {
            remove(0);
        }
    }

    void remove(int i){
        /* Be very careful when reordering method calls in here, since penguinHasBeenRemoved can depend on execution order.
         * In particular it may depend on whether a penguin has already been removed from a list or not.
         */
        Penguin toBeRemoved = penguins.get(i);
        penguins.remove(i);
        guardService.cancelAlarmAndNotificationForPenguin(toBeRemoved);
    }

    @Override
    public Iterator<Penguin> iterator() {
        return penguins.iterator();
    }

    int size(){
        return penguins.size();
    }

    Penguin get(int i){
        return penguins.get(i);
    }

    // Ideally we wouldn't expose the penguins Vector at all, but since ArrayAdapter need a collection, we return an unmodifiable copy of our penguins.
    List<Penguin> getUnmodifiableList(){
        return Collections.unmodifiableList(penguins);
    }
}
