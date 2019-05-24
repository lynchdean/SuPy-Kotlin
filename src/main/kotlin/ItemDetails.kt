import Main.logger
import java.io.FileInputStream
import java.util.*

class ItemDetails(propertiesPath: String) {

    var description: String
    var descExclude: String
    var colour: String
    var size: String

    init {
        val properties = getProperties(propertiesPath)
        this.description = properties.getProperty("desc")
        this.descExclude = properties.getProperty("descExclude")
        this.colour= properties.getProperty("colour")
        this.size = properties.getProperty("size")

    }

    private fun getProperties(propertiesPath: String) : Properties {
        val properties = Properties()
        val input = FileInputStream(propertiesPath)
        properties.load(input)
        logger?.info("Details loaded from item properties files")
        return properties
    }
}
