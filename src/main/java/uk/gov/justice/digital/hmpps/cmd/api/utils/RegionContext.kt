package uk.gov.justice.digital.hmpps.cmd.api.utils

object RegionContext {
  private val regionStore = ThreadLocal<Int>()
  fun getRegion(): Int? = regionStore.get()

  fun <T> setRegion(region: Int, function: () -> T): T {
    regionStore.set(region)
    return function().also { regionStore.remove() }
  }
}
