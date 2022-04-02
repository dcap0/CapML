package org.ddmac.capml

import androidx.test.platform.app.InstrumentationRegistry
import org.ddmac.capml.exceptions.CapmlFileFormatException
import org.ddmac.capml.exceptions.CapmlParseException
import org.ddmac.capml.exceptions.InvalidCapmlException
import org.ddmac.capml.parser.CapMLParser
import org.junit.Test
import java.io.File

class ParserExceptionsTests {

    val ctx = InstrumentationRegistry.getInstrumentation().context
    val am = ctx.assets

    @Test(expected = CapmlFileFormatException::class)
    fun invalidFiletypeTest(){
        val html = am.open("capml/ParserExceptionsTests/not-capml.html")
        val f = File(ctx.cacheDir,"not-capml.html")
        if(!f.exists()) f.createNewFile()
        f.writeBytes(html.readBytes())
        val p = CapMLParser(ctx)
        p.parse(f)
    }

    @Test(expected = CapmlParseException::class)
    fun elementMissingDecoratorTest(){
        val html = am.open("capml/ParserExceptionsTests/invalid-et.capml")
        val f = File(ctx.cacheDir,"invalid-et.capml")
        if(!f.exists()) f.createNewFile()
        f.writeBytes(html.readBytes())
        val p = CapMLParser(ctx)
        p.parse(f)
    }
}