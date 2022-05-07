package org.sjhstudio.howstoday.util

object Constants {

    const val DEBUG = "debug"
    const val XML = "xml"
    const val GSON = "gson"
    const val JSON = "json"
    const val LOCBOOKMARK_DATABASE = "locbookmark_database"

    // Air fragment state
    const val AIR_STATE_WAITING = "air_state_waiting"   // 불러오기 성공
    const val AIR_STATE_FAIL = "air_state_fail" // 불러오기 실패
    const val AIR_STATE_CHECKING_SERVER = "air_state_checking_server"   // 서버점검및교정

    // Api url
    const val OPEN_API_URL = "http://openapi.data.go.kr/"
    const val KAKAO_URL = "https://dapi.kakao.com/"
    const val APIS_URL = "http://apis.data.go.kr/"

    // Request
    const val REQ_PERMISSION = 0

}