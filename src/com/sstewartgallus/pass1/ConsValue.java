package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record ConsValue<H, T extends HList<T>>(Term<H> head, Term<T> tail) implements ValueTerm<HList.Cons<H, T>> {
    public ConsValue {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public Type<HList.Cons<H, T>> type() throws TypeCheckException {
        return new ConsNormal<>(head.type(), tail.type());
    }
}
