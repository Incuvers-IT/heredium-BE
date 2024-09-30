package art.heredium.domain.common.converter;

import art.heredium.domain.common.type.PersistableEnum;

import javax.persistence.AttributeConverter;

public class GenericTypeConverter<T extends Enum<T> & PersistableEnum<E>, E> implements AttributeConverter<T, E> {

    private final Class<T> cls;

    public GenericTypeConverter(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public E convertToDatabaseColumn(T attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public T convertToEntityAttribute(E dbData) {
        if (dbData == null) {
            return null;
        }

        T[] enums = cls.getEnumConstants();

        for (T e : enums) {
            if (e.getValue().equals(dbData)) {
                return e;
            }
        }
        throw new UnsupportedOperationException();
    }
}
