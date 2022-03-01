package org.ddmac.capml.parser

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.ddmac.capml.R
import org.ddmac.capml.exceptions.CapmlFileFormatException
import org.ddmac.capml.exceptions.CapmlParseException
import org.ddmac.capml.exceptions.InvalidCapmlException
import java.io.*
import java.util.*

/**
 * This class parses the .capml file
 *
 * @property [ctx] the context used to generate Android UI Elements.
 * @constructor primes the use of the parse() method.
 */

class CapMLParser(private val ctx: Context) {

    //the data will go here.
    val data = JsonObject()

    /**
     * Takes the [capmlFile] and opens a stream to it.
     * Manually iterates throug bytes.
     * Looks for the .capml...decorators? Flags?
     *
     * @return a [ScrollView] containing the elements, order maintained.
     */

    fun parse(capmlFile: File): ScrollView {

        if(capmlFile.name.substringAfterLast('.')!="capml"){
            throw CapmlFileFormatException(
                "File ${capmlFile.name} is not of type CapML. Please provide a .capml file"
            )
        }

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
        var key: String? = null

        while (r != -1) {
            r = br.read() //read the next byte
            if (r == -1) break //if it somehow gets here with EOF, stop
            when (Char(r)) { //int to char to check decorators
                '#' -> { br.readLine(); continue } //skip to end of line on comment char
                '~' -> { //the element identifier is the bytes of the following two element chars
                    element = br.read() + br.read()
                    br.readLine()
                }
                '+' -> { //add the string following the decorator
                    content.add(
                        br
                            .readLine()
                            .replaceFirst("+", "")
                            .trim()
                    )
                }
                '=' -> {//add the key following the decorator.
                    key = br.readLine()
                }
                '-' -> {//End of Element decorator. Adds all views to the linear layout.
                    ll.addView(createElement(element, content,key))
                    content = LinkedList()
                    br.read()
                }
                else -> continue //move along. move along.
            }
        }
        br.close()
        fos.close()

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
     * Takes the [capmlFile] and opens a stream to it.
     * Manually iterates throug bytes.
     * Looks for the .capml...decorators? Flags?
     *
     * @return a [ScrollView] containing the elements, order maintained.
     */

    fun parse(capmlFile: InputStream): ScrollView {
        //LinearLayout to contain elements. Basic "Style"
        val ll = LinearLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            orientation = LinearLayout.VERTICAL
        }

        //Sut up buffered reader to go through bytes.
        val br = BufferedReader(InputStreamReader(capmlFile))

        var r = 0 //EOF is -1
        var element = 0 //int representation of the parsed element.
        var content: LinkedList<String> = LinkedList() //Content when generating views.
        var key: String? = null

        while (r != -1) {
            r = br.read() //read the next byte
            if (r == -1) break //if it somehow gets here with EOF, stop
            when (Char(r)) { //int to char to check decorators
                '#' -> { br.readLine(); continue } //skip to end of line on comment char
                '~' -> { //the element identifier is the bytes of the following two element chars
                    element = br.read() + br.read()
                    br.readLine()
                }
                '+' -> { //add the string following the decorator
                    content.add(
                        br
                            .readLine()
                            .replaceFirst("+", "")
                            .trim()
                    )
                }
                '=' -> {//add the key following the decorator.
                   key = br.readLine()
                }
                '-' -> {//End of Element decorator. Adds view to the linear layout.
                    ll.addView(
                        createElement(
                            element, 
                            content,
                            key
                        )
                    )
                    content = LinkedList()
                    br.read()
                }
                else -> continue //move along. move along.
            }
        }

        br.close()
        capmlFile.close()

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
        content: LinkedList<String>,
        key: String?
    ): View {
        return when (element) {
            133 -> {
                if(key==null) throw CapmlParseException("CB element must have key decorator (=) and value")
                createCheckBox(
                    content.first,
                    key
                )

            }
            170 -> {
                createTextView(
                    content.first
                )
            }
            153 -> {
                if(key == null) throw CapmlParseException("Element ET must have key decorator (=) and value")
                createEditText(
                    content.first,
                    key
                )

            }
            163 -> {
                if(key==null) throw CapmlParseException("Element SP must have key decorator (=) and value")
                createSpinner(
                    content,
                    key
                )
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
    private fun createCheckBox(content: String, key: String) =
        CheckBox(ctx).apply {
            text = content
            setOnCheckedChangeListener { _, isChecked -> data.addProperty(key,isChecked) }
        }

    /**
     * Makes the [EditText] hint content be [content]
     *
     * @return [EditText]
     */

    private fun createEditText(content: String, key: String) =
        EditText(ctx).apply {
            hint = content
            addTextChangedListener(ETWatcher((key)))
        }

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

    private fun createSpinner(content: LinkedList<String>, key: String): Spinner {
        content.push(" ")
         return Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, R.layout.spinner_content,content)
            onItemSelectedListener = SPListener(key,this)
        }
    }

    //Extension function to allow the user to map their .capml data to an associated class.
    inline fun <reified T> JsonObject.toClass() = Gson().fromJson(this.asString,T::class.java)

    inner class ETWatcher(val key: String): TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            data.addProperty(key,s.toString())
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    inner class SPListener(private val key: String, private val spinner: Spinner): AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            data.addProperty(key, (view as TextView).text.toString())
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            data.addProperty(key,"")
        }
    }
    
}
