package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.runtime.FunValue;
import com.sstewartgallus.term.Id;
import com.sstewartgallus.term.IdGen;
import com.sstewartgallus.type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface TPass0<X> {
    static <T> TPass0<T> from(Type<T> type, IdGen vars) {
        return type.visit(new Type.Visitor<>() {
            @Override
            public TPass0<T> onPureType(Class<T> clazz) {
                return new TPass0.PureType<>(clazz);
            }

            @Override
            public TPass0<T> onLoadType(Id<T> variable) {
                return new Load<>(variable);
            }

            @Override
            public <A, B> TPass0<T> onFunctionType(Equality<T, F<A, B>> equality, Type<A> domain, Type<B> range) {
                // fixme...
                return (TPass0) new TPass0.FunType<>(TPass0.from(domain, vars), TPass0.from(range, vars));
            }
        });
    }

    default List<Class<?>> flatten() {
        return List.of(erase());
    }

    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <A> Signature<A, X> pointFree(Id<A> v, IdGen vars) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <T> TPass0<X> substitute(Id<T> v, TPass0<T> replacement) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    enum NilType implements TPass0<HList.Nil> {
        NIL;

        @Override
        public List<Class<?>> flatten() {
            return List.of(Void.class);
        }

        @Override
        public <X> Signature<X, HList.Nil> pointFree(Id<X> v, IdGen vars) {
            return new Signature.NilTPass0<>();
        }

        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    record FunType<A, B>(TPass0<A>domain, TPass0<B>range) implements TPass0<F<A, B>> {
        @Override
        public <X> Signature<X, F<A, B>> pointFree(Id<X> v, IdGen vars) {
            return new Signature.Function<>(domain.pointFree(v, vars), range.pointFree(v, vars));
        }

        @Override
        public <Z> TPass0<F<A, B>> substitute(Id<Z> v, TPass0<Z> replacement) {
            return new FunType<>(domain.substitute(v, replacement), range.substitute(v, replacement));
        }

        // fixme... consider just using object as we use a generic protocol for our functions
        @Override
        public Class<?> erase() {
            return FunValue.class;
        }

        @Override
        public String toString() {
            return "{" + domain + " → " + range + "}";
        }
    }

    // fixme... rename/retype, not clear enough this creates a new type...
    record PureType<A>(Class<A>clazz) implements TPass0<A> {
        public <T> Signature<T, A> pointFree(Id<T> argument, IdGen vars) {
            return new Signature.Pure<>(clazz);
        }

        public <Z> TPass0<A> substitute(Id<Z> v, TPass0<Z> replacement) {
            return new PureType<>(clazz);
        }

        @Override
        public Class<?> erase() {
            return clazz;
        }

        @Override
        public String toString() {
            return erase().getCanonicalName();
        }
    }

    record First<A, B>(TPass0<E<A, B>>value) implements TPass0<A> {
        public <L> Signature<L, A> pointFree(Id<L> argument, IdGen vars) {
            return new Signature.First<>(value.pointFree(argument, vars));
        }

        public <Z> TPass0<A> substitute(Id<Z> v, TPass0<Z> replacement) {
            return new First<>(value.substitute(v, replacement));
        }
    }

    record Second<A, B>(TPass0<E<A, B>>value) implements TPass0<B> {
        public <L> Signature<L, B> pointFree(Id<L> argument, IdGen vars) {
            return new Signature.Second<>(value.pointFree(argument, vars));
        }

        public <Z> TPass0<B> substitute(Id<Z> v, TPass0<Z> replacement) {
            return new Second<>(value.substitute(v, replacement));
        }
    }

    record Forall<A, B>(Function<TPass0<A>, TPass0<B>>f) implements TPass0<V<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public <T> Signature<T, V<A, B>> pointFree(Id<T> argument, IdGen vars) {
            /* TVar<E<A, T>> newVar = vars.createTPass0Var();

            var body = f.apply(new First<>(newVar))
                    .substitute(argument, new Second<>(newVar))
                    .pointFree(newVar, vars);
            return Signature.curry(body); */
            throw null;
        }

        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var t = new Id<A>(depth);
                str = "{forall " + t + ". " + f.apply(new Load<>(t)) + "}";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
        }
    }

    record Exists<A, B>(TPass0<A>x, TPass0<B>y) implements TPass0<E<A, B>> {
        @Override
        public String toString() {
            return "{exists " + x + ". " + y + "}";
        }
    }

    record Load<T>(Id<T>variable) implements TPass0<T> {
        @Override
        public String toString() {
            return variable.toString();
        }

        @Override
        public <V> Signature<V, T> pointFree(Id<V> argument, IdGen vars) {
            if (argument == variable) {
                return (Signature<V, T>) new Signature.Identity<T>();
            }
            throw new IllegalStateException("wrong variable " + argument + " " + this);
        }

        @Override
        public <Z> TPass0<T> substitute(Id<Z> v, TPass0<Z> replacement) {
            if (v == variable) {
                return (TPass0<T>) replacement;
            }
            return this;
        }
    }

    record ConsType<H, T extends HList<T>>(TPass0<H>head, TPass0<T>tail) implements TPass0<HList.Cons<H, T>> {
        @Override
        public Class<?> erase() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public List<Class<?>> flatten() {
            var l = new ArrayList<Class<?>>();
            l.add(head.erase());

            TPass0<?> current = tail;
            while (current instanceof ConsType<?, ?> cons) {
                l.add(cons.head.erase());
                current = cons.tail;
            }
            return l;
        }

        @Override
        public <X> Signature<X, HList.Cons<H, T>> pointFree(Id<X> v, IdGen vars) {
            return new Signature.ConsTPass0<>(head.pointFree(v, vars), tail.pointFree(v, vars));
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            builder.append("(");
            builder.append(head);

            TPass0<? extends HList<?>> current = tail;
            while (current instanceof ConsType<?, ?> cons) {
                builder.append(" Δ ");
                builder.append(cons.head);
                current = cons.tail;
            }
            builder.append(" Δ .)");
            return builder.toString();
        }
    }
}
