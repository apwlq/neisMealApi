package io.github.apwlq.mealapi

import io.github.apwlq.mealapi.dto.*
import io.github.apwlq.mealapi.exceptions.detailed.*

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.*
import java.net.UnknownHostException

class NeisApi private constructor(
    private val mealHost: String,
    private val schoolHost: String,
    private val menuPattern: String
) {

    fun getSchoolByName(name: String): List<School> {
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8)
        val document = parseXmlFromUrl("$schoolHost?SCHUL_NM=$encodedName")

        val nList = document.getElementsByTagName("row")
        val schools = mutableListOf<School>()

        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                schools.add(
                    School(
                        getTagValue("ATPT_OFCDC_SC_CODE", element),
                        getTagValue("SD_SCHUL_CODE", element),
                        getTagValue("LCTN_SC_NM", element),
                        getTagValue("SCHUL_NM", element)
                    )
                )
            }
        }
        return schools
    }

    fun getMealsByDay(day: LocalDate, scCode: String, schoolCode: String, includeKcal: Boolean = false): Meal {
        val formattedDate = day.toString().replace("-", "")
        val document = parseXmlFromUrl("$mealHost?ATPT_OFCDC_SC_CODE=$scCode&SD_SCHUL_CODE=$schoolCode&MLSV_YMD=$formattedDate")
        return if (includeKcal) getMealKcal(document) else getMeal(document)
    }

    private fun parseXmlFromUrl(url: String): Document {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            return dBuilder.parse(url)
        } catch (e: UnknownHostException) {
            throw ParseException("인터넷 연결 혹은 DNS에서 문제가 발생하였습니다: $url", e)
        } catch (e: Exception) {
            throw ParseException("URL에서 XML을 구문 분석하지 못했습니다: $url", e)
        }
    }

    private fun getMeal(doc: Document): Meal {
        val nList = doc.getElementsByTagName("row")
        val response = Meal()

        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val menus = parseMenus(getTagValue("DDISH_NM", element))
                when (getTagValue("MMEAL_SC_NM", element)) {
                    "조식" -> response.breakfast = menus
                    "중식" -> response.lunch = menus
                    "석식" -> response.dinner = menus
                }
            }
        }

        return response
    }

    private fun getMealKcal(doc: Document): Meal {
        val nList = doc.getElementsByTagName("row")
        val response = Meal()

        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val calInfo = getTagValue("CAL_INFO", element)
                val menu = mutableListOf(calInfo.substringBefore('k').trim())

                when (getTagValue("MMEAL_SC_NM", element)) {
                    "조식" -> response.breakfast = menu
                    "중식" -> response.lunch = menu
                    "석식" -> response.dinner = menu
                }
            }
        }

        return response
    }

    private fun getTagValue(tag: String, element: Element): String {
        val nodeList = element.getElementsByTagName(tag).item(0)?.childNodes
        return nodeList?.item(0)?.nodeValue ?: ""
    }

    private fun parseMenus(menus: String): MutableList<String> {
        return menus.split("<br/>").map { it.replace("\\(\\d+(\\.\\d+)*\\)|\\([ㄱ-힇]+\\d*\\)|[a-zA-Z]|[*^%$#@!_ ]".toRegex(), "").replace("[,-/]".toRegex(), "&").trim() }.toMutableList()
    }

    data class Builder(
        private var mealHost: String = "https://open.neis.go.kr/hub/mealServiceDietInfo",
        private var schoolHost: String = "https://open.neis.go.kr/hub/schoolInfo",
        private var menuPattern: String = "[^가-희]"
    ) {
        fun setMealHost(mealHost: String) = apply { this.mealHost = mealHost }
        fun setSchoolHost(schoolHost: String) = apply { this.schoolHost = schoolHost }
        fun setMenuPattern(menuPattern: String) = apply { this.menuPattern = menuPattern }
        fun build() = NeisApi(mealHost, schoolHost, menuPattern)
    }
}
