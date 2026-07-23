/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.aws.states;

/**
 * The predefined Error Names usable in "ErrorEquals", one nested namespace
 * per name prefix: {@link States} for the States Language specification's
 * Appendix A set plus the names AWS defines beyond the spec, {@link Lambda}
 * for the Lambda invocation errors, and {@link Sandbox} for the Lambda
 * runtime sandbox errors.
 *
 * Error name matching is exact and case-sensitive — there is no hierarchy
 * and no wildcarding beyond {@link States#ALL} — so these constants exist
 * to make typos unrepresentable.  Names an interpreter or task mints
 * itself MUST NOT begin with the "States." prefix.
 */
public interface Errors {

    /**
     * The "States." error names: the specification's Appendix A set, plus
     * two AWS defines beyond the spec, marked as such.
     *
     * @see <a href="https://states-language.net/spec.html#appendix-a">Appendix A: Predefined Error Codes</a>
     * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/concepts-error-handling.html">Handling errors in Step Functions workflows</a>
     */
    interface States {

        /**
         * A wildcard matching any Error Name.  MUST appear alone in its
         * "ErrorEquals" array and MUST be the last Retrier or Catcher.
         * On AWS, it does not match {@link #RUNTIME} or
         * {@link #DATA_LIMIT_EXCEEDED}, which always fail the execution.
         */
        String ALL = "States.ALL";

        /**
         * A Task State either ran longer than "TimeoutSeconds" or failed
         * to heartbeat for longer than "HeartbeatSeconds"
         */
        String TIMEOUT = "States.Timeout";

        /**
         * A Task State failed to heartbeat for longer than
         * "HeartbeatSeconds"
         */
        String HEARTBEAT_TIMEOUT = "States.HeartbeatTimeout";

        /**
         * A Task State failed during execution.  On AWS, when used in a
         * Retry or Catch this acts as a wildcard matching any known error
         * name except {@link #TIMEOUT} — a narrower net than {@link #ALL}.
         */
        String TASK_FAILED = "States.TaskFailed";

        /**
         * A Task State had insufficient privileges to execute the
         * specified code
         */
        String PERMISSIONS = "States.Permissions";

        /**
         * Query evaluation failed in a JSONata state: a type error, an
         * incorrectly typed result, or an undefined result
         */
        String QUERY_EVALUATION_ERROR = "States.QueryEvaluationError";

        /**
         * A branch of a Parallel State failed
         */
        String BRANCH_FAILED = "States.BranchFailed";

        /**
         * A Choice State matched no Choice Rule and has no "Default"
         */
        String NO_CHOICE_MATCHED = "States.NoChoiceMatched";

        /**
         * A Map State's failed items exceeded its tolerated failure
         * threshold
         */
        String EXCEED_TOLERATED_FAILURE_THRESHOLD = "States.ExceedToleratedFailureThreshold";

        /**
         * A Map State failed to read all items as specified by
         * "ItemReader"
         */
        String ITEM_READER_FAILED = "States.ItemReaderFailed";

        /**
         * A Map State failed to write all results as specified by
         * "ResultWriter"
         */
        String RESULT_WRITER_FAILED = "States.ResultWriterFailed";

        /**
         * A state's "ResultPath" could not be applied to the input it
         * received.  A JSONPath-dialect error; JSONata states have no
         * "ResultPath".
         */
        String RESULT_PATH_MATCH_FAILURE = "States.ResultPathMatchFailure";

        /**
         * Within a "Parameters" field, replacing a field whose name ends
         * in ".$" using a Path failed.  A JSONPath-dialect error; JSONata
         * states have no "Parameters".
         */
        String PARAMETER_PATH_FAILURE = "States.ParameterPathFailure";

        /**
         * Within a Payload Template, invoking an Intrinsic Function
         * failed.  A JSONPath-dialect error; JSONata states have no
         * Intrinsic Functions.
         */
        String INTRINSIC_FAILURE = "States.IntrinsicFailure";

        /**
         * AWS-defined, not in the specification: the interpreter itself
         * hit an unprocessable exception.  Not retriable and not
         * catchable — a Retry or Catch on {@link #ALL} does not match it,
         * and the execution always fails.
         */
        String RUNTIME = "States.Runtime";

        /**
         * AWS-defined, not in the specification: a state's input or
         * output exceeded the 256KB payload quota.  {@link #ALL} does not
         * match it, but it can be retried or caught by naming it
         * explicitly in "ErrorEquals".
         */
        String DATA_LIMIT_EXCEEDED = "States.DataLimitExceeded";

        /**
         * The "States.Http." error names an HTTP Task reports when
         * calling third-party APIs.  AWS-defined, not in the
         * specification.
         *
         * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/call-https-apis.html">Call HTTPS APIs</a>
         */
        interface Http {

            /**
             * The HTTP task timed out after 60 seconds
             */
            String SOCKET = "States.Http.Socket";

            String STATUS_CODE_400 = "States.Http.StatusCode.400";
            String STATUS_CODE_401 = "States.Http.StatusCode.401";
            String STATUS_CODE_404 = "States.Http.StatusCode.404";
            String STATUS_CODE_409 = "States.Http.StatusCode.409";
            String STATUS_CODE_429 = "States.Http.StatusCode.429";
            String STATUS_CODE_500 = "States.Http.StatusCode.500";
            String STATUS_CODE_502 = "States.Http.StatusCode.502";
            String STATUS_CODE_503 = "States.Http.StatusCode.503";
            String STATUS_CODE_504 = "States.Http.StatusCode.504";

            /**
             * The error name for any HTTP status, following the
             * "States.Http.StatusCode.&lt;code&gt;" pattern the documented
             * constants use
             */
            static String statusCode(final int code) {
                return "States.Http.StatusCode." + code;
            }
        }
    }

    /**
     * The "Lambda." error names.  Step Functions reports Lambda errors as
     * {@code Lambda.<ErrorName>}, and the names come from two layers: the
     * Lambda Invoke API's documented errors, and the AWS SDK client layer
     * Step Functions calls through, whose members are marked as such
     * below.  AWS's recommended Retrier for the transient family —
     * {@link #CLIENT_EXECUTION_TIMEOUT_EXCEPTION},
     * {@link #SERVICE_EXCEPTION}, {@link #AWS_LAMBDA_EXCEPTION},
     * {@link #SDK_CLIENT_EXCEPTION} — is IntervalSeconds 2, MaxAttempts 6,
     * BackoffRate 2.
     *
     * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/sfn-best-practices.html#bp-lambda-serviceexception">Handle transient Lambda service exceptions</a>
     * @see <a href="https://docs.aws.amazon.com/lambda/latest/api/API_Invoke.html#API_Invoke_Errors">Lambda Invoke API Errors</a>
     * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/concepts-error-handling.html">Handling errors in Step Functions workflows</a>
     */
    interface Lambda {

        /**
         * The Lambda service reported a general exception through the
         * AWS SDK client layer; transient, retry recommended.  An SDK
         * exception named in the Step Functions guidance rather than the
         * Invoke API.
         */
        String AWS_LAMBDA_EXCEPTION = "Lambda.AWSLambdaException";

        /**
         * The client-side call to the Lambda service timed out;
         * transient, retry recommended.  An SDK exception named in the
         * Step Functions guidance rather than the Invoke API.
         */
        String CLIENT_EXECUTION_TIMEOUT_EXCEPTION = "Lambda.ClientExecutionTimeoutException";

        /**
         * The function's code artifact user was deleted; wait for Lambda
         * to provision a new one or update the function's code package.
         * HTTP 409.
         */
        String CODE_ARTIFACT_USER_DELETED_EXCEPTION = "Lambda.CodeArtifactUserDeletedException";

        /**
         * Provisioning of the function's code artifact user failed;
         * update the function's code package or check its State and
         * StateReasonCode.  HTTP 409.
         */
        String CODE_ARTIFACT_USER_FAILED_EXCEPTION = "Lambda.CodeArtifactUserFailedException";

        /**
         * The function's code artifact user is still provisioning;
         * transient, wait for the function to become Active.  HTTP 409.
         */
        String CODE_ARTIFACT_USER_PENDING_EXCEPTION = "Lambda.CodeArtifactUserPendingException";

        /**
         * A durable execution with the given name and a different payload
         * already started; an idempotency conflict.  HTTP 409.
         */
        String DURABLE_EXECUTION_ALREADY_STARTED_EXCEPTION = "Lambda.DurableExecutionAlreadyStartedException";

        /**
         * Additional permissions are needed to configure the function's
         * VPC settings; configuration, retrying does not help.  HTTP 502.
         */
        String EC2_ACCESS_DENIED_EXCEPTION = "Lambda.EC2AccessDeniedException";

        /**
         * Amazon EC2 throttled Lambda during function initialization;
         * transient.  HTTP 502.
         */
        String EC2_THROTTLED_EXCEPTION = "Lambda.EC2ThrottledException";

        /**
         * Lambda received an unexpected EC2 client exception setting up
         * the function; transient.  HTTP 502.
         */
        String EC2_UNEXPECTED_EXCEPTION = "Lambda.EC2UnexpectedException";

        /**
         * Reading from or writing to the function's connected file
         * system failed.  HTTP 410.
         */
        String EFS_IO_EXCEPTION = "Lambda.EFSIOException";

        /**
         * The function could not make a network connection to its
         * configured file system; transient.  HTTP 408.
         */
        String EFS_MOUNT_CONNECTIVITY_EXCEPTION = "Lambda.EFSMountConnectivityException";

        /**
         * The function could not mount its configured file system due to
         * a permission or configuration issue; retrying does not help.
         * HTTP 403.
         */
        String EFS_MOUNT_FAILURE_EXCEPTION = "Lambda.EFSMountFailureException";

        /**
         * The file system mount operation timed out; transient.
         * HTTP 408.
         */
        String EFS_MOUNT_TIMEOUT_EXCEPTION = "Lambda.EFSMountTimeoutException";

        /**
         * The VPC's elastic network interface limit was reached; a
         * quota, retrying does not help until capacity frees.  HTTP 502.
         */
        String ENI_LIMIT_REACHED_EXCEPTION = "Lambda.ENILimitReachedException";

        /**
         * The elastic network interface for the function's VPC
         * connection is not ready yet; transient, wait and retry.
         * HTTP 502.
         */
        String ENI_NOT_READY_EXCEPTION = "Lambda.ENINotReadyException";

        /**
         * A request parameter is not valid; configuration.  HTTP 400.
         */
        String INVALID_PARAMETER_VALUE_EXCEPTION = "Lambda.InvalidParameterValueException";

        /**
         * The request body could not be parsed as JSON or a header is
         * invalid; configuration.  HTTP 400.
         */
        String INVALID_REQUEST_CONTENT_EXCEPTION = "Lambda.InvalidRequestContentException";

        /**
         * The function's runtime or runtime version is not supported;
         * configuration.  HTTP 502.
         */
        String INVALID_RUNTIME_EXCEPTION = "Lambda.InvalidRuntimeException";

        /**
         * The security group in the function's VPC configuration is not
         * valid; configuration.  HTTP 502.
         */
        String INVALID_SECURITY_GROUP_ID_EXCEPTION = "Lambda.InvalidSecurityGroupIDException";

        /**
         * The subnet in the function's VPC configuration is not valid;
         * configuration.  HTTP 502.
         */
        String INVALID_SUBNET_ID_EXCEPTION = "Lambda.InvalidSubnetIDException";

        /**
         * Lambda could not unzip the function's deployment package; a
         * bad deploy, retrying does not help.  HTTP 502.
         */
        String INVALID_ZIP_FILE_EXCEPTION = "Lambda.InvalidZipFileException";

        /**
         * Environment variable decryption was denied by AWS KMS; check
         * the function's KMS permissions.  HTTP 502.
         */
        String KMS_ACCESS_DENIED_EXCEPTION = "Lambda.KMSAccessDeniedException";

        /**
         * The KMS key for the function's environment variables is
         * disabled; configuration.  HTTP 502.
         */
        String KMS_DISABLED_EXCEPTION = "Lambda.KMSDisabledException";

        /**
         * The KMS key's state is not valid for Decrypt; configuration.
         * HTTP 502.
         */
        String KMS_INVALID_STATE_EXCEPTION = "Lambda.KMSInvalidStateException";

        /**
         * The KMS key for the function's environment variables was not
         * found; configuration.  HTTP 502.
         */
        String KMS_NOT_FOUND_EXCEPTION = "Lambda.KMSNotFoundException";

        /**
         * The function does not support the requested invocation mode,
         * for example a synchronous Invoke of an asynchronous-only
         * function; configuration.  HTTP 400.
         */
        String MODE_NOT_SUPPORTED_EXCEPTION = "Lambda.ModeNotSupportedException";

        /**
         * The function has no published versions; configuration, seen
         * when a qualifier requires one.  HTTP 400.
         */
        String NO_PUBLISHED_VERSION_EXCEPTION = "Lambda.NoPublishedVersionException";

        /**
         * Lambda detected the function in a recursive invocation loop
         * with other AWS resources and stopped it.  HTTP 400.
         */
        String RECURSIVE_INVOCATION_EXCEPTION = "Lambda.RecursiveInvocationException";

        /**
         * The request payload exceeded the Invoke quota of 6 MB.  Step
         * Functions' own 256KB payload quota
         * ({@link States#DATA_LIMIT_EXCEEDED}) triggers first, so this is
         * unlikely to surface through a state machine.  HTTP 413.
         */
        String REQUEST_TOO_LARGE_EXCEPTION = "Lambda.RequestTooLargeException";

        /**
         * The resource already exists or another operation is in
         * progress, for example invoking during a function update;
         * transient.  HTTP 409.
         */
        String RESOURCE_CONFLICT_EXCEPTION = "Lambda.ResourceConflictException";

        /**
         * The function does not exist — deleted, or the name is wrong;
         * configuration.  HTTP 404.
         */
        String RESOURCE_NOT_FOUND_EXCEPTION = "Lambda.ResourceNotFoundException";

        /**
         * The function is inactive and its VPC connection is no longer
         * available; transient, wait for the connection to reestablish.
         * HTTP 502.
         */
        String RESOURCE_NOT_READY_EXCEPTION = "Lambda.ResourceNotReadyException";

        /**
         * The function could not connect to its configured S3 Files
         * access point; transient.  HTTP 408.
         */
        String S3_FILES_MOUNT_CONNECTIVITY_EXCEPTION = "Lambda.S3FilesMountConnectivityException";

        /**
         * The function could not mount its configured S3 Files access
         * point due to a permission or configuration issue; retrying
         * does not help.  HTTP 403.
         */
        String S3_FILES_MOUNT_FAILURE_EXCEPTION = "Lambda.S3FilesMountFailureException";

        /**
         * The S3 Files access point mount operation timed out;
         * transient.  HTTP 408.
         */
        String S3_FILES_MOUNT_TIMEOUT_EXCEPTION = "Lambda.S3FilesMountTimeoutException";

        /**
         * The SDK client failed calling the Lambda service, for example
         * a connection failure; transient, retry recommended.  An SDK
         * exception named in the Step Functions guidance rather than the
         * Invoke API.
         */
        String SDK_CLIENT_EXCEPTION = "Lambda.SdkClientException";

        /**
         * The request payload exceeded the maximum size for serialized
         * request entities.  HTTP 413.
         */
        String SERIALIZED_REQUEST_ENTITY_TOO_LARGE_EXCEPTION = "Lambda.SerializedRequestEntityTooLargeException";

        /**
         * The Lambda service encountered an internal error; transient,
         * retry recommended.  HTTP 500.
         */
        String SERVICE_EXCEPTION = "Lambda.ServiceException";

        /**
         * The request would exceed a Lambda service quota; needs a quota
         * increase, retrying does not help.  HTTP 402.
         */
        String SERVICE_QUOTA_EXCEEDED_EXCEPTION = "Lambda.ServiceQuotaExceededException";

        /**
         * The function's afterRestore() SnapStart runtime hook failed;
         * a function bug, check the CloudWatch logs.  HTTP 400.
         */
        String SNAP_START_EXCEPTION = "Lambda.SnapStartException";

        /**
         * Lambda is still initializing the SnapStart function;
         * transient, invoke when the function state becomes Active.
         * HTTP 409.
         */
        String SNAP_START_NOT_READY_EXCEPTION = "Lambda.SnapStartNotReadyException";

        /**
         * Lambda could not regenerate the function's SnapStart snapshot;
         * wait for Lambda to retry or update the function's
         * configuration to trigger a new snapshot.  HTTP 409.
         */
        String SNAP_START_REGENERATION_FAILURE_EXCEPTION = "Lambda.SnapStartRegenerationFailureException";

        /**
         * Lambda could not restore the SnapStart snapshot within the
         * timeout; transient.  HTTP 408.
         */
        String SNAP_START_TIMEOUT_EXCEPTION = "Lambda.SnapStartTimeoutException";

        /**
         * A configured subnet has no available IP addresses for the
         * function's VPC access; capacity, retrying does not help until
         * addresses free.  HTTP 502.
         */
        String SUBNET_IP_ADDRESS_LIMIT_REACHED_EXCEPTION = "Lambda.SubnetIPAddressLimitReachedException";

        /**
         * The invocation was throttled; transient, retry recommended.
         * HTTP 429.
         */
        String TOO_MANY_REQUESTS_EXCEPTION = "Lambda.TooManyRequestsException";

        /**
         * The function failed without reporting structured error
         * information, for example a crash the runtime could not
         * describe.  Historically also how function timeouts surfaced;
         * newer runtimes report those as {@link Sandbox#TIMEDOUT}.
         */
        String UNKNOWN = "Lambda.Unknown";

        /**
         * The Invoke request body's content type is not JSON;
         * configuration.  HTTP 415.
         */
        String UNSUPPORTED_MEDIA_TYPE_EXCEPTION = "Lambda.UnsupportedMediaTypeException";
    }

    /**
     * The "Sandbox." error names the Lambda runtime sandbox reports.
     * AWS's error handling guidance recommends matching
     * {@link Lambda#UNKNOWN}, {@link Sandbox#TIMEDOUT}, and
     * {@link States#TASK_FAILED} together to cover the ways a function
     * can fail without structured error information.
     *
     * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/concepts-error-handling.html">Handling errors in Step Functions workflows</a>
     */
    interface Sandbox {

        /**
         * The function ran out of time; reported by newer Lambda
         * runtimes where older ones reported {@link Lambda#UNKNOWN}
         */
        String TIMEDOUT = "Sandbox.Timedout";
    }
}
