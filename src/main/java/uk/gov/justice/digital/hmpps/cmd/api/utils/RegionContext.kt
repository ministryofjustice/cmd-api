package uk.gov.justice.digital.hmpps.cmd.api.utils

object RegionContext {
  private val regionStore = ThreadLocal<Int>()
  fun getRegion(): Int? = regionStore.get()

  fun setRegion(region: Int) {
    regionStore.set(region)
  }
}
