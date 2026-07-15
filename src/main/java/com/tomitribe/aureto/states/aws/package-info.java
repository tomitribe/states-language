/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
/**
 * AWS's definitions of the runtime values the States Language leaves to
 * the interpreter: the Context Object and friends.  These are the shapes
 * a task's code reads at execution time, as opposed to the document model
 * one package up, which is the shape a state machine is written in.
 */
@JsonbVisibility(ModelVisibility.class)
package com.tomitribe.aureto.states.aws;

import com.tomitribe.aureto.states.jsonb.ModelVisibility;
import jakarta.json.bind.annotation.JsonbVisibility;
