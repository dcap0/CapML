package org.ddmac.capml

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class CapMLParser(private val ctx: Context) {

    //we want to take the CapML, determine the item type, and generate it.
    //TODO need to add an exception for the filetype being wrong =/
    fun parse(capmlFile: File): ScrollView {
        val ll = LinearLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            orientation = LinearLayout.VERTICAL
        }

        //type of element -> ~
        //If I come across this char, the next two characters are the element type
        //any content -> +
        //element end -> -
        val fos = FileInputStream(capmlFile)
        val br = BufferedReader(InputStreamReader(fos))

        var r = 0
        var element: Int = 0
        var content: LinkedList<String> = LinkedList()

        while (r != -1) {
            r = br.read()
            if (r == -1) break
            when (Char(r)) {
                '#' -> { br.readLine(); continue }
                '~' -> {
                    element = br.read() + br.read()
                    br.skip(1) //skip the newline
                }
                '+' -> {
                    content.add(
                        br
                            .readLine()
                            .replaceFirst("+", "")
                            .trim()
                    )
                }
                '-' -> {
                    ll.addView(createElement(element, content))
                    //this is messin it up
                    //content.clear()
                    content = LinkedList()
                    br.read() //puts us at the end of the line?
                }
                else -> continue
            }
        }


        (ll as ViewGroup).children.forEach {
            println(it::class.simpleName)
        }


        return ScrollView(ctx)
            .apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(ll)
            }
    }


    private fun createElement(
        element: Int,
        content: LinkedList<String>
    ): View {
        return when (element) {
            133 -> {
                createCheckBox(content.first)
            }
            170 -> {
                createTextView(content.first)
            }
            153 -> {
                createEditText(content.first)
            }
            163 -> {
                createSpinner(content)
            }
            else -> {
                View(ctx)
            }
        }
    }


    private fun createCheckBox(content: String) =
        CheckBox(ctx).apply { text = content }

    private fun createEditText(content: String) =
        EditText(ctx).apply { hint = content }

    private fun createTextView(content: String) =
        TextView(ctx).apply { text = content }

    private fun createSpinner(content: LinkedList<String>): Spinner {
        val spinner = Spinner(ctx)
        content.push(" ")
        val adapter = ArrayAdapter<String>(ctx, R.layout.spinner_content,content)
        spinner.adapter = adapter
        return spinner
    }


}
