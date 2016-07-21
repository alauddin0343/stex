package cz.uhk.cityunavigate.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * List that allows observers to detect changes such as adding
 * or removing elements.
 */
public class ObservableList<E> implements List<E>, Serializable {
    private ArrayList<E> data = new ArrayList<>();

    private final List<ItemAddListener<E>> addListeners = new ArrayList<>();
    private final List<ItemRemoveListener<E>> removeListeners = new ArrayList<>();

    public ObservableList() {
    }


    /**
     * Creates a new observable list pre-filled with the items from another collection.
     * The add observers are not called for these items.
     * @param other data
     */
    public ObservableList(@NotNull Collection<? extends E> other) {
        addAll(other);
    }

    private void fireItemsAdded(Collection<E> added) {
        for (ItemAddListener<E> addListener : addListeners) {
            addListener.onItemAdded(this, added);
        }
    }

    private void fireItemsRemoved(Collection<E> removed) {
        for (ItemRemoveListener<E> removeListener : removeListeners) {
            removeListener.onItemRemoved(this, removed);
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    @NotNull
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    @NotNull
    public Object[] toArray() {
        return data.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    @NotNull
    public <T> T[] toArray(@NotNull T[] a) {
        return data.toArray(a);
    }

    @Override
    public boolean add(@NotNull E e) {
        //noinspection ConstantConditions
        if (e == null)
            throw new NullPointerException("ObservableList cannot contain null values");
        if (data.add(e)) {
            fireItemsAdded(Collections.singletonList(e));
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        if (data.remove(o)) {
            fireItemsRemoved(Collections.singletonList((E) o));
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        if (c.contains(null))
            throw new NullPointerException("ObservableList cannot contain null values");

        if (data.addAll(c)) {
            fireItemsAdded(new ArrayList<>(c));
            return true;
        }
        return false;
    }

    public boolean addAll(@NotNull List<E> c) {
        if (c.contains(null))
            throw new NullPointerException("ObservableList cannot contain null values");

        if (data.addAll(c)) {
            fireItemsAdded(c);
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int index,  @NotNull Collection<? extends E> c) {
        if (c.contains(null))
            throw new NullPointerException("ObservableList cannot contain null values");

        if (data.addAll(index, c)) {
            fireItemsAdded(new ArrayList<>(c));
            return true;
        }
        return false;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return batchRemove(c, false);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return batchRemove(c, true);
    }

    private boolean batchRemove(@NotNull Collection<?> c, boolean complement) {
        //noinspection ConstantConditions
        if (c == null)
            throw new NullPointerException("Collection to remove/retain cannot be null");

        ArrayList<E> removed = new ArrayList<>();
        ArrayList<E> remaining = new ArrayList<>();
        try {
            if (!data.isEmpty() && !c.isEmpty() && c.iterator().next() != null) {
                if (!data.get(0).getClass().isAssignableFrom(c.iterator().next().getClass()))
                    throw new ClassCastException("Invalid collection type for removeAll/retainAll");
            }

            for (E elem : data) {
                if (c.contains(elem) != complement) {
                    removed.add(elem);
                } else {
                    remaining.add(elem);
                }
            }
        } catch (Throwable thr) {
            return false;
        }

        data.clear(); // to force ConcurrentModificationExceptions
        data = remaining;
        if (removed.isEmpty()) {
            return false;
        } else {
            fireItemsRemoved(removed);
            return true;
        }
    }

    @Override
    public void clear() {
        if (!data.isEmpty()) {
            ArrayList<E> tmp = new ArrayList<>(data);
            data.clear();
            fireItemsRemoved(tmp);
        }
    }

    @Override
    public E get(int index) {
        return data.get(index);
    }

    @Override
    public E set(int index, @NotNull E element) {
        //noinspection ConstantConditions
        if (element == null)
            throw new NullPointerException("ObservableList cannot contain null values");

        final E previous = data.set(index, element);
        fireItemsRemoved(Collections.singletonList(previous));
        fireItemsAdded(Collections.singletonList(element));
        return previous;
    }

    @Override
    public void add(int index, @NotNull E element) {
        //noinspection ConstantConditions
        if (element == null)
            throw new NullPointerException("ObservableList cannot contain null values");
        data.add(index, element);
        fireItemsAdded(Collections.singletonList(element));
    }

    @Override
    public E remove(int index) {
        final E res = data.remove(index);
        fireItemsRemoved(Collections.singletonList(res));
        return res;
    }

    @Override
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    @Override
    @NotNull
    public ListIterator<E> listIterator() {
        return data.listIterator();
    }

    @Override
    @NotNull
    public ListIterator<E> listIterator(int index) {
        return data.listIterator(index);
    }

    @Override
    @NotNull
    public List<E> subList(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObservableList)
            return ((ObservableList) obj).data.equals(this.data);
        return obj instanceof Collection && data.equals(obj);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return data.toString();
    }

    /**
     * Listen to add item events
     * @param listener add event listener
     * @return this for fluent api
     */
    public ObservableList<E> addItemAddListener(ItemAddListener<E> listener) {
        addListeners.add(listener);
        return this;
    }

    /**
     * Listen to item remove events
     * @param listener listener to add
     * @return this for fluent api
     */
    public ObservableList<E> addItemRemoveListener(ItemRemoveListener<E> listener) {
        removeListeners.add(listener);
        return this;
    }

    /**
     * Makes sure that all items from the given list are part of this list as well.
     * @param list list to synchronize with
     * @param <T> list type
     * @return this for fluent api
     */
    public <T extends E> ObservableList<E> synchronizeWith(@NotNull ObservableList<T> list) {
        // copy all available content
        this.addAll(list);
        // register future event
        list.addItemAddListener(new ItemAddListener<T>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<T> list, @NotNull Collection<T> addedItems) {
                addAll(addedItems);
            }
        });
        list.addItemRemoveListener(new ItemRemoveListener<T>() {
            @Override
            public void onItemRemoved(@NotNull ObservableList<T> list, @NotNull Collection<T> removedItems) {
                removeAll(removedItems);
            }
        });

        return this;
    }

    /**
     * Maps the observable list to another observable list with the mapper applied
     * to each element of the list. The lists stay synchronized.
     * @param mapper mapping function
     * @param <R> result type
     * @return mapped observable list
     */
    public <R> ObservableList<R> map(final Function<? super E, ? extends R> mapper) {
        final ObservableList<R> res = new ObservableList<>();

        final Map<E, R> cache = new HashMap<>();
        for (E elem : this) {
            R mapped = mapper.apply(elem);
            res.add(mapped);
            cache.put(elem, mapped);
        }

        addItemAddListener(new ItemAddListener<E>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<E> list, @NotNull Collection<E> addedItems) {
                for (E addedItem : addedItems) {
                    R mapped = mapper.apply(addedItem);
                    cache.put(addedItem, mapped);
                    res.add(mapped);
                }
            }
        });

        addItemRemoveListener(new ItemRemoveListener<E>() {
            @Override
            public void onItemRemoved(@NotNull ObservableList<E> list, @NotNull Collection<E> removedItems) {
                for (E removedItem : removedItems) {
                    R mapped = cache.get(removedItem);
                    if (mapped != null)
                        res.remove(mapped);
                    cache.remove(removedItem);
                }
            }
        });

        return res;
    }

    /**
     * Maps and flattens the observable list to another observable list with the mapper applied
     * to each element of the list. The lists stay synchronized.
     * @param mapper mapping function
     * @param <R> result type
     * @return mapped observable list
     */
    public <R> ObservableList<R> flatMap(final Function<? super E, ObservableList<R>> mapper) {
        final ObservableList<R> res = new ObservableList<>();

        final Map<E, R> cache = new HashMap<>();
        for (E elem : this) {
            ObservableList<R> mapped = mapper.apply(elem);
            for (R r : mapped) {
                res.add(r);
                cache.put(elem, r);
            }

            addItemAddListener(new ItemAddListener<E>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<E> list, @NotNull Collection<E> addedItems) {
                    for (E addedItem : addedItems) {
                        ObservableList<R> mapped = mapper.apply(addedItem);
                        mapped.addItemAddListener(new ItemAddListener<R>() {
                            @Override
                            public void onItemAdded(@NotNull ObservableList<R> list, @NotNull Collection<R> addedItems) {

                            }
                        });
                        for (R r : mapped) {
                            res.add(r);
                            cache.put(addedItem, r);
                        }
                    }
                }
            });

            addItemRemoveListener(new ItemRemoveListener<E>() {
                @Override
                public void onItemRemoved(@NotNull ObservableList<E> list, @NotNull Collection<E> removedItems) {
                    for (E removedItem : removedItems) {
                        R mapped = cache.get(removedItem);
                        if (mapped != null)
                            res.remove(mapped);
                        cache.remove(removedItem);
                    }
                }
            });
        }



        return res;
    }

    /**
     * Called when one or more items are removed from the list. The list is given as the first parameter
     * and already has the items removed when the observer is called. You can access the removed items
     * using the second argument removedItems.
     * @param <E> element type
     */
    public interface ItemRemoveListener<E> {
        void onItemRemoved(@NotNull ObservableList<E> list, @NotNull Collection<E> removedItems);
    }

    /**
     * Called when one or more items are added to the list. The list is given as the first parameter
     * and already has the items added when the observer is called. You can access only the added items
     * using the second argument addedItems.
     * @param <E> element type
     */
    public interface ItemAddListener<E> {
        void onItemAdded(@NotNull ObservableList<E> list, @NotNull Collection<E> addedItems);
    }
}
