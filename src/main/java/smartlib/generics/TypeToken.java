package smartlib.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {
    private final Type type;

    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException("TypeToken must be created with generic type information");
        }
        this.type = parameterizedType.getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
