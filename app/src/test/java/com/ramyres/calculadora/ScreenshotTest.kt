package com.ramyres.calculadora

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {
    @Test
    fun geraPrintComCalculoExecutado() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().visible().get()
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)

        listOf("4", "9", "0", "0", "+", "1", "5", "9", "1", "0", "=").forEach { label ->
            findText(root, label).performClick()
        }

        val width = 390
        val height = 800
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))
        val workingDirectory = requireNotNull(System.getProperty("user.dir"))
        val projectRoot = requireNotNull(File(workingDirectory).parentFile)
        val output = projectRoot.resolve("docs/calculadora-executando.png")
        output.parentFile?.mkdirs()
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun findText(view: View, text: String): TextView {
        if (view is TextView && view.text.toString() == text) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                runCatching { return findText(view.getChildAt(index), text) }
            }
        }
        error("Botão $text não encontrado")
    }
}
