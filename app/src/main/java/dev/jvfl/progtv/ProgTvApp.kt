package dev.jvfl.progtv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Hilt generates the dependency graph from here. */
@HiltAndroidApp
class ProgTvApp : Application()
