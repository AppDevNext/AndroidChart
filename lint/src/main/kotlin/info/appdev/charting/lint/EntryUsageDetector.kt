package info.appdev.charting.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.getParentOfType

/**
 * Lint detector that flags all usages of deprecated legacy entry classes and offers
 * auto-fixes to replace them with either the `*Float` or `*Double` equivalent.
 *
 * Deprecated → Float replacement → Double replacement:
 *  Entry        → EntryFloat        → EntryDouble
 *  BarEntry     → BarEntryFloat     → BarEntryDouble
 *  BubbleEntry  → BubbleEntryFloat  → BubbleEntryDouble
 *  CandleEntry  → CandleEntryFloat  → CandleEntryDouble
 *  PieEntry     → PieEntryFloat     → PieEntryDouble
 *  RadarEntry   → RadarEntryFloat   → RadarEntryDouble
 */
class EntryUsageDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val PKG = "info.appdev.charting.data"

        /** Deprecated FQN → Float replacement FQN */
        private val FLOAT_REPLACEMENTS: Map<String, String> = mapOf(
            "$PKG.Entry"        to "$PKG.EntryFloat",
            "$PKG.BarEntry"     to "$PKG.BarEntryFloat",
            "$PKG.BubbleEntry"  to "$PKG.BubbleEntryFloat",
            "$PKG.CandleEntry"  to "$PKG.CandleEntryFloat",
            "$PKG.PieEntry"     to "$PKG.PieEntryFloat",
            "$PKG.RadarEntry"   to "$PKG.RadarEntryFloat",
        )

        /** Deprecated FQN → Double replacement FQN */
        private val DOUBLE_REPLACEMENTS: Map<String, String> = mapOf(
            "$PKG.Entry"        to "$PKG.EntryDouble",
            "$PKG.BarEntry"     to "$PKG.BarEntryDouble",
            "$PKG.BubbleEntry"  to "$PKG.BubbleEntryDouble",
            "$PKG.CandleEntry"  to "$PKG.CandleEntryDouble",
            "$PKG.PieEntry"     to "$PKG.PieEntryDouble",
            "$PKG.RadarEntry"   to "$PKG.RadarEntryDouble",
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id               = "LegacyEntryUsage",
            briefDescription = "Replace deprecated legacy entry class with its `*Float` or `*Double` equivalent",
            explanation      = """
                Several entry classes (`Entry`, `BarEntry`, `BubbleEntry`, `CandleEntry`, \
                `PieEntry`, `RadarEntry`) are deprecated and will be removed in a future version. \
                Replace them with their `*Float` equivalents for identical precision, or \
                `*Double` equivalents for higher precision.
            """,
            category         = Category.CORRECTNESS,
            priority         = 6,
            severity         = Severity.ERROR,
            implementation   = Implementation(
                EntryUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private val DEPRECATED_SIMPLE_NAMES: Set<String> =
            FLOAT_REPLACEMENTS.keys.map { it.substringAfterLast('.') }.toSet()
    }

    override fun getApplicableReferenceNames(): List<String> =
        DEPRECATED_SIMPLE_NAMES.toList()

    override fun visitReference(
        context:    JavaContext,
        reference:  UReferenceExpression,
        referenced: PsiElement
    ) {
        if (referenced !is PsiClass) return
        val deprecatedFqn  = referenced.qualifiedName ?: return
        val floatFqn       = FLOAT_REPLACEMENTS[deprecatedFqn]  ?: return
        val doubleFqn      = DOUBLE_REPLACEMENTS[deprecatedFqn] ?: return

        val deprecatedSimple = deprecatedFqn.substringAfterLast('.')
        val floatSimple      = floatFqn.substringAfterLast('.')
        val doubleSimple     = doubleFqn.substringAfterLast('.')

        val importNode = reference.getParentOfType<UImportStatement>()
        val combinedFix: LintFix = if (importNode != null) {
            // For import statements replace the full FQN
            fix().alternatives(
                fix().replace().name("Replace with $floatSimple")
                    .text(deprecatedFqn).with(floatFqn).autoFix().build(),
                fix().replace().name("Replace with $doubleSimple (high precision)")
                    .text(deprecatedFqn).with(doubleFqn).autoFix().build()
            )
        } else {
            // For type refs / constructor calls replace only the simple name
            fix().alternatives(
                fix().replace().name("Replace with $floatSimple")
                    .text(deprecatedSimple).with(floatSimple).autoFix().build(),
                fix().replace().name("Replace with $doubleSimple (high precision)")
                    .text(deprecatedSimple).with(doubleSimple).autoFix().build()
            )
        }

        val location = if (importNode != null)
            context.getLocation(importNode)
        else
            context.getLocation(reference)

        val element = importNode ?: reference

        context.report(
            ISSUE,
            element,
            location,
            "Replace deprecated `$deprecatedSimple` — use `$floatSimple` (same precision) or `$doubleSimple` (higher precision)",
            combinedFix
        )
    }
}
