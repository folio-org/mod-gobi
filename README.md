# mod-gobi

Copyright (C) 2018 - 2019 The Open Library Foundation

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

### Issue tracker

See project [MODGOBI](https://issues.folio.org/browse/MODGOBI)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
