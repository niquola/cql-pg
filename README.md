# cql/elm in postgresql

* Data could be saved in pg using jsonb and preserving model format (aka fhirbase for FHIR).
* Most of cql rules could be expressed as views and/or CTE and/or queries in postgresql.
* Terminology could be  part of database
* Some dynamic code could be done by plv8/js


So the result could be CQL database, which could efficiently eval CQL rules.

Here is example:

```cql
using QUICK

valueset "Asthma": '2.16.840.1.113883.3.117.1.7.1.271'
valueset "Asthma Management Plan": '2.16.840.1.113883.3.117.1.7.1.131'
valueset "Home Discharge Disposition": 'TBD'
valueset "Doctor": 'TBD'

parameter MeasurementPeriod default Interval[DateTime(2013, 1, 1, 0, 0, 0, 0), DateTime(2014, 1, 1, 0, 0, 0, 0))

context Patient

define "In Demographic":
  AgeInYearsAt(start of MeasurementPeriod) >= 2 and AgeInYearsAt(start of MeasurementPeriod) <= 17

define "Asthma Encounters":
  ["Encounter": "Asthma"] E
    where E."class" = 'inpatient'
      and E."length" <= 120 days
      and E."hospitalization"."dischargeDisposition" in "Home Discharge Disposition"
      and E."period" ends during MeasurementPeriod

define "Asthma Encounters with Plan":
  "Asthma Encounters" E
    with ["CommunicationRequest"] C
      such that (C."encounter" as "Encounter")."id" = E."id"
        and exists ((C."reason") R where R in "Asthma Management Plan")
        and exists (((C."sender" as "Practitioner")."practitionerRole") R where R."role" in "Doctor")
        and (First(C."recipient") as "Patient")."id" = (C."subject" as "Patient")."id"

define "In Initial Patient Population":
    "In Demographic" and exists ("Asthma Encounters")

context Population

define "Denominator": "Asthma Encounters"

define "Numerator": "Asthma Encounters with Plan"
```

which could be compiled into CTE


```sql

WITH doctor_vs AS (

  select code from codes where system = '<oid>'  and code in ('<code1>')

), asthma_vs AS (

  select code from codes where system = '<oid>'  and code in ('<code1>')

), population AS (

  SELECT patient.*
  FROM Patient
  WHERE age_in_years_at(?, (resource->>'birthDate')::timestamp) >= 2
    and age_in_years_at(?, (resource->>'birthDate')::timestamp) <= 17

), astma_encounters AS (

  SELECT e.*
    FROM Encounter e
    JOIN population p ON reference(e, '{patient}') = p.id
   WHERE resource->>'class' == 'inpatient'
     AND resource->>'length' <= 120 -- days
     AND extract(e, '{hospitalization,discargeDisposition}', 'token') ilike  '%HomeDischarge%'
     AND extract(e, '{period,end}', 'date') <@ '[2010-01-01 14:30, 2010-01-01 15:30)'::tsrange

), AS encounters_with_plan (

  SELECT e.*
    FROM astma_encounters e
    JOIN population p p ON reference(e, '{patient}', p)
    JOIN CommunicationRequest cr ON reference(cr, '{encounter}', e)
   WHERE extract(cr, 'reason', 'code') in (select code from astma_vs)
     AND extract(cr, 'role', 'code') in (select code from doctor_vs)
     AND extract(cr, 'recipient', 'reference') = p.id
)

SELECT (select count(*) from astma_encounters)  as Denominator,
       (select count(*) from encounters_with_plan) as Numerator,
       (select count(*) from population) as Population;


```

Libraries could be implemented as schemas & views:

```sql

CREATE VIEW <module_schema>.astma_encounters AS (

  SELECT e.*
    FROM Encounter e
   WHERE resource->>'class' == 'inpatient'
     AND resource->>'length' <= 120 -- days
     AND extract(e, '{hospitalization,discargeDisposition}', 'token') ilike  '%HomeDischarge%'
     AND extract(e, '{period,end}', 'date') <@ '[2010-01-01 14:30, 2010-01-01 15:30)'::tsrange

);

```

## License

Copyright © 2016 niquola

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
