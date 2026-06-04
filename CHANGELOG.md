# Changelog

All notable changes to this project will be documented in this file. The changes should be categorized under one of these sections: Added, Changed, Deprecated, Removed, Fixed or Security.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## 1.0.3-[Unreleased]

## [1.0.2]-202606040709-16-c8ea62f

### Changed

- Removed debug output showing result map like `|[{"id" : 32, "createdAt" : "2026-04-28T07:40:27...|`.
- Support variables as part of a GraphQL operation.
- Support nested lookups, in the style of hasura.
- Support for related tables in TableAndConditionService.
- Transform DSL tree to string such that regelwerk can call $replace better and improve performance.

## [1.0.1]-202511241700-5-9af7191

### Added

- Support variables of type array

## [1.0.0]-202510030859-26-e043661

[Artifacts](https://nexus3.inventage.com/#browse/browse:inventage-projectware-maven:com%2Finventage%2Fgraphql%2Fbyos%2F1.0.0-202510030859-26-e043661)

- Initial release

[Unreleased]: https://github.com/inventage/byos/compare/1.0.0...main
[1.0.0]: https://github.com/inventage/byos/compare/00da796...1.0.0
[1.0.1]: https://github.com/inventage/byos/releases/tag/1.0.1
[1.0.2]: https://github.com/inventage/byos/releases/tag/1.0.2
