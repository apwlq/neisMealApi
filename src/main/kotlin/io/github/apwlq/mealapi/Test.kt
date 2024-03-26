package io.github.apwlq.mealapi

import java.time.LocalDate


const val schoolName = "부산동고등학교"
const val date = "20240325"

fun main() {
    val meal = getLunch().replace("[하단|하단중|공산]".toRegex(), "") + getDinner().replace("[하단|하단중|공산]".toRegex(), "")

    println(meal)

    println("=====================================")

    println(getLunchKcal())

}

fun getMealTime(mealType: (meals: io.github.apwlq.mealapi.dto.Meal) -> MutableList<String>?): String {
    val neis = NeisApi.Builder().build()
    val sch = neis.getSchoolByName(schoolName).firstOrNull() ?: return "등록된 학교가 없음"
    val meal = neis.getMealsByDay(date, sch.scCode, sch.schoolCode, false)

    return mealType(meal)?.joinToString("\n") ?: "확인된 급식 없음"
}

fun getMealKcalTime(mealType: (meals: io.github.apwlq.mealapi.dto.Meal) -> MutableList<String>?): String {
    val neis = NeisApi.Builder().build()
    val sch = neis.getSchoolByName(schoolName).firstOrNull() ?: return "등록된 학교가 없음"
    val meal = neis.getMealsByDay(date, sch.scCode, sch.schoolCode, true)

    return mealType(meal)?.joinToString("\n") ?: "확인된 급식 없음"
}

fun getBreakfast(): String = getMealTime { it.breakfast }
fun getLunch(): String = getMealTime { it.lunch }
fun getDinner(): String = getMealTime { it.dinner }

fun getLunchKcal(): String = getMealKcalTime { it.lunch }