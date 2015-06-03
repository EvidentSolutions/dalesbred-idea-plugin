## 0.3.1 (2015-06-03)

- Handle comments in SQL statements when counting placeholders.

## 0.3.0 (2015-05-14)

- Support for Dalesbred 1.0 (which changed package to org.dalesbred).
  Old 0.x-versions are still supported as well.

## 0.2.9 (2015-04-02)

- Improvements in CTE parsing.

## 0.2.8 (2015-04-02)

- Support for CTE column definitions.

## 0.2.7 (2015-04-02)

- Support analyzing common table expressions.

## 0.2.6 (2015-03-27)

- Support @DalesbredIgnore introduced by Dalesbred 0.7.1.

## 0.2.5 (2014-10-28)

- Fixed infinite loop when validating find-calls of primitive types.
- Fixed adding allowed types for instantiation problems -inspection.
 
## 0.2.4 (2014-07-07)

- Normalize quoted alias-names when analyzing SQL-queries.

## 0.2.3 (2014-06-26)

- Support analyzing SQL statements with newlines.
- Support analyzing new updateAndProcessGeneratedKeys.

## 0.2.2 (2014-05-26)

- Fixed target bytecode version so that plugin works with JDK 1.6.

## 0.2.1 (2014-05-23)

- Fixed bugs in select list parsing:
    - argument-separating commas in function calls were confused with select-list item separators.
    - commas inside quoted strings were confused with select-list item separators. 

## 0.2.0 (2014-01-19)

- Produce warnings about uninitialized properties.

## 0.1.1 (2013-12-12)

- Improvements in parsing select-list.

## 0.1.0 (2013-11-03)

- Support verifying calls to update-methods.

## 0.0.8 (2013-10-23)

- Fixed exception which was thrown when SQL was invalid.

## 0.0.7 (2013-10-18)

- Fixed spurious enum-instantiation warnings.
- Support parsing SQL-statements without FOR.

## 0.0.6 (2013-10-16)

- Use same resolution rules for property names as Dalesbred uses, causing fewer false warnings.

## 0.0.5 (2013-09-11)

- Support "insert/delete/update ... returning ..." for instantiation inspection.

## 0.0.4 (2013-09-16)

- Fixed ArrayIndexOutOfBoundsException when not enough parameters we passed.

## 0.0.3 (2013-09-15)

- Cover more of Dalesbred's API in inspections.

## 0.0.2 (2013-09-15)

- More thorough instantiation inspections.

## 0.0.1 (2013-09-14)

- Initial revision.
