package com.sstewartgallus.interpreter;

import java.util.Objects;
import java.util.function.Function;

public record ApplyCode<A, B>(Code<Function<A, B>>f, Code<A>x) implements Code<B> {
    public ApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }

}
