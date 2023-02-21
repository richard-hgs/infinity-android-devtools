package com.infinity.devtools.model.sqlite

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.infinity.devtools.constants.Constants.NO_VALUE
import com.infinity.devtools.constants.ConstantsDb.TABLE_MYSQL_CONN

@Entity(
    tableName = TABLE_MYSQL_CONN,
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["host", "port", "user"], unique = true)
    ]
)
data class MysqlConn(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val host: String,
    val port: Int,
    val user: String,
    val pass: String,
    val dbname: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor() : this(0, NO_VALUE, NO_VALUE, 0, NO_VALUE, NO_VALUE, NO_VALUE)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(host)
        parcel.writeInt(port)
        parcel.writeString(user)
        parcel.writeString(pass)
        parcel.writeString(dbname)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MysqlConn> {
        override fun createFromParcel(parcel: Parcel): MysqlConn {
            return MysqlConn(parcel)
        }

        override fun newArray(size: Int): Array<MysqlConn?> {
            return arrayOfNulls(size)
        }
    }
}