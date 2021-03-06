/*
 * Copyright (C) 2019. Uber Technologies
 *
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
package com.uber.lintchecks.android

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement

/**
 * Detector to check for usages of ResourcesCompat.getColor
 */
class ResCompatGetColorDetector : Detector(), SourceCodeScanner {

  companion object {
    private const val ISSUE_ID = "ResCompatGetColorUsage"
    const val LINT_ERROR_MESSAGE = "Don't use ResourcesCompat#getColor(Resources,int,Theme), instead use ContextCompat.getColor(Context,int)"
    @JvmField
    val ISSUE = Issue.create(
        ISSUE_ID,
        "Use ContextCompat.getColor(Context, int)",
        LINT_ERROR_MESSAGE,
        Category.CORRECTNESS,
        6,
        Severity.ERROR,
        createImplementation<ResCompatGetColorDetector>())
  }

  override fun createUastHandler(context: JavaContext): UElementHandler {
    return object : UElementHandler() {
      override fun visitCallExpression(node: UCallExpression) {
        if (!getApplicableMethodNames().contains(node.methodName)) return

        if (node.methodName == "getColor" && isCalledOnResCompat(context.evaluator, node)) {
          context.report(ISSUE, context.getLocation(node), LINT_ERROR_MESSAGE)
        }
      }
    }
  }

  private fun isCalledOnResCompat(evaluator: JavaEvaluator, node: UCallExpression): Boolean {
    return evaluator.isMemberInClass(node.resolve(), "androidx.core.content.res.ResourcesCompat")
  }

  override fun getApplicableUastTypes(): List<Class<out UElement>>? {
    return listOf(UCallExpression::class.java)
  }

  override fun getApplicableMethodNames(): List<String> {
    return listOf("getColor")
  }
}
