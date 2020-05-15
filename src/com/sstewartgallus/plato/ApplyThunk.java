package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pointfree.CallThunk;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;

public record ApplyThunk<A, B>(Term<F<A, B>>f, Term<A>x) implements ThunkTerm<B>, CoreTerm<B> {
    public ApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public <X> Term<F<X, B>> pointFree(VarValue<X> varValue) {
        // fixme.. is there a better way ? could also do compose eval...
        var fValue = f.pointFree(varValue);
        var xValue = x.pointFree(varValue);
        return new CallThunk<>(fValue, xValue);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionType<A, B>) fType;
        var range = funType.range();

        var argType = x.type();

        fType.unify(argType.to(range));

        return funType.range();
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }

    @Override
    public Term<B> stepThunk() {
        var fType = f.type();

        var funType = (FunctionType<A, B>) fType;
        var range = funType.range();

        var fNorm = Interpreter.normalize(f);
        return ((LambdaValue<A, B>) fNorm).apply(x);
    }
}
