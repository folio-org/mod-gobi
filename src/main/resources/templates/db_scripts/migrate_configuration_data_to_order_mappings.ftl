<#if mode.name() == "UPDATE">

INSERT INTO ${myuniversity}_mod_gobi.order_mappings (id, jsonb)
SELECT
  cd.id,
  jsonb_build_object(
    'mappings', ((cd.jsonb->>'value')::jsonb)->'mappings',
    'orderType', ((cd.jsonb->>'value')::jsonb)->'orderType'
  )
FROM
  ${myuniversity}_mod_configuration.config_data AS cd
WHERE
  cd.jsonb->>'module' = 'GOBI' AND
  cd.jsonb->>'code' IN ('gobi.order.ListedElectronicMonograph', 'gobi.order.ListedPrintMonograph', 'gobi.order.UnlistedPrintMonograph')
  AND NOT EXISTS (
    SELECT 1
    FROM ${myuniversity}_mod_gobi.order_mappings AS om
    WHERE om.jsonb->>'orderType' = ((cd.jsonb->>'value')::jsonb)->>'orderType'
  );

</#if>
