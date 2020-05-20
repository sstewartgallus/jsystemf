package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public final class CurryValue<L extends Tuple<L>, C, D> implements ThunkTerm<F<F<L, C>, D>> {
    private final Signature<L, C, D> signature;

    public CurryValue(Signature<L, C, D> signature) {
        Objects.requireNonNull(signature);
        this.signature = signature;
    }

    public Signature<L, C, D> signature() {
        return signature;
    }

    @Override
    public Term<F<F<L, C>, D>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <B> Term<B> stepThunk(Function<ValueTerm<F<F<L, C>, D>>, Term<B>> k) {
        return k.apply(signature.argType().to(signature.retType()).l(signature::curry));
    }

    @Override
    public Type<F<F<L, C>, D>> type() throws TypeCheckException {
        var argType = signature.argType();
        var retType = signature.retType();
        return argType.to(retType).to(signature.type());
    }

    @Override
    public String toString() {
        return "curry";
    }

}
