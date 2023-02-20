package com.infinity.devtools.ui.components.sharedelement.navigation

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by richard on 20/02/2023 15:06
 *
 */
data class SharedNavStackDest(
    val route: String
) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString()!!)

    companion object {
        val EMPTY = SharedNavStackDest("")

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
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SharedNavStackDest(route='$route')"
    }
}
