/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

import com.android.volley.exception.VolleyError;

/**
 * Retry policy for a request.
 *
 * <p>A retry policy can control two parameters:
 *
 * <ul>
 * <li>The number of tries. This can be a simple counter or more complex logic based on the type
 * of error passed to {@link #retry(VolleyError)}, although {@link #getCurrentRetryCount()}
 * should always return the current retry count for logging purposes.
 * <li>The request timeout for each try, via {@link #getCurrentTimeout()}. In the common case that
 * a request times out before the response has been received from the server, retrying again
 * with a longer timeout can increase the likelihood of success (at the expense of causing the
 * user to wait longer, especially if the request still fails).
 * </ul>
 *
 * <p>Note that currently, retries triggered by a retry policy are attempted immediately in sequence
 * with no delay between them (although the time between tries may increase if the requests are
 * timing out and {@link #getCurrentTimeout()} is returning increasing values).
 *
 * <p>By default, Volley uses {@link DefaultRetryPolicy}.
 */
public interface RetryPolicy
{
    /**
     * Returns the current timeout (used for logging).
     */
    int getCurrentTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     *
     * @param error The error code of the last attempt.
     * @throws VolleyError In the event that the retry could not be performed (for example if we ran
     *                     out of attempts), the passed in error is thrown.
     */
    void retry(VolleyError error) throws VolleyError;

    // --------------------------------------------------------------------------------------------------
    // - Impl
    // - Default retry policy for requests.
    // --------------------------------------------------------------------------------------------------
    class DefaultRetryPolicy implements RetryPolicy
    {
        /**
         * The current timeout in milliseconds.
         */
        private int mCurrentTimeoutMs;

        /**
         * The current retry count.
         */
        private int mCurrentRetryCount;

        /**
         * The maximum number of attempts.
         */
        private final int mMaxNumRetries;

        /**
         * The backoff multiplier for the policy.
         */
        private final float mBackoffMultiplier;

        /**
         * The default socket timeout in milliseconds
         */
        public static final int DEFAULT_TIMEOUT_MS = 2500;

        /**
         * The default number of retries
         */
        public static final int DEFAULT_MAX_RETRIES = 1;

        /**
         * The default backoff multiplier
         */
        public static final float DEFAULT_BACKOFF_MULTIPLIER = 1f;

        /**
         * Constructs a new retry policy using the default timeouts.
         */
        public DefaultRetryPolicy()
        {
            this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULTIPLIER);
        }

        /**
         * Constructs a new retry policy.
         *
         * @param initialTimeoutMs  The initial timeout for the policy.
         * @param maxNumRetries     The maximum number of retries.
         * @param backoffMultiplier Backoff multiplier for the policy.
         */
        public DefaultRetryPolicy(int initialTimeoutMs,
                                  int maxNumRetries,
                                  float backoffMultiplier)
        {
            mCurrentTimeoutMs = initialTimeoutMs;
            mMaxNumRetries = maxNumRetries;
            mBackoffMultiplier = backoffMultiplier;
        }

        /**
         * Returns the current timeout.
         */
        @Override
        public int getCurrentTimeout()
        {
            return mCurrentTimeoutMs;
        }

        /**
         * Returns the current retry count.
         */
        @Override
        public int getCurrentRetryCount()
        {
            return mCurrentRetryCount;
        }

        /**
         * Returns the backoff multiplier for the policy.
         */
        public float getBackoffMultiplier()
        {
            return mBackoffMultiplier;
        }

        /**
         * Prepares for the next retry by applying a backoff to the timeout.
         *
         * @param error The error code of the last attempt.
         */
        @Override
        public void retry(VolleyError error) throws VolleyError
        {
            mCurrentRetryCount++;
            mCurrentTimeoutMs += (int) (mCurrentTimeoutMs * mBackoffMultiplier);
            if (!hasAttemptRemaining())
            {
                throw error;
            }
        }

        /**
         * Returns true if this policy has attempts remaining, false otherwise.
         */
        protected boolean hasAttemptRemaining()
        {
            return mCurrentRetryCount <= mMaxNumRetries;
        }
    }
}
