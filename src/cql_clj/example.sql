WITH doctor_vs AS (

  select code from codes where system = '...' 

), asthma_vs AS (

  select code from codes where system = '...' 

), population AS (

  SELECT patient.*
  FROM Patient
  WHERE
        age_in_years_at(?, (resource->>'birthDate')::timestamp) >= 2
    and age_in_years_at(?, (resource->>'birthDate')::timestamp) <= 17

), astma_encounters AS (

  SELECT e.*
    FROM Encounter e
    JOIN population p ON reference(e, '{patient}', p)
   WHERE
          resource->>'class' == 'inpatient'
      and resource->>'length' <= 120 -- days
      and extract(e, '{hospitalization,discargeDisposition}', 'token') ilike  '%HomeDischarge%'
      and extract(e, '{period,end}') <@ '[2010-01-01 14:30, 2010-01-01 15:30)'::tsrange

), AS encounters_with_plan (

  SELECT e.*
    FROM astma_encounters e
    JOIN population p p ON reference(e, '{patient}', p)
    JOIN CommunicationRequest cr ON reference(cr, '{encounter}', e)
  WHERE
         extract(cr, 'reason', code) in (select code from astma_vs)
     and extract(cr, 'role', code) in (select code from doctor_vs)
     and extract(cr, 'recipient') = p.id
)
SELECT (select count(*) from astma_encounters)  as Denominator,
       (select count(*) from encounters_with_plan) as Numerator,
       (select count(*) from population) as Population;

