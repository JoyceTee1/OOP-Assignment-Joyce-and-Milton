package smartlib.generics;

import smartlib.domain.Book;
import smartlib.domain.Repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public final class TypeTokenRepositoryDemo {
    private TypeTokenRepositoryDemo() {
    }

    public static List<String> repositoryTypeArguments() {
        TypeToken<Repository<Book, String>> token = new TypeToken<>() {
        };
        Type type = token.getType();
        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException("Expected parameterized type for Repository<Book, String>");
        }
        return Arrays.stream(parameterizedType.getActualTypeArguments())
                .map(Type::getTypeName)
                .toList();
    }

    public static void printRepositoryTypeArguments() {
        List<String> args = repositoryTypeArguments();
        System.out.println("Repository type arguments: " + args);
    }
}
