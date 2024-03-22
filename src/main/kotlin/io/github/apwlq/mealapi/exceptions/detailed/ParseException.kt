package io.github.apwlq.mealapi.exceptions.detailed

import io.github.apwlq.mealapi.exceptions.NeisException

/**
 * NEIS API의 응답을 구문 분석하는 데 오류가 있을 때 발생하는 예외입니다.
 */
class ParseException : NeisException {
    constructor(s: String, e: Exception) : super("NEIS API 응답을 구문 분석하는 동안 오류가 발생했습니다.\n$s", e)
    constructor(s: String) : super("NEIS API 응답을 구문 분석하는 동안 오류가 발생했습니다.\n$s")
    constructor() : super("NEIS API 응답을 구문 분석하는 동안 오류가 발생했습니다.")
}
