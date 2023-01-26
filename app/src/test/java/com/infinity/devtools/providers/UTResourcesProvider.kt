package com.infinity.devtools.providers

import android.content.res.Resources
import com.infinity.devtools.utils.FileUtils
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

    /**
     * Gets a resource string
     */
    fun getString(id: Int) : String {
        if (stringsMap.containsKey(id)) {
            stringsMap.get(key = id)
        } else {
            throw Resources.NotFoundException("string resource ID #$id Not found")
        }
        return ""
    }
}