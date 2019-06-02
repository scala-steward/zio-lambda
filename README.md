# AWS Lambda Runtime with ZIO

Creates an [AWS Lambda custom runtime](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html) on top of ZIO.

## TODO

- add license
- tests
- runtime api
- error handling (timeouts, function errors, etc.)
- streaming request and response bodies
- logging to AWS Cloudwatch
- comonadic logging (https://hackage.haskell.org/package/co-log)