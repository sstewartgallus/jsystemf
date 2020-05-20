package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.function.Function;

public record ConstantThunk<A, B>() implements ThunkTerm<V<A, V<B, F<A, F<B, A>>>>> {
    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<V<A, V<B, F<A, F<B, A>>>>>, Term<C>> k) {
        return k.apply(Term.v(left -> Term.v(right -> left.l(x -> right.l(y -> x)))));
    }

    @Override
    public Type<V<A, V<B, F<A, F<B, A>>>>> type() throws TypeCheckException {
        return Type.v(l -> Type.v(r -> l.to(r.to(l))));
    }

    @Override
    public Term<V<A, V<B, F<A, F<B, A>>>>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <X> Term<F<X, V<A, V<B, F<A, F<B, A>>>>>> pointFree(VarValue<X> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public String toString() {
        return "K";
    }
}
