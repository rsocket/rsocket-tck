# RSocket TCK

[![Build Status](https://github.com/rsocket/rsocket-tck/workflows/Java%20CI/badge.svg)](https://github.com/rsocket/rsocket-tck/actions?query=workflow%3A%22Java+CI%22)

The goal of this project is to provide a reliable polyglot testing suite to 
verify both existing and upcoming versions of servers and clients that use the 
RSocket protocol.

## Notice

Previous scala implementation is in [master branch](https://github.com/rsocket/rsocket-tck/tree/master)

## Outline

# Summary

The purpose of the RSocket Technology Compatibility Kit (from here on referred
to as: the TCK) is to guide and help RSocket library implementers to validate
their implementations against the rules defined in
[the Protocol Specification](https://github.com/rsocket/rsocket).


# Detailed Design

The TCK designed to have two independent parts:

 * the first one is a test runner in the form of a CLI tool along with a form of
   extendable test suites
 * the second part is a driver, or simply saying a set of responses to logical
   interactions which allows ensuring protocol correctness.

Please note, that the first part is well specified, where the second part lies
on the implementors.
