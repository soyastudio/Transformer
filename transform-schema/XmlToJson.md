# XML to JSON Type Conversion

## Basic Type Mappings

| XSD Type              | Json Type          | Json Array (*)     |
| --------------------- | ------------------ | ------------------ |
| complex               | json object        | array              |
| xs:boolean            | boolean            | boolean_array      |
| xs:base64Binary       | string             | string_array       |
| xs:hexBinary          | string             | string_array       |
| xs:anyURI             | string             | string_array       |
| xs:QName              | string             | string_array       |
| xs:NOTATION           | string             | string_array       |
| xs:float              | float              | number_array       |
| xs:double             | number             | number_array       |
| xs:decimal            | number             | number_array       |
| xs:string             | string             | string_array       |
| xs:duration           | string             | string_array       |
| xs:dateTime           | string             | string_array       |
| xs:time               | string             | string_array       |
| xs:date               | string             | string_array       |
| xs:gYearMonth         | string             | string_array       |
| xs:gYear              | string             | string_array       |
| xs:gMonthDay          | string             | string_array       |
| xs:gDay               | string             | string_array       |
| xs:gMonth             | string             | string_array       |
| xs:integer            | number             | number_array       |
| xs:long               | number             | number_array       |
| xs:int                | number             | number_array       |
| xs:short              | number             | number_array       |
| xs:byte               | number             | number_array       |
| xs:nonPositiveInteger | number             | number_array       |
| xs:NegativeInteger    | number             | number_array       |
| xs:nonNegativeInteger | number             | number_array       |
| xs:positiveInteger    | number             | number_array       |
| xs:unsignedLong       | number             | number_array       |
| xs:unsignedInt        | number             | number_array       |
| xs:unsignedShort      | number             | number_array       |
| xs:unsignedByte       | number             | number_array       |
| xs:normalizedString   | string             | string_array       |
| xs:token              | string             | string_array       |
| xs:language           | string             | string_array       |
| xs:NCName             | string             | string_array       |
| xs:ID                 | string             | string_array       |
| xs:IDREF              | string             | string_array       |
| xs:IDREFS             | string             | string_array       |
| xs:ENTITY             | string             | string_array       |
| xs:ENTITIES           | string             | string_array       |
| xs:NMTOKEN            | string             | string_array       |
| xs:NMTOKENS           | string             | string_array       |

* Json Array Types Mapping Rule: cardinality(min-max) when max > 1

## Mapping Function Expression

- Single Complex Type and String Type do not need to indicate;
- Indication expression: {type_name}({xpath});

## Examples:

- boolean(GetGroceryOrder/GroceryOrderData/GroceryOrderHeader/CustomerRefund/CancellationEligibleInd);
- number(GetGroceryOrder/GroceryOrderData/GroceryOrderHeader/CustomerRefundCancellation/SequenceNbr)
- number(GetGroceryOrder/GroceryOrderData/GroceryOrderHeader/CustomerRefund/BalanceAmt);
- array(GetGroceryOrder/GroceryOrderData/GroceryOrderHeader/RetailCustomer/Contact)
- string_array(GetGroceryOrder/DocumentData/Document/Description);
