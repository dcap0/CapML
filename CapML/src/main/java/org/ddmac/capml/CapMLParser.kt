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

/**
 * This class parses the .capml file
 *
 * @property [ctx] the context used to generate Android UI Elements.
 * @constructor primes the use of the parse() method.
 */

class CapMLParser(private val ctx: Context) {

//TODO need to add an exception for the filetype being wrong =/

    /**
     * Takes the [capmlFile] and opens a stream to it.
     * Manually iterates throug bytes.
     * Looks for the .capml...decorators? Flags?
     *
     * @return a [ScrollView] containing the elements, order maintained.
     */

    fun parse(capmlFile: File): ScrollView {
        //LinearLayout to contain elements. Basic "Style"
        val ll = LinearLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            orientation = LinearLayout.VERTICAL
        }

        //Sut up buffered reader to go through bytes.
        val fos = FileInputStream(capmlFile)
        val br = BufferedReader(InputStreamReader(fos))

        var r = 0 //EOF is -1
        var element = 0 //int representation of the parsed element.
        var content: LinkedList<String> = LinkedList() //Content when generating views.

        while (r != -1) {
            r = br.read() //read the next byte
            if (r == -1) break //if it somehow gets here with EOF, stop
            when (Char(r)) { //int to char to check decorators
                '#' -> { br.readLine(); continue } //skip to end of line on comment char
                '~' -> { //the element identifier is the bytes of the following two element chars
                    element = br.read() + br.read()
                    br.skip(1)
                }
                '+' -> { //add the string following the decorator
                    content.add(
                        br
                            .readLine()
                            .replaceFirst("+", "")
                            .trim()
                    )
                }
                '-' -> {//End of Element decorator. Adds all views to the linear layout.
                    ll.addView(createElement(element, content))
                    content = LinkedList()
                    br.read()
                }
                else -> continue //move along. move along.
            }
        }

        //Wrap the linear layout in a scroll view. Accounts for many elements. More "Style"
        return ScrollView(ctx)
            .apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(ll)
            }
    }


    /**
     * Takes [element] value to determine the element type.
     * Depending on the [element], the [content] may be one or many.
     *
     * @return the [View] subclass denoted by the [element]
     */

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


    /**
     * Makes the [CheckBox] text content be [content]
     *
     * @return [CheckBox]
     */
    private fun createCheckBox(content: String) =
        CheckBox(ctx).apply { text = content }

    /**
     * Makes the [EditText] hint content be [content]
     *
     * @return [EditText]
     */

    private fun createEditText(content: String) =
        EditText(ctx).apply { hint = content }

    /**
     * Makes the [TextView] text content be [content]
     *
     * @return [TextView]
     */

    private fun createTextView(content: String) =
        TextView(ctx).apply { text = content }

    /**
     * Creates a [Spinner]
     * Creates an [ArrayAdapter] from the [content], with a space pushed to the front
     * Applies the [ArrayAdapter] to the [Spinner]
     *
     * @return [Spinner]
     */

    private fun createSpinner(content: LinkedList<String>): Spinner {
        val spinner = Spinner(ctx)
        content.push(" ")
        val adapter = ArrayAdapter(ctx, R.layout.spinner_content,content)
        spinner.adapter = adapter
        return spinner
    }


}
