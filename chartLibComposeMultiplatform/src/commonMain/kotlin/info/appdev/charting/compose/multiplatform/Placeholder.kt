package info.appdev.charting.compose.multiplatform

/**
 * Placeholder entry point for the Compose Multiplatform chart renderers (Step A.7).
 *
 * This module will host `DrawScope`-based re-implementations of the `chartLib`
 * renderer classes so that charts can be rendered from common code on Android,
 * iOS, desktop (and potentially wasmJs), consuming the portable model classes
 * already migrated to `chartLibCore`.
 */
public const val CHART_LIB_COMPOSE_MULTIPLATFORM_MODULE_NAME: String = "chartLibComposeMultiplatform"
