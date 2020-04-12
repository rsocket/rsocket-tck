# Summary

The purpose of the RSocket Technology Compatibility Kit (from here on referred to as: the TCK) is to guide and help RSocket library implementers to validate their implementations against the rules defined in [the Protocol Specification](https://github.com/rsocket/rsocket).


# Detailed Design

The TCK designed to have two independent parts. 
The first one is a test runner in the form of a CLI tool along with a form of extendable test suites. The second part is a driver, or simply saying a set of responses to logical interactions which allows ensuring protocol correctness. Please note, that the first part is well specified, where the second part lies on the implementors.

## Testing network communication

As RSocket implementation is a library that allows two applications talk to each
other reactively over the network TCK should support both `client` and `server`
compatibility tests. For that reason TCK should be a separate application that
can be run either as RSocket `client` or as RSocket `server`.

## Tests as specification

TCK should provide `DSL` language to allow programmatically describe cases from
protocol specification. Each case should have separate code section that allow
to validate an implementation in full and in case by case scenarios. Convenient 
`dsl` also allows describing specification in more precise way and catch any 
issues in specification itself.

## Test Reports

As the main goal of TCK development is to help engineers in implementing RSocket
protocol, the most critical feature of TCK is to well described reports on
which specification sections an implementation is not covering. Reports should
contain information about failures of not following the specification (e.g. 
precise frames ordering and format if states otherwise in spec)
