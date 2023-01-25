package com.infinity.devtools.providers

import com.infinity.devtools.domain.resource.ResourcesProvider
import com.infinity.devtools.utils.FileUtils
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.File
import java.nio.file.Paths

object UTResourcesProvider {

    /**
     * Map string resource name for int id's 0x00000
     */
    private val stringsIdResMap: HashMap<String, Int> = HashMap()

    /**
     * Map string resource value for int id's 0x00000
     */
    val stringsMap: HashMap<Int, String> = HashMap()

    /**
     * Initialize resources mapping to improve performance
     */
    init {
        val appPath = Paths.get("").toAbsolutePath().toString()
        // build\intermediates\runtime_symbol_list\debug
        val fIdRes = File(appPath, "build/intermediates/runtime_symbol_list/debug/R.txt")
        val fStrings = File(appPath, "/src/main/res/values/strings.xml")
        val fIdResContent = FileUtils.readFile(fIdRes)
        val fStringsContent = FileUtils.readFile(fStrings)
        val idResStringsMatcher = "int\\sstring\\s(?<resname>.*)\\s(?<resid>0x[0-9a-zA-Z]*)"
        val stringsNameValMatcher = "<string\\sname=\"(?<resid>[a-zA-Z_]*)\">(?<resval>.*)</string>"
        val stringsIdResMatches = Regex(idResStringsMatcher).findAll(fIdResContent)
        val stringsMatches = Regex(stringsNameValMatcher).findAll(fStringsContent)

        stringsIdResMatches.forEach {
            val (name, id) = it.destructured
            stringsIdResMap[name] = Integer.decode(id)
        }

        stringsMatches.forEach {
            val (name, value) = it.destructured
            if (stringsIdResMap.containsKey(key = name)) {
                val idResMapVal = stringsIdResMap.getValue(key = name)
                stringsMap[idResMapVal] = value
            }
            // println("name: ($name) -> ($value)")
        }
    }
}