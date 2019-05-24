import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.Select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

object Main {
    val logger: Logger? = LoggerFactory.getLogger(this.javaClass)

    private const val baseUrl = "https://www.supremenewyork.com"

    private var userDir = System.getProperty("user.dir") + "/src/main/resources/"
    private var autofillPath = userDir + "autofill.properties"
    private var itemPath = userDir + "item.properties"

    private val inventoryMap = HashMap<String, Map<String, String>>() // Description -> (Colour -> urlPath)

    private fun updateInventory() {
        logger?.info("Starting inventory update")
        val postFix = "/shop/all/"
        val homeHtml = Jsoup.connect(baseUrl + postFix).get().html()
        val homeDoc = Jsoup.parse(homeHtml)
        val links = homeDoc.select("a")
        val categoryDivs = getCategoryDivs(links)

        // Update the inventory map with the items from each category
        for (div in categoryDivs) {
            val categoryHtml = Jsoup.connect(baseUrl + div.attr("href")).get().html()
            val categoryDoc = Jsoup.parse(categoryHtml)
            val itemDivs = categoryDoc.getElementsByClass("name-link")

            val itemIterator = itemDivs.iterator()
            while (itemIterator.hasNext()) {
                val descDiv = itemIterator.next()
                val colourDiv = itemIterator.next()
                addToInventoryMap(descDiv.text(), colourDiv.text(), colourDiv.attr("href"))
            }
        }
        logger?.info("Inventory update complete")
    }

    private fun getCategoryDivs(links: Elements): ArrayList<Element> {
        val categoryLinks = ArrayList<Element>()
        for (link in links) {
            if (link.toString().contains("/shop/all/")) {
                categoryLinks.add(link)
            } else if (categoryLinks.isNotEmpty()) {
                // If categoryLinks isn't empty and the string doesn't match, all the links have been found
                break
            }
        }
        return categoryLinks
    }

    private fun addToInventoryMap(description: String, colour: String, urlPath: String) {
        val colours: MutableMap<String, String>
        if (inventoryMap.containsKey(description)) {
            colours = inventoryMap[description] as MutableMap<String, String>
        } else {
            colours = HashMap()
        }
        colours[colour] = urlPath
        inventoryMap[description] = colours
    }

    private fun findTargetPath(itemDetails: ItemDetails): String? {
        for (itemDescription in inventoryMap.keys) {
            if (!itemDescription.contains(itemDetails.descExclude) || itemDetails.descExclude == "") {
                if (itemDescription.contains(itemDetails.description)) {
                    val itemColours = inventoryMap[itemDescription]
                    if (itemColours != null) {
                        return if (itemColours[itemDetails.colour] != null) {
                            logger?.info("Item found - url: ${itemColours[itemDetails.colour]} ")
                            itemColours[itemDetails.colour]
                        } else {
                            logger?.info("Item not found, using colour ${itemColours.keys.first()} instead - url: ${itemColours[itemColours.keys.first()]} ")
                            itemColours[itemColours.keys.first()]
                        }
                    }
                }
            }
        }
        logger?.error("Item not found")
        System.exit(0)
        return ""
    }

    private fun addToCart(driver: ChromeDriver, itemDetails: ItemDetails) {
        try {
            val sizeDropdown = Select(driver.findElement(By.id("size")))
            sizeDropdown.selectByVisibleText(itemDetails.size)
        } catch (e: Exception) {
            logger?.warn("Matching size not found in product page dropdown, continuing anyway with default size")
        }
        driver.findElement(By.name("commit")).click() // Click ATC button
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // Start webdriver beforehand to avoid wasting valuable time
        System.setProperty("webdriver.chrome.driver", "chromedriver")
        val driver = ChromeDriver()
        logger?.info("Webdriver started")

        // Setup details
        val coDetails = CheckoutDetails(autofillPath)
        val itemDetails = ItemDetails(itemPath)


        // Wait for website to update
        val wpm = WebPageMonitor("$baseUrl/shop/all")
        wpm.waitForChange(0.5)

        // Find the item
        updateInventory()
        val targetPath = findTargetPath(itemDetails)
        driver.get(baseUrl + targetPath)

        // ATC & Checkout
        addToCart(driver, itemDetails)
        logger?.info("Added to cart successfully")
        CheckoutAutoFill.fill(driver, coDetails)
        logger?.info("Complete?")
    }
}