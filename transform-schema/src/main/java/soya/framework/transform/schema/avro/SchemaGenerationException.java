package soya.framework.transform.schema.avro;

import org.apache.avro.AvroRuntimeException;

public class SchemaGenerationException extends AvroRuntimeException {

    public SchemaGenerationException(Throwable cause) {
        super(cause);
    }

    public SchemaGenerationException(String message) {
        super(message);
    }

    public SchemaGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

}
