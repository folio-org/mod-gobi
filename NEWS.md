## 2.2.0 Unreleased

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
 
