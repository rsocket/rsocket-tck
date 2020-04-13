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

## Testing Client-Server Implementations

![](https://www.websequencediagrams.com/files/render?link=wg2HRdSALIB370a8r8Gymyf6V9wXFj6xFwfnV3IenlNP9mFadX5ZztaRBxkS298j)

## Testing Client Only Implementations

![](https://www.websequencediagrams.com/files/render?link=2ULQtZY1gDjOeuVlezjuWNFzxfUhQehUMEfSx4kqXJadK3RZgjpXYC4UvsqR3W9p)
