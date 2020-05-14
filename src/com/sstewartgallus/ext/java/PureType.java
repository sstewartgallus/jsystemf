package com.sstewartgallus.ext.java;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.V;

// fixme... rename/retype, not clear enough this creates a new type...
// fixme... doesn't need to be core !
public record PureType<A>(Class<A>clazz) implements Type<A> {

    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        if (this != right) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    @Override
    public <Z> Type<A> substitute(Id<Z> v, Type<Z> replacement) {
        return new PureType<>(clazz);
    }

    @Override
    public <Z> Signature<V<Z, A>> pointFree(Id<Z> argument, IdGen vars) {
        return new Signature.K<>(new Signature.Pure<>(clazz));
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}