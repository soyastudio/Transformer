## XSD to AVSC Conversion

### Basic Types Conversion

| XSD Type              | Avro Type          | Json Type          |
| --------------------- | ------------------ | ------------------ |
| xs:boolean            | boolean            | boolean            |
| xs:base64Binary       | bytes              | string             |
| xs:hexBinary          | bytes              | string             |
| xs:anyURI             | string             | string             |
| xs:QName              | string             | string             |
| xs:NOTATION           | string             | string             |
| xs:float              | float              | number             |
| xs:double             | double             | number             |
| xs:decimal            | double             | number             |
| xs:string             | string             | string             |
| xs:duration           | string             | string             |
| xs:dateTime           | string             | string             |
| xs:time               | string             | string             |
| xs:date               | string             | string             |
| xs:gYearMonth         | string             | string             |
| xs:gYear              | string             | string             |
| xs:gMonthDay          | string             | string             |
| xs:gDay               | string             | string             |
| xs:gMonth             | string             | string             |
| xs:integer            | int                | number             |
| xs:long               | long               | number             |
| xs:int                | int                | number             |
| xs:short              | int                | number             |
| xs:byte               | int                | number             |
| xs:nonPositiveInteger | int                | number             |
| xs:NegativeInteger    | int                | number             |
| xs:nonNegativeInteger | int                | number             |
| xs:positiveInteger    | int                | number             |
| xs:unsignedLong       | long               | number             |
| xs:unsignedInt        | int                | number             |
| xs:unsignedShort      | int                | number             |
| xs:unsignedByte       | int                | number             |
| xs:normalizedString   | string             | string             |
| xs:token              | string             | string             |
| xs:language           | string             | string             |
| xs:NCName             | string             | string             |
| xs:ID                 | string             | string             |
| xs:IDREF              | string             | string             |
| xs:IDREFS             | string             | string             |
| xs:ENTITY             | string             | string             |
| xs:ENTITIES           | string             | string             |
| xs:NMTOKEN            | string             | string             |
| xs:NMTOKENS           | string             | string             |

### Complex Types Conversion

- XS Complex Type maps to Avro Schema record type;
- XS cardinality "minOccur=0" maps to Avro union type;
- Xs cardinality "maxOccur=unbounded" or "maxOccur=n" (n > 1) maps to Avro array type

