## 3.1.0 - Unreleased

## 3.0.2 - (Sunflower R1 2025 Hot Hix)
The primary focus of the release was to update to RMB 35.4.2 and Vertx 4.5.23

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v3.0.1...v3.0.2)

### Bug fixes
* [MODGOBI-239] (https://folio-org.atlassian.net/browse/MODGOBI-239) - Sunflower CSP - RMB 35.4.2 Vertx 4.5.23 fixing CVE-2025-67735 netty CRLF
* [MODGOBI-237] (https://folio-org.atlassian.net/browse/MODGOBI-237) - Sunflower CSP - RMB Logging release

## 3.0.1 - (Sunflower R1 2025 Hot Hix)
The primary focus of this release was to fix issues with populating tenantId for non ECS envs.

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v3.0.0...v3.0.1)

### Bug fixes
* [MODGOBI-232] (https://folio-org.atlassian.net/browse/MODGOBI-232) - Skip populating tenantId from Gobi when not necessary

## 3.0.0 - (Sunflower R1 2025)
The primary focus of this release was to simplify mappings and do dependencies upgrade including Java 21

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v2.9.0...v3.0.0)

### Stories
* [MODBOGI-211] (https://folio-org.atlassian.net/browse/MODGOBI-211) - Invalid entry in mapping causes process to try to use default mapping which generally has errors
* [FOLIO-4208] (https://folio-org.atlassian.net/browse/FOLIO-4208) - Update to mod-gobi Java 21

### Dependencies
* Bump `java` from `17` to `21`
* Bump `raml` from `35.3.0` to `35.4.0`
* Bump `vertx` from `4.5.10` to `4.5.13`
* Bump `log4j` from `2.24.1` to `2.24.3`
* Bump `rest-assured` from `5.5.0` to `5.5.1`

## 2.9.0 - Released (Ramsons R2 2024)
The primary focus of this release was to update location translation and dependencies

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v2.8.0...v2.9.0)

### Breaking changes
Introduced new permissions for the module instead of `gobi.item.post`:
* `gobi.validate.item.get` for GET `/gobi/validate`
* `gobi.validate.item.post` for POST `/gobi/validate`
* `gobi.orders.item.post` for POST `/gobi/orders`

Before (v2.8.0):
* `gobi.item.post` for GET `/gobi/validate`
* `gobi.item.post` for POST `/gobi/validate`
* `gobi.item.post` for POST `/gobi/orders`

### Stories
* [MODBOGI-214] (https://folio-org.atlassian.net/browse/MODGOBI-214) - Update libraries of dependant acq modules to the latest versions
* [MODBOGI-212] (https://folio-org.atlassian.net/browse/MODGOBI-212) - Review and cleanup Module Descriptors for mod-gobi
* [MODGOBI-209] (https://folio-org.atlassian.net/browse/MODGOBI-209) - Update location translation for central ordering

### Dependencies
* Bump `raml` from `35.2.0` to `35.3.0`
* Added `folio-module-descriptor-validator` version `1.0.0`


## 2.8.0 - Released (Quesnelia R1 2024)
The primary focus of this release was to update custom mappings loading and library versions  

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v2.7.0...v2.8.0)

### Stories
* [MODGOBI-200] (https://folio-org.atlassian.net/browse/MODGOBI-200) - Add possibility to work with empty vendorDetails object
* [MODBOGI-203] (https://folio-org.atlassian.net/browse/MODGOBI-203) - Update GOBI settings to include Quesnelia functional updates

### Bug Fixes
* [MODGOBI-206] (https://folio-org.atlassian.net/browse/MODGOBI-206) - Fix endpoint to load custom mappings

### Dependencies
* Bump `raml` from `35.0.1` to `35.2.0`
* Bump `vertx` from `4.3.4` to `4.5.4`

## 2.7.0 Released (Poppy R2 2023)

Major changes in this release related to fixing bugs relate to mapping and update java version to 17

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.6.0...v2.7.0)

### Stories
* [MODGOBI-192](https://issues.folio.org/browse/MODGOBI-192) updated to java-17
* [MODGOBI-191](https://issues.folio.org/browse/MODGOBI-191) Use GitHub Workflows api-lint and api-schema-lint and api-doc
* [MODGOBI-187](https://issues.folio.org/browse/MODGOBI-187) Update dependent raml-util

### Bug fixes
* [MODGOBI-195](https://issues.folio.org/browse/MODGOBI-195) invalid entry in mapping causes process to try to use default mapping
* [MODGOBI-194](https://issues.folio.org/browse/MODGOBI-194) Acquisition units mapping by name has empty result

### Dependencies

* Bump `java version` from `11` to `17`

## 2.6.0 Released (Orchid R1 2023)
Major changes in this release related to updating mapping logic and improvement Logging

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.5.0...v2.6.0)

### Stories
* [MODGOBI-186](https://issues.folio.org/browse/MODGOBI-186) Remove 'Lookup mock' translator from gobi configuration
* [MODGOBI-178](https://issues.folio.org/browse/MODGOBI-178) Logging improvement - Configuration
* [MODGOBI-146](https://issues.folio.org/browse/MODGOBI-146) Logging improvement

### Bug fixes
* [MODGOBI-188](https://issues.folio.org/browse/MODGOBI-188) Organization lookup hardcoded
* [MODGOBI-183](https://issues.folio.org/browse/MODGOBI-183) Mapper.multiply ignores a single value


## 2.5.0 - Nolana R3 2022
Major changes in this release related to implementing GOBI integration user interface and replacing VertxCompletableFuture usage

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.4.0...v2.5.0)

### Stories
* [MODGOBI-161] (https://issues.folio.org/browse/MODGOBI-161) Define and implement new APIs for retrieving Fields, Translators and mapping Types
* [MODGOBI-160] (https://issues.folio.org/browse/MODGOBI-160) Create and update the schemas needed for the fields and translators mapping APIs
* [MODGOBI-176] (https://issues.folio.org/browse/MODGOBI-176) Replace FolioVertxCompletableFuture usage
* [MODGOBI-175] (https://issues.folio.org/browse/MODGOBI-175) mod-gobi: Upgrade RAML Module Builder
* [MODGOBI-157] (https://issues.folio.org/browse/MODGOBI-157) Define and implement API for retrieving GOBI mapping configs


## 2.4.0 Released
Major changes in this release related to removing usage of VertxCompletableFuture, adding few PO/POL fields to GOBI configuration 

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.3.0...v2.4.0)

### Stories
* [MODGOBI-154] (https://issues.folio.org/browse/MODGOBI-154) Remove usage of VertxCompletableFuture
* [MODGOBI-152] (https://issues.folio.org/browse/MODGOBI-152) Add additional PO/POL fields to GOBI configuration mapping
* [MODGOBI-132] (https://issues.folio.org/browse/MODGOBI-132) Adjust GOBI mapping for subaccount to perform lookup
* [MODGOBI-129] (https://issues.folio.org/browse/MODGOBI-129) Add acquisition unit and internal note fields to GOBI configuration mapping

### Bug fixes
* [MODGOBI-163] (https://issues.folio.org/browse/MODGOBI-163) Fail to create GOBI order for fundId
* [MODGOBI-150] (https://issues.folio.org/browse/MODGOBI-150) Fix asynchronous CompletableFuture flows in the DataSourceResolver


## 2.3.0 Released

Major changes in this release related to allow mapping for expense classes

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.2.0...v2.3.0)

### Stories
* [MODGOBI-149] (https://issues.folio.org/browse/MODGOBI-149) Material Supplier not mapping correctly to GOBI organization type
* [MODGOBI-139] (https://issues.folio.org/browse/MODGOBI-139) Support expense classes from LocalData fields

### Bug fixes
* [MODGOBI-144] (https://issues.folio.org/browse/MODGOBI-144) Can not process order with material type in the LocalData1 field


## 2.2.0 Released
Major changes in this release related to add mapping od expense classes

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.1.0...v2.2.0)

### Stories
* [MODGOBI-125] (https://issues.folio.org/browse/MODGOBI-125) Support expense classes from LocalData fields
* [MODGOBI-123] (https://issues.folio.org/browse/MODGOBI-123) Allow mapping for expense classes through GOBI


## 2.1.0 Released
Major changes in this release related with upgrading the RMB to v33.0.0

[Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v2.0.0...v2.1.0)

### Technical tasks
* [MODGOBI-122] (https://issues.folio.org/browse/MODGOBI-122) Update RMB v33.0.0

## 2.0.0 Released
Major changes in this release related with upgrading the RMB to v31.1.3

  [Full Changelog] (https://github.com/folio-org/mod-gobi/compare/v1.11.1...v1.12.0)  
    
### Technical tasks
 * [MODGOBI-117] (https://issues.folio.org/browse/MODGOBI-117) Update RMB    
 
### Stories  
 * [MODGOBI-110] (https://issues.folio.org/browse/MODGOBI-110) Add POL tags from incoming GOBI data
 * [MODGOBI-100] (https://issues.folio.org/browse/MODGOBI-100) Update the logic of checking the availability of orders and acq-module


## 1.11.1 Released
The primary focus of this release was to fix logging issues and fundCode mappings

  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.11.0...v1.11.1)
  
### Bug Fixes
 * [MODGOBI-113](https://issues.folio.org/browse/MODGOBI-113) No logging in honeysuckle version
 * [MODGOBI-109](https://issues.folio.org/browse/MODGOBI-109) Issue with Fund Code mapping from GOBI


## 1.11.0 Released
Major changes in this release related with upgrading the RMB to v31.1.1 and migrating to JDK 11 

  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.10.0...v1.11.0)
  
### Stories  
 * [MODGOBI-107](https://issues.folio.org/browse/MODGOBI-107) Update RMB v31.1.1
 * [MODGOBI-104](https://issues.folio.org/browse/MODGOBI-104) Migrate mod-gobi to JDK 11


## 1.10.0 Released
Major changes in this release related with upgrading the RMB to v30.0.1
  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.9.0...v1.10.0)
  
### Stories  
 * [MODGOBI-102](https://issues.folio.org/browse/MODGOBI-102) mod-gobi: Update to RMB v30.0.1
## 1.9.0 Released
Major changes in this release related with schema changes for Subscription/renewal and use finance business module.
  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.8.0...v1.9.0)
  
### Stories  
 * [MODGOBI-97](https://issues.folio.org/browse/MODGOBI-97) Subscription/renewal schema changes
 * [MODGOBI-81](https://issues.folio.org/browse/MODGOBI-81) Use finance business logic module
 
## 1.8.0 Released
 Major changes in this release related to updating RMB fixing the memory leak and security issues, updating fund 
 distribution schema. 
  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.7.0...v1.8.0)
  
### Stories  
 * [MODGOBI-96](https://issues.folio.org/browse/MODGOBI-96) Update RMB to 29.0.1
 * [MODGOBI-95](https://issues.folio.org/browse/MODGOBI-95) Use JVM features to manage container memory
 * [MODGOBI-87](https://issues.folio.org/browse/MODGOBI-87) Fund Distribution schema changes
 * [MODGOBI-86](https://issues.folio.org/browse/MODGOBI-86) Separate ISBN and qualifier
 * [FOLIO-2235](https://issues.folio.org/browse/FOLIO-2235) Add LaunchDescriptor settings to each backend non-core module repository
  

## 1.7.0 Released
 Major changes in this release include applying the latest schema changes related to funds and acquisitions units.

  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.6.0...v1.7.0)

### Stories
 * [MODFISTO-19](https://issues.folio.org/browse/MODFISTO-19) - Update fund schema
 * [MODORDERS-290](https://issues.folio.org/browse/MODORDERS-290) - Adding approve permission
 * [MODORDSTOR-103](https://issues.folio.org/browse/MODORDSTOR-103) - Orders schema updates
 
## 1.6.0 Released
 Major changes in this release include aligning code with latest schema changes related to owner being removed, making Source an enum and tags. 
 Also changing the contributor name type to UUID.

  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.5.0...v1.6.0)

### Stories
 * [MODGOBI-79](https://issues.folio.org/browse/MODGOBI-79) - updates to poLine.source
 * [MODGOBI-78](https://issues.folio.org/browse/MODGOBI-78) - Orders schema changes: purchase-order.owner is removed
 * [MODGOBI-77](https://issues.folio.org/browse/MODGOBI-77) - Contributor-name-type is required for contributor added to POL

## 1.5.0 Released
   Major changes in this release include aligning code with latest schema changes related to looking up UUID for productID type, fund type. 
   Also handled prevention of duplicate orders by looking for unique YBP  Order number before placing an order

  [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.4.0...v1.5.0)

### Stories
 * [MODGOBI-74](https://issues.folio.org/browse/MODGOBI-74) - acq-model updates
 * [MODGOBI-73](https://issues.folio.org/browse/MODGOBI-73) - PO Line: product id type is uuid
 * [MODGOBI-72](https://issues.folio.org/browse/MODGOBI-72) - Prevent duplicate orders
 * [MODGOBI-71](https://issues.folio.org/browse/MODGOBI-71) - "Owner" field is on PO level

## 1.4.0 Released
 Changes in this release include increasing flexibility with inventory interaction by including flags for CreateInventory, specifying inidividual material 
 types for physical and eresources and switching to mod-organization-storage from mod-vendors
 
 [Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.3.0...v1.4.0)
 
### Stories
 * [MODGOBI-69](https://issues.folio.org/browse/MODGOBI-69) - Adjusting required fields according to CompositePurchase Order schema
 * [MODGOBI-67](https://issues.folio.org/browse/MODGOBI-67) - Switch to mod-organizations-storage
 * [MODGOBI-63](https://issues.folio.org/browse/MODGOBI-63) - Ability to specifiy material type for physical and resource
 * [MODGOBI-61](https://issues.folio.org/browse/MODGOBI-61) - Increased flexibility in inventory integration- adding createInventory flag

## 1.3.0 Released
 Major changes in this release deal with aligning the code with new schema changes to acq-models and handling mandatory fields like material-types
 and locations which are required creating orders with inventory interaction.
 
[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.2.1...v1.3.0)
 
### Stories
 * [MODGOBI-59](https://issues.folio.org/browse/MODGOBI-59) - Remove Adjustments and add new fields from Cost
 * [MODGOBI-57](https://issues.folio.org/browse/MODGOBI-57) - Add a default material-type "unspecified" if the request doesnot provide one
 * [MODGOBI-55](https://issues.folio.org/browse/MODGOBI-55) - Refactor the code to align with new composite_purchase_order schema

### Bugs
 * [MODGOBI-60](https://issues.folio.org/browse/MODGOBI-60) - Default Currency to USD if one is not specified
 * [MODGOBI-54](https://issues.folio.org/browse/MODGOBI-54) - Use the location specified in the request
 * [MODGOBI-52](https://issues.folio.org/browse/MODGOBI-52) - Add workflow_status mapping for ListedElectronicMonograph
 


## 1.2.1 Released
[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.1.0...v1.2.1)

### Stories
 * [MODGOBI-49](https://issues.folio.org/browse/MODGOBI-49) Sonar cloud security fix
 * [MODGOBI-48](https://issues.folio.org/browse/MODGOBI-48) Make location repeatable
 * [MODGOBI-46](https://issues.folio.org/browse/MODGOBI-46) Property name change in composite_purchase_order.json schema
 * [MODGOBI-42](https://issues.folio.org/browse/MODGOBI-42) Mapping Remaining fields for default mapping

## 1.2.0 Unreleased
 * Ignore this release to conform with vendor-storage.vendors interface version 1.0 and other ui modules
 
## 1.1.0 Released

[Full Changelog](https://github.com/folio-org/mod-gobi/compare/v1.0.1...v1.1.0)

### Stories
 * [MODGOBI-43](https://issues.folio.org/browse/MODGOBI-43) Adjusting to new acq models with Renewal changes
 * Updating to use mod-orders Interface 2.0
 
## 1.0.1 (Released 12/03/2018)
First release of mod-gobi implemeting the gobi business logic

### Stories
 * [MODGOBI-39](https://issues.folio.org/browse/MODGOBI-39) - Use the latest GOBI schema 
 * [MODGOBI-36](https://issues.folio.org/browse/MODGOBI-36) - cache/reuse Mapping POJO 
 * [MODGOBI-33](https://issues.folio.org/browse/MODGOBI-33) - Integrate with Latest acquisition schemas 
 * [MODGOBI-30](https://issues.folio.org/browse/MODGOBI-30) - Ability to have default/fallback configurations 
 * [MODGOBI-24](https://issues.folio.org/browse/MODGOBI-24) - Integrate with mod-configuration
 * [MODGOBI-23](https://issues.folio.org/browse/MODGOBI-23) - Add support for combining multiple fields
 * [MODGOBI-15](https://issues.folio.org/browse/MODGOBI-15) - Map common fields from GOBI to FOLIO
 * [MODGOBI-14](https://issues.folio.org/browse/MODGOBI-14) - update GOBI schema
 * [MODGOBI-9](https://issues.folio.org/browse/MODGOBI-9) - Look up UUID
 * [MODGOBI-8](https://issues.folio.org/browse/MODGOBI-8) - Retrieve Mapping Configurations from Mod-configuration 
 * [MODGOBI-7](https://issues.folio.org/browse/MODGOBI-7) - Integrate with mod-orders
 * [MODGOBI-3](https://issues.folio.org/browse/MODGOBI-3) - Data Translation
 * [MODGOBI-2](https://issues.folio.org/browse/MODGOBI-2) - Define API 
 * Added the GOBI `PurchaseOrder.xsd` for validation and code generation.
   Currently, we cannot use the XSD file in the RAML file due to RMB using
   a JSON unmarshaller instead of XML. The "model" code is generated by a
   JAXB maven plugin.
 * Added the acq-models submodule and generate "model" code for the 
   necessary schemas, namely CompositePurchaseOrder and CompositePoLine.
   This code is generated by the jsonschema2pojo maven plugin due to a
   limitation of RMB that doesn't support generation of pojos for schemas
   that aren't consumed or produced by a RAML endpoint.
 * Updated the test data w/ actual requests placed by GOBI in the qa env 
   during BAT.
 * Initial work
 
### Bugs
 * [MODGOBI-38](https://issues.folio.org/browse/MODGOBI-38) - Mapping corrections for tenant-specific configuration
 * [MODGOBI-27](https://issues.folio.org/browse/MODGOBI-27) - cql queries should be using '=='
 * [MODGOBI-28](https://issues.folio.org/browse/MODGOBI-28) - Requires `locations` interface 2.1 or 3.0
 * [MODGOBI-26](https://issues.folio.org/browse/MODGOBI-26) - Response must contain PO Line Number
 * [MODGOBI-17](https://issues.folio.org/browse/MODGOBI-17) - Pass okapi Headers to request
 
