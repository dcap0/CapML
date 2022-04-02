package org.ddmac.capml

import android.content.Context
import android.widget.*
import androidx.core.view.get
import androidx.test.platform.app.InstrumentationRegistry
import org.ddmac.capml.parser.CapMLParser

import org.junit.Test


class ElementCreationTests{

    private val ctx: Context = InstrumentationRegistry.getInstrumentation().context
    val am = ctx.resources.assets

    @Test
    fun testCreateEditText(){
        val p = CapMLParser(ctx)
        val etFile = am.open("capml/ElementCreationTests/et.capml")
        val parentView = p.parse(etFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        assert(ll.getChildAt(0)::class == EditText::class)
    }

    @Test
    fun testCreateTextView(){
        val p = CapMLParser(ctx)
        val tvFile = am.open("capml/ElementCreationTests/tv.capml")
        val parentView = p.parse(tvFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        assert(ll.getChildAt(0)::class == TextView::class)
    }

    @Test
    fun testCreateCheckBox(){
        val p = CapMLParser(ctx)
        val cbFile = am.open("capml/ElementCreationTests/cb.capml")
        val parentView = p.parse(cbFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        assert(ll.getChildAt(0)::class == CheckBox::class)
    }

    @Test
    fun testCreateSpinner(){
        val p = CapMLParser(ctx)
        val cbFile = am.open("capml/ElementCreationTests/sp.capml")
        val parentView = p.parse(cbFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        assert(ll.getChildAt(0)::class == Spinner::class)
    }

    @Test
    fun confirmValuesPopulateSpinner(){
        val p = CapMLParser(ctx)
        val cbFile = am.open("capml/ElementCreationTests/sp.capml")
        val parentView = p.parse(cbFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        val sp = ll[0] as Spinner
        sp.setSelection(2)
        assert(sp.selectedItem.toString() == "is")
    }

    @Test
    fun createManySpinners(){
        val p = CapMLParser(ctx)
        val lFile = am.open("capml/ElementCreationTests/large-file.capml")
        val parentView = p.parse(lFile)
        val ll = parentView.getChildAt(0) as LinearLayout
        assert(ll.childCount == 10000)
    }

}