package com.infinity.mysql.processor.model

/**
 * Created by richard on 16/02/2023 21:41
 *
 * Partial result of a code generated from a function used to generate only a small piece of the full code.
 */
data class PartialGenResult(
    val genCode: String,
    val genCodeParams: Array<Any>
) {

    /**
     * Used to check two objects equality since we are using a generic Array<Any> as property.
     *
     * @param other A other [PartialGenResult] or [Any] being verified
     * @return [Boolean] True=Equals, false=NotEqual
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartialGenResult

        if (genCode != other.genCode) return false
        if (!genCodeParams.contentEquals(other.genCodeParams)) return false

        return true
    }

    /**
     * Used to generate a hash code for this instance, since we are using a generic Array<Any> as property.
     *
     * @return [Int] hash code.
     */
    override fun hashCode(): Int {
        var result = genCode.hashCode()
        result = 31 * result + genCodeParams.contentHashCode()
        return result
    }
}
