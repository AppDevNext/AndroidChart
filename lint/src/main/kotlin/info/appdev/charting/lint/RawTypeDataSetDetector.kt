package info.appdev.charting.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClassType
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UTypeReferenceExpression
import org.jetbrains.uast.getParentOfType

class RawTypeDataSetDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val DATA_PKG       = "info.appdev.charting.data"
        private const val IFACE_PKG      = "info.appdev.charting.interfaces.datasets"
        private const val FLOAT_DEFAULT  = "EntryFloat"
        private const val DOUBLE_DEFAULT = "EntryDouble"

        private val GENERIC_TYPES: Map<String, Pair<String, String>> = mapOf(
            "$DATA_PKG.DataSet"                                 to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$DATA_PKG.LineDataSet"                             to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$IFACE_PKG.IDataSet"                               to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$IFACE_PKG.ILineDataSet"                           to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$IFACE_PKG.ILineRadarDataSet"                      to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$IFACE_PKG.ILineScatterCandleRadarDataSet"         to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
            "$IFACE_PKG.IBarLineScatterCandleBubbleDataSet"     to (FLOAT_DEFAULT to DOUBLE_DEFAULT),
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id               = "RawTypeDataSet",
            briefDescription = "Specify an explicit entry type parameter instead of `<*>` or no argument",
            explanation      = """
                Using a star projection `Type<*>` or omitting the type argument entirely loses \
                compile-time type-safety. Replace with `<EntryFloat>` (same precision as the \
                legacy API) or `<EntryDouble>` (higher precision). \
                Example: `LineDataSet` or `LineDataSet<*>` → `LineDataSet<EntryFloat>`.
            """,
            category         = Category.CORRECTNESS,
            priority         = 5,
            severity         = Severity.ERROR,
            implementation   = Implementation(
                RawTypeDataSetDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    // -----------------------------------------------------------------------
    // Why multiple node types?
    //
    //   UTypeReferenceExpression in getApplicableUastTypes() is only traversed
    //   for INLINE expressions (e.g. `is LineDataSet` checks).
    //
    //   For type ANNOTATIONS on declarations the UTypeReferenceExpression is
    //   NOT a traversed tree node — it is only accessible as a property:
    //     ULocalVariable.typeReference   → val x: LineDataSet<*>
    //     UField.typeReference           → class Foo { val x: LineDataSet<*> }
    //     UParameter.typeReference       → fun f(x: LineDataSet<*>)
    //     UMethod.returnTypeReference    → fun f(): LineDataSet<*>
    //
    //   We therefore visit all container node types and pull the typeReference
    //   from each one explicitly.
    // -----------------------------------------------------------------------
    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        ULocalVariable::class.java,           // val x: LineDataSet<*>
        UField::class.java,                   // class Foo { val x: LineDataSet<*> }
        UParameter::class.java,               // fun f(x: LineDataSet<*>)
        UMethod::class.java,                  // fun f(): LineDataSet<*>
        UTypeReferenceExpression::class.java, // is LineDataSet, cast as LineDataSet<*>, …
    )

    override fun createUastHandler(context: JavaContext): UElementHandler =
        object : UElementHandler() {

            override fun visitLocalVariable(node: ULocalVariable) =
                checkTypeRef(context, node.typeReference)

            override fun visitField(node: UField) =
                checkTypeRef(context, node.typeReference)

            override fun visitParameter(node: UParameter) =
                checkTypeRef(context, node.typeReference)

            override fun visitMethod(node: UMethod) =
                checkTypeRef(context, node.returnTypeReference)

            // Catches inline type positions: `is LineDataSet`, `as LineDataSet<*>`, …
            override fun visitTypeReferenceExpression(node: UTypeReferenceExpression) {
                if (node.getParentOfType<UImportStatement>() != null) return
                checkTypeRefNode(context, node)
            }

            // ---- shared implementation ----------------------------------------

            private fun checkTypeRef(context: JavaContext, typeRef: UTypeReferenceExpression?) {
                if (typeRef == null) return
                if (typeRef.getParentOfType<UImportStatement>() != null) return
                checkTypeRefNode(context, typeRef)
            }

            private fun checkTypeRefNode(context: JavaContext, node: UTypeReferenceExpression) {
                val sourcePsi = node.sourcePsi ?: return
                val nodeText  = sourcePsi.text.trim()

                for ((fqn, typeArgs) in GENERIC_TYPES) {
                    val simpleName        = fqn.substringAfterLast('.')
                    val isStarProjection  = nodeText == "$simpleName<*>"
                    val isMissingTypeArg  = nodeText == simpleName
                    if (!isStarProjection && !isMissingTypeArg) continue

                    // Verify FQN when the type resolves (guards against same-named classes)
                    val resolved = (node.type as? PsiClassType)?.resolve()
                    if (resolved != null && resolved.qualifiedName != fqn) continue

                    val (floatArg, doubleArg) = typeArgs
                    val (oldText, newFloat, newDouble) = if (isStarProjection)
                        Triple("$simpleName<*>", "$simpleName<$floatArg>", "$simpleName<$doubleArg>")
                    else
                        Triple(simpleName, "$simpleName<$floatArg>", "$simpleName<$doubleArg>")

                    context.report(
                        ISSUE,
                        node,
                        context.getLocation(sourcePsi),
                        if (isStarProjection) "Replace `$simpleName<*>` with an explicit entry type for type-safety"
                        else "Add missing type argument to `$simpleName`",
                        fix().alternatives(
                            fix().replace()
                                .name("Replace with $newFloat")
                                .text(oldText).with(newFloat)
                                .autoFix().build(),
                            fix().replace()
                                .name("Replace with $newDouble (high precision)")
                                .text(oldText).with(newDouble)
                                .autoFix().build()
                        )
                    )
                    break
                }
            }
        }
}
