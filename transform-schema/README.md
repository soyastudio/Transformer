## XSD to AVSC Conversion

### Basic Types Conversion

| XSD Type              | Avro Type          | 
| --------------------- | ------------------ | 
| xs:boolean            | boolean            |
| xs:base64Binary       | bytes              |
| xs:hexBinary          | bytes              |
| xs:anyURI             | string             |
| xs:QName              | string             |
| xs:NOTATION           | string             |
| xs:float              | float              |
| xs:double             | double             |
| xs:decimal            | double             |
| xs:string             | string             |
| xs:duration           | string             |
| xs:dateTime           | string             |
| xs:time               | string             |
| xs:date               | string             |
| xs:gYearMonth         | string             |
| xs:gYear              | string             |
| xs:gMonthDay          | string             |
| xs:gDay               | string             |
| xs:gMonth             | string             |
| xs:integer            | int                |
| xs:long               | long               |
| xs:int                | int                |
| xs:short              | int                |
| xs:byte               | int                |
| xs:nonPositiveInteger | int                |
| xs:NegativeInteger    | int                |
| xs:nonNegativeInteger | int                |
| xs:positiveInteger    | int                |
| xs:unsignedLong       | long               |
| xs:unsignedInt        | int                |
| xs:unsignedShort      | int                |
| xs:unsignedByte       | int                |
| xs:normalizedString   | string             |
| xs:token              | string             |
| xs:language           | string             |
| xs:NCName             | string             |
| xs:ID                 | string             |
| xs:IDREF              | string             |
| xs:IDREFS             | string             |
| xs:ENTITY             | string             |
| xs:ENTITIES           | string             |
| xs:NMTOKEN            | string             |
| xs:NMTOKENS           | string             |

### Complex Types Conversion

- XS Complex Type maps to Avro Schema record type;
- XS cardinality "minOccur=0" maps to Avro union type;
- Xs cardinality "maxOccur=unbounded" or "maxOccur=n" (n > 1) maps to Avro array type

