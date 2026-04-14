package info.appdev.charting.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.getParentOfType

/**
 * Lint detector that flags all usages of deprecated legacy entry classes and offers an
 * auto-fix to replace them with their `*Float` equivalents.
 *
 * Covered classes → replacements:
 *  - Entry        → EntryFloat
 *  - BarEntry     → BarEntryFloat
 *  - BubbleEntry  → BubbleEntryFloat
 *  - CandleEntry  → CandleEntryFloat
 *  - PieEntry     → PieEntryFloat
 *  - RadarEntry   → RadarEntryFloat
 */
class EntryUsageDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val PKG = "info.appdev.charting.data"

        /**
         * Maps deprecated FQN → replacement FQN.
         * Add future deprecated classes here; everything else is derived automatically.
         */
        private val REPLACEMENTS: Map<String, String> = mapOf(
            "$PKG.Entry"        to "$PKG.EntryFloat",
            "$PKG.BarEntry"     to "$PKG.BarEntryFloat",
            "$PKG.BubbleEntry"  to "$PKG.BubbleEntryFloat",
            "$PKG.CandleEntry"  to "$PKG.CandleEntryFloat",
            "$PKG.PieEntry"     to "$PKG.PieEntryFloat",
            "$PKG.RadarEntry"   to "$PKG.RadarEntryFloat",
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id               = "LegacyEntryUsage",
            briefDescription = "Replace deprecated legacy entry class with its `*Float` equivalent",
            explanation      = """
                Several entry classes (`Entry`, `BarEntry`, `BubbleEntry`, `CandleEntry`, \
                `PieEntry`, `RadarEntry`) are deprecated and will be removed in a future version. \
                Replace them with their `*Float` equivalents (e.g. `BarEntryFloat`) for identical \
                precision, or with `*Double` equivalents for higher precision.
            """,
            category         = Category.CORRECTNESS,
            priority         = 6,
            severity         = Severity.WARNING,
            implementation   = Implementation(
                EntryUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        /** Pre-computed set of simple names for fast look-up in visitReference(). */
        private val DEPRECATED_SIMPLE_NAMES: Set<String> =
            REPLACEMENTS.keys.map { it.substringAfterLast('.') }.toSet()
    }

    // -----------------------------------------------------------------------
    // Tell Lint to invoke visitReference() for every one of these identifiers.
    // -----------------------------------------------------------------------
    override fun getApplicableReferenceNames(): List<String> =
        DEPRECATED_SIMPLE_NAMES.toList()

    override fun visitReference(
        context:    JavaContext,
        reference:  UReferenceExpression,
        referenced: PsiElement
    ) {
        if (referenced !is PsiClass) return
        val deprecatedFqn  = referenced.qualifiedName ?: return
        val replacementFqn = REPLACEMENTS[deprecatedFqn] ?: return

        val deprecatedSimple  = deprecatedFqn.substringAfterLast('.')
        val replacementSimple = replacementFqn.substringAfterLast('.')

        // --- Import statement: replace the whole FQN ---
        val importNode = reference.getParentOfType<UImportStatement>()
        if (importNode != null) {
            context.report(
                ISSUE,
                importNode,
                context.getLocation(importNode),
                "Replace deprecated `$deprecatedSimple` import with `$replacementSimple`",
                fix().replace()
                    .text(deprecatedFqn)
                    .with(replacementFqn)
                    .autoFix()
                    .build()
            )
            return
        }

        // --- All other references (type annotations, constructor calls, …) ---
        context.report(
            ISSUE,
            reference,
            context.getLocation(reference),
            "Replace deprecated `$deprecatedSimple` with `$replacementSimple`",
            fix().replace()
                .text(deprecatedSimple)
                .with(replacementSimple)
                .autoFix()
                .build()
        )
    }
}
