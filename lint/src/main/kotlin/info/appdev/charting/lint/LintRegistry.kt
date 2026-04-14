package info.appdev.charting.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class LintRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        EntryUsageDetector.ISSUE
    )

    /** Must match the Lint API version used at compile time. */
    override val api: Int = CURRENT_API

    /** Minimum Lint API version this registry works with. */
    override val minApi: Int = 14

    override val vendor: Vendor = Vendor(
        vendorName  = "AndroidChart",
        feedbackUrl = "https://github.com/AppDevNext/AndroidChart/issues"
    )
}

