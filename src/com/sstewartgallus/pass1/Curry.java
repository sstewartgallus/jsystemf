package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

public final class Curry {
    private Curry() {
    }

    public static <A> Term<A> curry(Term<A> term, IdGen ids) {
        if (!(term instanceof CoreTerm<A> core)) {
            return term;
        }

        if (core instanceof PureValue || core instanceof VarValue) {
            return core;
        }

        if (core instanceof ApplyThunk<?, A> apply) {
            return curryApply(apply, ids);
        }

        if (core instanceof LambdaValue<?, ?> lambda) {
            // fixme...
            return (Term) curryLambda(lambda, ids);
        }

        throw new IllegalArgumentException("Unexpected core term " + term);
    }

    private static <A, B> Term<B> curryApply(ApplyThunk<A, B> apply, IdGen ids) {
        return new ApplyThunk<>(curry(apply.f(), ids), curry(apply.x(), ids));
    }

    private static <A, B> Term<F<A, B>> curryLambda(LambdaValue<A, B> lambda, IdGen ids) {
        var domain = lambda.domain();
        var f = lambda.f();

        var v = ids.<A>createId();
        var body = f.apply(new VarValue<>(domain, v));

        var curriedBody = curry(body, ids);

        if (curriedBody instanceof CurriedLambdaValue<B> curriedLambdaValue) {
            var expr = curriedLambdaValue.body();
            return new CurriedLambdaValue<>(new CurriedLambdaValue.LambdaBody<>(domain, x -> expr.substitute(v, x)));
        }

        return new CurriedLambdaValue<>(new CurriedLambdaValue.LambdaBody<>(domain, x -> new CurriedLambdaValue.MainBody<>(body.substitute(v, x))));
    }
}