# RSocket TCK

[![Build Status](https://travis-ci.org/rsocket/rsocket-tck.svg?branch=master)](https://travis-ci.org/rsocket/rsocket-tck)

The goal of this project is to provide a reliable polyglot testing suite to 
verify both existing and upcoming versions of servers and clients that use the 
RSocket protocol.

## Notice

Previous scala implementation is in [master branch](https://github.com/rsocket/rsocket-tck/tree/master)

## Outline

The challenge of creating any test suite for network protocols is that we must 
have a wait for one side to behave according to how we expect, so we can check 
that the appropriate outputs are received given a certain input. Another 
challenge is that the tests must be polyglot (support multiple languages), 
as this network protocol can, and will be, implemented in various languages.
