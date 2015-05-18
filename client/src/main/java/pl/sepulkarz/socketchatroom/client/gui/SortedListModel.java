package pl.sepulkarz.socketchatroom.client.gui;

import javax.swing.*;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * List model that maintains natural order. Minimal necessary implementation.
 *
 * @param <T> Type of the element.
 */
public class SortedListModel<T> extends AbstractListModel<T> {

    private SortedSet<T> model = new TreeSet<T>();

    @Override
    public int getSize() {
        return model.size();
    }

    public T getElementAt(int index, Iterator<T> iterator) {
        T element = iterator.next();
        return index == 0 ? element : getElementAt(index - 1, iterator);
    }

    @Override
    public T getElementAt(int index) {
        return getElementAt(index, model.iterator());
    }

    public void addElement(T element) {
        model.add(element);
        fireContentsChanged(this, 0, model.size());
    }

    public void removeElement(T element) {
        model.remove(element);
        fireContentsChanged(this, 0, model.size());
    }

}
