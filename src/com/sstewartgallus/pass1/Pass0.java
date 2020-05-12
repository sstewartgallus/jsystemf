package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public interface Pass0<L> {
    static <T> Pass0<T> from(Term<T> term, IdGen vars) {
        if (term instanceof PureValue<T> pure) {
            return new Pure<T>(TPass0.from(pure.type(), vars), pure.value());
        }

        if (term instanceof VarThunk<T> load) {
            return new Var<>(TPass0.from(load.type(), vars), load.variable());
        }

        if (term instanceof ApplyThunk<?, T> apply) {
            return fromApply(apply, vars);
        }

        if (term instanceof MonoLambdaValue<?, ?> lambda) {
            // fixme... penguin
            return (Pass0) fromLambda(lambda, vars);
        }
        throw new IllegalArgumentException("unexpected term " + term);
    }

    static <A, B> Pass0<? super F<A, B>> fromLambda(MonoLambdaValue<A, B> lambda, IdGen vars) {
        var domain = lambda.domain();
        var f = lambda.f();
        var d0 = TPass0.from(domain, vars);
        var v = vars.<A>createId();
        var body = from(f.apply(new VarThunk<>(domain, v)), vars);
        return new Pass0.Lambda<>(d0, x -> body.substitute(v, x));
    }

    static <A, B> Pass0<B> fromApply(ApplyThunk<A, B> apply, IdGen vars) {
        return new Apply<>(from(apply.f(), vars), from(apply.x(), vars));
    }

    Pass1<L> aggregateLambdas(IdGen vars);

    TPass0<L> type();

    default <X> Pass0<L> substitute(Id<X> variable, Pass0<X> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Apply<A, B>(Pass0<F<A, B>>f, Pass0<A>x) implements Pass0<B> {
        @Override
        public TPass0<B> type() {
            var funTPass0 = ((TPass0.FunType<A, B>) f.type());
            var t = x.type();
            if (!Objects.equals(t, funTPass0.domain())) {
                throw new RuntimeException("TPass0 error");
            }
            return funTPass0.range();
        }

        @Override
        public String toString() {
            return "(" + f + " " + x + ")";
        }

        @Override
        public <X> Pass0<B> substitute(Id<X> variable, Pass0<X> replacement) {
            return new Apply<>(f.substitute(variable, replacement), x.substitute(variable, replacement));
        }

        @Override
        public Pass1<B> aggregateLambdas(IdGen vars) {
            return new Pass1.Apply<>(f.aggregateLambdas(vars), x.aggregateLambdas(vars));
        }
    }

    record Lambda<A, B>(TPass0<A>domain, Function<Pass0<A>, Pass0<B>>f) implements Pass0<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        @Override
        public <X> Pass0<F<A, B>> substitute(Id<X> variable, Pass0<X> replacement) {
            return new Lambda<>(domain, x -> f.apply(x).substitute(variable, replacement));
        }

        public Pass1<F<A, B>> aggregateLambdas(IdGen vars) {
            var v = vars.<A>createId();
            var body = f.apply(new Var<>(domain, v)).aggregateLambdas(vars);

            if (body instanceof Pass1.Thunk<B> thunk) {
                var expr = thunk.body();
                return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> expr.substitute(v, x)));
            }

            return new Pass1.Thunk<>(new Pass1.Lambda<>(domain, x -> new Pass1.Expr<>(body.substitute(v, x))));
        }

        public TPass0<F<A, B>> type() {
            var range = f.apply(new Var<>(domain, new Id<>(0))).type();
            return new TPass0.FunType<>(domain, range);
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new Var<>(domain, new Id<>(depth));
                var body = f.apply(dummy);
                String bodyStr = body.toString();

                str = "({" + dummy + ": " + domain + "} → " + bodyStr + ")";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }
}