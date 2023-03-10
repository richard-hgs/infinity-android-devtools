package com.infinity.devtools.ui.components.sharedelement

/**
 * Unique identifier for a shared element
 * @param key        A unique key for a shared element on a screen
 * @param screenKey  The screen key where the element is located at
 */
class SharedElId(
    val key : String,
    val screenKey : String
) {
    /**
     * String presentation of a SharedElId
     *
     * @return
     */
    override fun toString(): String {
        return "SharedElId(key='$key', screenKey='$screenKey')"
    }

    /**
     * Overriding equals allow us to compare [SharedElId] == [SharedElId] with different hash codes,
     * but with same attributes.
     * @param other The other element to check against
     */
    override fun equals(other: Any?): Boolean {
        other as SharedElId
        if (key != other.key) return false
        if (screenKey != other.screenKey) return false
        return true
    }

    /**
     * Overriding hash code allow us to calculate hash based on attributes contents,
     * to have more control about equality checks
     *
     * @return
     */
    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + screenKey.hashCode()
        return result
    }
}
