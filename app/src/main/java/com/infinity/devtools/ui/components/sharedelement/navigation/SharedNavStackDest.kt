package com.infinity.devtools.ui.components.sharedelement.navigation

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable


data class SharedNavStackDest(
    val route: String,
    val arguments: Bundle?,
    val animatedKeys: Array<Any>?
) : Parcelable {

    @Suppress("DEPRECATION")
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readBundle(ClassLoader.getSystemClassLoader()),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) parcel.readArray(ClassLoader.getSystemClassLoader(), Any::class.java) else parcel.readArray(ClassLoader.getSystemClassLoader())
    )

    companion object {
        val EMPTY = SharedNavStackDest("", null, null)

        @JvmField val CREATOR : Parcelable.Creator<SharedNavStackDest> = object : Parcelable.Creator<SharedNavStackDest> {
            override fun createFromParcel(parcel: Parcel): SharedNavStackDest {
                return SharedNavStackDest(parcel)
            }

            override fun newArray(size: Int): Array<SharedNavStackDest?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(route)
        parcel.writeBundle(arguments)
        parcel.writeArray(animatedKeys)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SharedNavStackDest(route='$route')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharedNavStackDest

        if (route != other.route) return false
        if (arguments != other.arguments) return false
        if (animatedKeys != null) {
            if (other.animatedKeys == null) return false
            if (!animatedKeys.contentEquals(other.animatedKeys)) return false
        } else if (other.animatedKeys != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = route.hashCode()
        result = 31 * result + (arguments?.hashCode() ?: 0)
        result = 31 * result + (animatedKeys?.contentHashCode() ?: 0)
        return result
    }
}
