package info.kgeorgiy.ja.kadyrov.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> data;
    private final Comparator<? super E> comparator;

    // :NOTE: duplicate code
    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    public ArraySet() {
        this(Collections.emptyList(), Comparator.naturalOrder());
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(List.copyOf(new TreeSet<>(collection)), Comparator.naturalOrder());
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this(toList(collection, comparator), comparator);
    }

    private static <E> List<E> toList(Collection<? extends E> collection, Comparator<? super E> comparator) {
        TreeSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        return List.copyOf(treeSet);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    public Comparator<? super E> comparator() {
        if (comparator == Comparator.naturalOrder()) {
            return null;
        }
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        return (Collections.binarySearch(data, (E) element, comparator) >= 0);
    }

    @Override
    // :NOTE: O(n), duplicate
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("First element greater than second element");
        }
        return checkedSubSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        if (data.size() == 0) {
            return new ArraySet<E>(comparator);
        }
        return checkedSubSet(first(), true, toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        if (data.size() == 0) {
            return new ArraySet<E>(comparator);
        }
        return checkedSubSet(fromElement, true, last(), true);
    }

    @Override
    public E first() {
        if (data.isEmpty())
            throw new NoSuchElementException("No lowest element.");
        return data.get(0);
    }

    @Override
    public E last() {
        if (data.size() == 0)
            throw new NoSuchElementException("No highest element.");
        return data.get(data.size() - 1);
    }

    private int findIndex(E element) {
        int i = Collections.binarySearch(data, element, comparator);
        return i < 0 ? -i - 1 : i;
    }

    private SortedSet<E> checkedSubSet(E fromElement, boolean inclusiveFrom, E toElement, boolean inclusiveTo) {
        int fromIndex = findIndex(fromElement);
        if (!inclusiveFrom) {
            fromIndex--;
        }
        int toIndex = findIndex(toElement);
        if (inclusiveTo) {
            toIndex++;
        }
        return new ArraySet<>(data.subList(fromIndex, toIndex), comparator);
    }
}
