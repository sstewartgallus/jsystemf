package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyValue;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;
import java.util.function.Function;

public record LambdaValue<A, B>(Type<A>domain,
                                Function<Term<A>, Term<B>>f) implements ValueTerm<F<A, B>>, CoreTerm<F<A, B>> {
    public LambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    public Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain);
        var body = visitor.term(f.apply(v));
        return new LambdaValue<>(visitor.type(domain), x -> v.substituteIn(body, x));
    }

    @Override
    public <X> Term<F<X, F<A, B>>> pointFree(VarValue<X> varValue) {
        // fixme... traditional solution is creating a tuple of the old var value and the new one...
        var v = new VarValue<>(domain);
        var body = f.apply(v);
        return body.pointFree(v).pointFree(varValue);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        try (var pretty = PrettyValue.generate(domain)) {
            var range = f.apply(pretty).type();
            return new FunctionType<>(domain, range);
        }
    }

    @Override
    public String toString() {
        try (var pretty = PrettyValue.generate(domain)) {
            var body = f.apply(pretty);
            return "({" + pretty + ": " + domain + "} → " + body + ")";
        }
    }

    public Term<B> apply(Term<A> x) {
        // fixme... typecheck domain?
        return f.apply(x);
    }
}
