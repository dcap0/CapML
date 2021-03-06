package org.ddmac.capml.parser

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ddmac.capml.R
import org.ddmac.capml.exceptions.CapmlFileFormatException
import org.ddmac.capml.exceptions.CapmlParseException
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.logging.Logger

/**
 * This class parses the .capml file
 *
 * @property [ctx] the context used to generate Android UI Elements.
 * @constructor primes the use of the parse() method.
 */

class CapMLParser(
    private val ctx: Context
) {

    private val logger = Logger.getLogger(CapMLParser::class.simpleName ?: "CAPML-PARSER")
    private val loggingEnabled = false
    //the data will go here.
    var data = JsonObject()


    /**
     * Takes the [capmlFile] and opens a stream to it.
     * Manually iterates through bytes.
     * Looks for the .capml...decorators? Flags?
     * Once it has finished assembling the scrollview, executes a callback
     * using the view
     *
     * @return [Unit]
     */
    @Throws(CapmlFileFormatException::class, CapmlParseException::class)
    suspend fun parse(
        capmlFile: File,
        callback:((View) -> Unit)
    ){
        callback.invoke(parse(capmlFile.inputStream()))
    }

    /**
     * Takes the [capmlData] stream and manually iterates through bytes.
     * Looks for the .capml...decorators? Flags?
     * Once it has finished assembling the scrollview, executes a callback
     * using the view
     *
     * @return [Unit]
     */
    @Throws(CapmlFileFormatException::class, CapmlParseException::class)
    suspend fun parse(
        capmlData: InputStream,
        callback:((View) -> Unit)
    ){
        callback.invoke(parse(capmlData))
    }


    /**
     * Takes the [capmlFile] and opens a stream to it.
     * Manually iterates through bytes.
     * Looks for the .capml...decorators? Flags?
     *
     * @return a [ScrollView] containing the elements, order maintained.
     */
    @Throws(CapmlFileFormatException::class, CapmlParseException::class)
    suspend fun parse(capmlFile: File): ScrollView {
        return parse(capmlFile.inputStream())
    }


    /**
     * Takes the [capmlData] and opens a stream to it.
     * Manually iterates through bytes.
     * Looks for the .capml...decorators? Flags?
     *
     * @return a [ScrollView] containing the elements, order maintained.
     */
    @Throws(CapmlFileFormatException::class, CapmlParseException::class)
    suspend fun parse(
        capmlData: InputStream
    ): ScrollView = withContext(Dispatchers.IO){
        //LinearLayout to contain elements. Basic "Style"
        val ll = LinearLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            orientation = LinearLayout.VERTICAL
        }

        //Sut up buffered reader to go through bytes.
        val br = BufferedReader(InputStreamReader(capmlData))

        var r = 0 //EOF is -1
        var element = ""
        var content: LinkedList<String> = LinkedList() //Content when generating views.
        var key: String? = null

        while (r != -1) {
            r = br.read() //read the next byte
            if (r == -1) break //if it somehow gets here with EOF, stop
            when (Char(r)) { //int to char to check decorators
                '#' -> {
                    br.readLine(); continue
                } //skip to end of line on comment char
                '~' -> {
                    //the element identifier is the bytes of the following two element chars
                    element = Char(br.read()).toString() + Char(br.read()).toString()
                    br.readLine()
                }
                '+' -> { //add the string following the decorator
                    content.add(
                        br
                            .readLine()
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
        capmlData.close()

        //Wrap the linear layout in a scroll view. Accounts for many elements. More "Style"
        ScrollView(ctx)
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
        element: String,
        content: LinkedList<String>,
        key: String?
    ): View {
        if(loggingEnabled) {
            logger.warning(element)
            logger.warning(element.hashCode().toString())
        }
        return when (element.hashCode()) {
            Widgets.CB -> {
                if (key == null) throw CapmlParseException("CB element must have key decorator (=) and value")
                if (!data.has(key)) {
                    data.addProperty(key, "")
                }
                createCheckBox(content.first(), key)
            }
            Widgets.TV -> {
                createTextView(content.first())
            }
            Widgets.ET -> {
                if (key == null) throw CapmlParseException("Element ET must have key decorator (=) and value")
                if (!data.has(key)) data.addProperty(key, "")
                createEditText(content.first(), key)
            }
            Widgets.SP -> {
                if (key == null) throw CapmlParseException("Element SP must have key decorator (=) and value")
                if (!data.has(key)) data.addProperty(key, "")
                createSpinner(content, key)
            }
            else -> {
                View(ctx)
            }
        }
    }


    /**
     * Makes the [CheckBox] text content be [content].
     * If the parser object's data has the key, it will set it checked based on it's value.
     *
     * @return [CheckBox]
     */
    private fun createCheckBox(content: String, key: String) =
        CheckBox(ctx).apply {
            text = content
            setOnCheckedChangeListener { _, isChecked -> data.addProperty(key, isChecked) }
            isChecked = data.get(key).asBoolean
        }

    /**
     * Makes the [EditText] hint content be [content]
     * If the parser's data object has a value for the key provided,
     *  The [EditText] will be populated with that value.
     *
     * @return [EditText]
     */

    private fun createEditText(content: String, key: String) =
        EditText(ctx).apply {
            hint = content
            setText((data.get(key) ?: "").toString())
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
     * If the parse object's data object has a value,
     *  that value will be selected if it exists in the [Spinner]
     *
     * @return [Spinner]
     */

    private fun createSpinner(content: LinkedList<String>, key: String): Spinner {
        content.push("")
        if(loggingEnabled) logger.warning(content.joinToString())
        val index = if (data.has(key)) {
            content.indexOf(data.get(key).toString())
        } else {
            0
        }
        return Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, R.layout.spinner_content, content)
            onItemSelectedListener = SPListener(key)
            this.setSelection(index)
        }
    }

    /**
     * Function that allows user to translate the parser's data json to a class they pass in.
     *
     * @param [c] is the user's class of choice
     * @return the [Class] populated with the user's data.
     */
    inline fun <reified T> dataToClass(c: Class<T>): T = Gson().fromJson(data, c)

    /**
     * Function that takes user data class and translates into a JsonObject.
     *
     * @param [c] the user's [Class]
     * @return [JsonObject]
     */

    fun <T> classToData(c: T): JsonObject = JsonParser.parseString(Gson().toJson(c)).asJsonObject

    inner class ETWatcher(private val key: String) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            data.addProperty(key, s.toString())
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    inner class SPListener(private val key: String) : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            data.addProperty(key, (view as TextView).text.toString())
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            data.addProperty(key, "")
        }
    }

    private object Widgets {
        const val TV: Int = 2690
        const val ET: Int = 2223
        const val SP: Int = 2653
        const val CB: Int = 2143
    }

}