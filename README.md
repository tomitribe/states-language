# States Language

A Java model, builder API, and validator for the [Amazon States Language](https://states-language.net/spec.html) —
the JSON language behind AWS Step Functions — targeting the **JSONata dialect** introduced in the
November 2024 revision of the specification.

Read state machines, write state machines, round-trip them without losing a field, and catch
mistakes at build time instead of deploy time.

```java
final TaskState chargeCard = TaskState.builder()
        .resource("arn:aws:lambda:us-east-1:123456789012:function:ChargeCard")
        .arguments(Arguments.builder()
                .expression("orderId", "$states.input.orderId")
                .value("currency", "USD")
                .build())
        .retrier(Retrier.LAMBDA_TRANSIENT)
        .catcher(Catcher.builder()
                .error(Errors.States.ALL)
                .output(Output.expression("$merge([$states.input, {'error': $states.errorOutput}])"))
                .next("Refund")
                .build())
        .next("Ship")
        .build();

final TaskState ship = TaskState.builder()
        .resource("arn:aws:lambda:us-east-1:123456789012:function:Ship")
        .end(true)
        .build();

final TaskState refund = TaskState.builder()
        .resource("arn:aws:lambda:us-east-1:123456789012:function:Refund")
        .end(true)
        .build();

final StateMachine machine = StateMachine.builder()
        .comment("Charge the card, ship the order, refund on any failure")
        .queryLanguage("JSONata")
        .startAt("Charge Card")
        .states(Map.of(
                "Charge Card", chargeCard,
                "Ship", ship,
                "Refund", refund))
        .build();

machine.validate().requireValid();

final String json = JsonbBuilder.create().toJson(machine);
```

Which produces:

```json
{
  "Comment": "Charge the card, ship the order, refund on any failure",
  "QueryLanguage": "JSONata",
  "StartAt": "Charge Card",
  "States": {
    "Charge Card": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:ChargeCard",
      "Arguments": {
        "orderId": "{% $states.input.orderId %}",
        "currency": "USD"
      },
      "Retry": [
        {
          "ErrorEquals": [
            "Lambda.ClientExecutionTimeoutException",
            "Lambda.ServiceException",
            "Lambda.AWSLambdaException",
            "Lambda.SdkClientException",
            "Lambda.TooManyRequestsException"
          ],
          "IntervalSeconds": 2,
          "MaxAttempts": 6,
          "BackoffRate": 2.0
        }
      ],
      "Catch": [
        {
          "ErrorEquals": ["States.ALL"],
          "Output": "{% $merge([$states.input, {'error': $states.errorOutput}]) %}",
          "Next": "Refund"
        }
      ],
      "Next": "Ship"
    },
    "Ship": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:Ship",
      "End": true
    },
    "Refund": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:Refund",
      "End": true
    }
  }
}
```

Reading works the same way in reverse — any JSON-B `Jsonb` instance will do:

```java
final StateMachine machine = jsonb.fromJson(json, StateMachine.class);
machine.validate().requireValid();
```

Every example in this README is compiled and asserted by
[`ReadmeTest`](src/test/java/org/tomitribe/aws/states/ReadmeTest.java).

## Background

The AWS SDK for Java v1 shipped a
[`StepFunctionBuilder`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/stepfunctions/builder/StepFunctionBuilder.html)
for authoring state machines in Java. The v1 SDK reached end-of-support on December 31, 2025, and the
builder was never ported to v2 — [aws/aws-sdk-java-v2#40](https://github.com/aws/aws-sdk-java-v2/issues/40)
is closed as not planned. The v1 builder also predates most of the modern language: the JSONata dialect,
`QueryLanguage`, variables and `Assign`, `ItemProcessor`/`ItemSelector`/`ItemReader`/`ResultWriter`/`ItemBatcher`,
tolerated-failure thresholds, `MaxDelaySeconds` and `JitterStrategy`.

This library is a fresh implementation of the current specification for plain Java: immutable records,
fluent builders, JSON-B marshalling, and no dependency on any deployment framework. If you are all-in on
CDK, use CDK — its Step Functions constructs are excellent. If you want to construct, parse, inspect, or
validate ASL documents as ordinary Java objects, that is what this is for.

## What you get

- **The full JSONata-dialect model** — all eight state types, `Arguments`/`Output`/`Condition`/`Items`/
  `ItemSelector`/`BatchInput`, variables via `Assign`, `Retry`/`Catch`, the complete Map state including
  distributed-map fields, and the AWS `Context` object (`$states.context`) shape.
- **Round-trip fidelity** — documents deserialize into records and serialize back without gaining or
  losing a field. The test suite round-trips every document it touches.
- **Expressions as a first-class concept** — every field the spec defines as "a JSON value *or* a JSONata
  string" is a typed union (`ArgumentsObject` | `ArgumentsExpression`, `ConditionBoolean` |
  `ConditionExpression`, ...). Expression text is held *bare*; the `{% %}` delimiters exist only in the
  JSON. Builders guard both directions:

  ```java
  Assign.builder().value("total", "{% $sum($states.input.items.price) %}")
  // IllegalArgumentException: Value for variable "total" looks like a JSONata expression:
  //   "{% $sum($states.input.items.price) %}".
  //   Use expression("total", "$sum($states.input.items.price)") instead
  ```

- **Spec rules enforced at construction** — the specification's single-object MUSTs are simply
  unrepresentable: a state with neither `Next` nor `End: true`, a `Wait` with both `Seconds` and
  `Timestamp`, a negative `MaxConcurrency`, a `BackoffRate` below 1.0, `States.ALL` anywhere but alone
  and last, a variable named `states`, a state name over 80 characters. Every failure message says what
  rule broke and how to fix it.
- **Whole-document validation** — `machine.validate()` checks what only the complete machine can:
  `StartAt` and every transition target resolve within their own scope, state names are unique across
  the entire machine, inner scopes don't shadow outer variables, the effective query language is
  JSONata. Warnings flag legal-but-suspect documents: unreachable states, a `Choice` without a
  `Default`, duplicate error names in a `Retry` list.

  ```text
  StartAt: "StartAt": "Charge Card" does not match any state in States.  States present: Charge
  States.Charge: "Next": "Shipp" does not match any state.  States present in this scope: Charge
  ```

- **A typo-proof error-name catalog** — `Errors` holds all 74 predefined names in four namespaces:
  the spec's Appendix A (`Errors.States.*`), the HTTP Task family (`Errors.States.Http.*`), the Lambda
  invocation errors including the full Invoke API catalog (`Errors.Lambda.*`), and the runtime sandbox
  (`Errors.Sandbox.TIMEDOUT`) — each documented with its meaning, retryability, and source. The javadoc
  also records the matching rules that surprise people: `ErrorEquals` matching is exact-string (no
  exception hierarchies), `States.ALL` cannot catch `States.Runtime` or `States.DataLimitExceeded`, and
  on AWS `States.TaskFailed` acts as a wildcard for everything except `States.Timeout`.
- **AWS's recommended Lambda retry policy as a constant** — `Retrier.LAMBDA_TRANSIENT` encodes the
  documented transient-exception policy (plus `Lambda.TooManyRequestsException`); being immutable it
  doubles as a template: `Retrier.LAMBDA_TRANSIENT.toBuilder().maxAttempts(3).build()`.

## Getting it

```xml
<dependency>
  <groupId>org.tomitribe.aws.states</groupId>
  <artifactId>states-language</artifactId>
  <version>0.3-SNAPSHOT</version>
</dependency>
```

Until the first release lands on Maven Central, build from source: `mvn install`.

Requires Java 17+. The library depends on the Jakarta JSON-P and JSON-B APIs; bring any JSON-B 3.0
implementation at runtime. The test suite runs against Apache Johnzon.

## Scope

This library implements the **JSONata dialect** of the States Language. The JSONPath dialect — `InputPath`,
`Parameters`, `ResultSelector`, `ResultPath`, `OutputPath`, intrinsic functions, and path-based Choice
rules — is intentionally not modeled. New machines should prefer JSONata, and AWS's own console does.

The specification this implements is the November 22, 2024 revision, included in this repository as
[`spec.html`](spec.html) under its own permissive license. Where the model encodes AWS-specific behavior
beyond the spec — error names, the Context object, retry guidance — the javadoc marks it as AWS-defined
and cites the AWS documentation it came from.

## License

[Apache License 2.0](LICENSE)
