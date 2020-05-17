package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record TuplePairValue<H, T extends Tuple<T>>(Term<H>head, ValueTerm<T>tail) implements ValueTerm<P<H, T>> {
    public TuplePairValue {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public Term<P<H, T>> visitChildren(Visitor visitor) {
        return new TuplePairValue<>(visitor.term(head), Interpreter.normalize(visitor.term(tail)));
    }

    @Override
    public Type<P<H, T>> type() throws TypeCheckException {
        return new TuplePairType<>(head.type(), tail.type());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("(Δ ");
        str.append(head);
        Term<?> current = tail;
        while (current instanceof TuplePairValue<?, ?> cons) {
            str.append(" ").append(cons.head);
            current = cons.tail;
        }
        str.append(")");
        return str.toString();
    }
}