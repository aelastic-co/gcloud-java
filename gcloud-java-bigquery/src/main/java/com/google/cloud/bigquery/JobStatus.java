package com.google.cloud.bigquery;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A Google BigQuery Job status. Objects of this class can be examined when polling an asynchronous
 * job to see if the job completed.
 */
public class JobStatus implements Serializable {

  private static final long serialVersionUID = -714976456815445365L;

  /**
   * Possible states that a BigQuery Job can assume.
   */
  public enum State {
    /**
     * The BigQuery Job is waiting to be executed.
     */
    PENDING,

    /**
     * The BigQuery Job is being executed.
     */
    RUNNING,

    /**
     * The BigQuery Job has completed either succeeding or failing. If failed {@link #error()} will
     * be non-null.
     */
    DONE
  }

  private final State state;
  private final BigQueryError error;
  private final List<BigQueryError> executionErrors;

  JobStatus(State state) {
    this.state = state;
    this.error = null;
    this.executionErrors = null;
  }

  JobStatus(State state, BigQueryError error, List<BigQueryError> executionErrors) {
    this.state = state;
    this.error = error;
    this.executionErrors = executionErrors != null ? ImmutableList.copyOf(executionErrors) : null;
  }

  /**
   * Returns the state of the job. A {@link State#PENDING} job is waiting to be executed. A
   * {@link State#RUNNING} is being executed. A {@link State#DONE} job has completed either
   * succeeding or failing. If failed {@link #error()} will be non-null.
   */
  public State state() {
    return state;
  }

  /**
   * Returns the final error result of the job. If present, indicates that the job has completed
   * and was unsuccessful.
   *
   * @see <a href="https://cloud.google.com/bigquery/troubleshooting-errors">
   *     Troubleshooting Errors</a>
   */
  public BigQueryError error() {
    return error;
  }

  /**
   * Returns all errors encountered during the running of the job. Errors here do not necessarily
   * mean that the job has completed or was unsuccessful.
   *
   * @see <a href="https://cloud.google.com/bigquery/troubleshooting-errors">
   *     Troubleshooting Errors</a>
   */
  public List<BigQueryError> executionErrors() {
    return executionErrors;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("state", state)
        .add("error", error)
        .add("executionErrors", executionErrors)
        .toString();
  }

  @Override
  public final int hashCode() {
    return Objects.hash(state, error, executionErrors);
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this
        || obj != null
        && obj.getClass().equals(JobStatus.class)
        && Objects.equals(toPb(), ((JobStatus) obj).toPb());
  }

  com.google.api.services.bigquery.model.JobStatus toPb() {
    com.google.api.services.bigquery.model.JobStatus statusPb =
        new com.google.api.services.bigquery.model.JobStatus();
    if (state != null) {
      statusPb.setState(state.toString());
    }
    if (error != null) {
      statusPb.setErrorResult(error.toPb());
    }
    if (executionErrors != null) {
      statusPb.setErrors(Lists.transform(executionErrors, BigQueryError.TO_PB_FUNCTION));
    }
    return statusPb;
  }

  static JobStatus fromPb(com.google.api.services.bigquery.model.JobStatus statusPb) {
    List<BigQueryError> allErrors = null;
    if (statusPb.getErrors() != null) {
      allErrors = Lists.transform(statusPb.getErrors(), BigQueryError.FROM_PB_FUNCTION);
    }
    BigQueryError error =
        statusPb.getErrorResult() != null ? BigQueryError.fromPb(statusPb.getErrorResult()) : null;
    return new JobStatus(State.valueOf(statusPb.getState()), error, allErrors);
  }
}
