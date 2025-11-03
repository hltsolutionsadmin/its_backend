package com.its.common.populator;

public interface Populator<S, T> {
    void populate(S source, T target);
}
