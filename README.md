# mod-gobi


Copyright (C) 2018-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

GOBI® (Global Online Bibliographic Information) is the leading web-based
acquisitions tool for finding, ordering and managing e-books and print books
for libraries. This module allows GOBI initiated orders to be fulfilled by
FOLIO.

## Additional information
MODGOBI currently can handle the below order Types:
1. Listed Electronic Monograph
2. Listed Electrnoic serial
3. Listed Print Monograph
4. Listed Print Serial
5. Unlisted Print Monograph
6. UnlistedPrint Serial

When any of the above order types come through GOBI, the order is mapped to corresponding schemas in https://github.com/folio-org/acq-models
and the Orders APIs are called.

## Database Support

Starting from version 3.1.0, mod-gobi includes built-in database support for storing custom order mappings. This replaces the previous dependency on mod-configuration.

### Database Schema

The module automatically creates and manages a PostgreSQL table (`order_mappings`) during tenant initialization. The table stores custom order mappings.

### Tenant API

The module implements the Tenant API (version 2.0) to handle:
- Database schema creation and initialization during module deployment
- Automatic migration of database schema when upgrading module versions
- Tenant-specific data isolation

### Custom Mappings Storage

Custom order mappings are now stored in the local database instead of using mod-configuration. This provides:
- Better performance through local database queries
- Improved data isolation per tenant
- Simplified deployment (no external configuration dependency)
- Support for database migrations and versioning

## Using custom order mapping
Please take a look wiki : [Using custom order mapping](https://wiki.folio.org/display/DD/GOBI+-+Overriding+default+Mapping)

**Note**: With the introduction of database support, custom mappings are now managed through the module's own database tables rather than mod-configuration. The API endpoints remain the same, ensuring backward compatibility.

## Environment Variables

For database connection, the following environment variables are used:

| Environment Variable | Description                                         | Default Value |
|----------------------|-----------------------------------------------------|---------------|
| `DB_HOST`            | The hostname or IP address of the database server.  | `localhost`   |
| `DB_PORT`            | The port number on which the database is listening. | `5432`        |
| `DB_USERNAME`        | The username for connecting to the database.        | `postgres`    |
| `DB_PASSWORD`        | The password for the specified username.            | `postgres`    |
| `DB_DATABASE`        | The name of the database to connect to.             | `postgres`    |

These variables are used to configure the connection to the PostgreSQL database.

### Issue tracker

See project [MODGOBI](https://issues.folio.org/browse/MODGOBI)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
