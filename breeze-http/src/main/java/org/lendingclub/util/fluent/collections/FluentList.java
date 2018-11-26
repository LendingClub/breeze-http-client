package org.lendingclub.util.fluent.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluentList extends ArrayList<Object> {
    private static final long serialVersionUID = -1;

    public static FluentList list() {
        return new FluentList();
    }

    public static FluentList list(Object... items) {
        return new FluentList(items);
    }

    public static FluentList list(List<?> items) {
        return new FluentList(items);
    }

    public FluentList() {
    }

    public FluentList(Object item) {
        add(item);
    }

    public FluentList(Object... items) {
        super();
        items(items);
    }

    public FluentList(List<?> items) {
        super();
        items(items);
    }

    public FluentList item(Object item) {
        add(item);
        return this;
    }

    public FluentList items(Object... items) {
        Collections.addAll(this, items);
        return this;
    }

    public FluentList items(List<?> items) {
        addAll(items);
        return this;
    }
}
