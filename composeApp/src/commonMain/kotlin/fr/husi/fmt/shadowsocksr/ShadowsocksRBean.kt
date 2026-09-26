package fr.husi.fmt.shadowsocksr

import kotlinx.serialization.Serializable as KxsSerializable
import fr.husi.fmt.AbstractBean
import fr.husi.fmt.BeanConverters
import fr.husi.io.BinaryInput
import fr.husi.io.BinaryOutput

@KxsSerializable
class ShadowsocksRBean : AbstractBean() {

    companion object {
        @JvmField
        val CREATOR = object : CREATOR<ShadowsocksRBean>() {
            override fun newInstance(): ShadowsocksRBean {
                return ShadowsocksRBean()
            }

            override fun newArray(size: Int): Array<ShadowsocksRBean?> {
                return arrayOfNulls(size)
            }
        }
    }

    var method: String = "none"
    var password: String = ""
    var protocol: String = "auth_chain_a"
    var protocolParam: String = ""
    var obfs: String = "plain"
    var obfsParam: String = ""

    override fun initializeDefaultValues() {
        super.initializeDefaultValues()
        if (method.isBlank()) method = "none"
        if (protocol.isBlank()) protocol = "auth_chain_a"
        if (obfs.isBlank()) obfs = "plain"
    }

    override fun serialize(output: BinaryOutput) {
        output.writeInt(1)
        super.serialize(output)
        output.writeString(method)
        output.writeString(password)
        output.writeString(protocol)
        output.writeString(protocolParam)
        output.writeString(obfs)
        output.writeString(obfsParam)
    }

    override fun deserialize(input: BinaryInput) {
        val version = input.readInt()
        super.deserialize(input)
        method = input.readString()
        password = input.readString()
        protocol = input.readString()
        protocolParam = input.readString()
        obfs = input.readString()
        obfsParam = input.readString()
    }

    override fun clone(): ShadowsocksRBean {
        return BeanConverters.deserialize(ShadowsocksRBean(), BeanConverters.serialize(this))
    }

    override val defaultPort get() = 8388
}
