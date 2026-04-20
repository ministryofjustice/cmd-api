package uk.gov.justice.digital.hmpps.cmd.api.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import uk.gov.justice.digital.hmpps.cmd.api.controllers.ResourceTest
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.utils.RegionContext
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import javax.sql.DataSource

class CsrDetailServiceIntTest(
  @Autowired private val detailService: CsrDetailService,
  @Autowired @Qualifier("regionDataSource") private val regionDataSource: DataSource,
) : ResourceTest() {
  private val jdbcTemplate: JdbcTemplate = JdbcTemplate(regionDataSource)

  companion object {
    private const val ONE_HR = 60 * 60
    private const val TWO_HRS = 60 * 60 * 2
    private const val NINE_HRS = 60 * 60 * 9
    private const val TEN_HRS = 60 * 60 * 10
  }

  @BeforeEach
  fun cleanUp() {
    RegionContext.setRegion(1) {
      jdbcTemplate.update("delete from CMD_NOTIFICATION")
      jdbcTemplate.update("delete from R2.CMD_NOTIFICATION")
      jdbcTemplate.update("delete from TK_MODEL")
      jdbcTemplate.update("delete from TK_TYPE")
      jdbcTemplate.update("delete from TK_MODELITEM")
    }
  }

  @Nested
  inner class UserDetails {
    @Test
    @WithMockAuthUser(username = "TEST-USER")
    fun testUserDetails() {
      RegionContext.setRegion(1) {
        jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (9, 'plain activity')")
        jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (10, 'present')")
        jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (11, 'break')")
        jdbcTemplate.update("insert into TK_MODEL(TK_MODEL_ID, NAME, FRAME_START, FRAME_END, IS_DELETED) values (12, 'L1234', 0, 0, 0)")
        jdbcTemplate.update("insert into TK_MODEL_INFO(TK_MODEL_INFO_ID, TK_MODEL_ID, FRAME_START, FRAME_END, IS_DELETED) values (1012, 12, $ONE_HR, $TWO_HRS, 0)")
        jdbcTemplate.update(
          """insert into TK_MODELITEM(TK_MODELITEM_ID,TK_MODEL_ID,TK_TYPE_ID,TASKSTYLE,IS_FRAME_RELATIVE,TASK_START,TASK_END)
          values (100, 1012, 10, 0, 1, 0, 5 * 60)
          """.trimMargin(),
        )
        jdbcTemplate.update(
          """insert into TK_MODELITEM(TK_MODELITEM_ID,TK_MODEL_ID,TK_TYPE_ID,TASKSTYLE,IS_FRAME_RELATIVE,TASK_START,TASK_END)
          values (100, 1012, 11, 0, 0, $ONE_HR + 5 * 60, $ONE_HR + 10 * 60)
          """.trimMargin(),
        )
        jdbcTemplate.update(
          """insert into TK_MODELITEM(TK_MODELITEM_ID,TK_MODEL_ID,TK_TYPE_ID,TASKSTYLE,IS_FRAME_RELATIVE,TASK_START,TASK_END)
          values (100, 1012, 10, 0, 1, 10 * 60, 60 * 60)
          """.trimMargin(),
        )

        jdbcTemplate.update(
          """Insert into TW_SCHEDULE (TW_SCHEDULE_ID, ON_DATE, LEVEL_ID, ST_STAFF_ID, LAYER, PU_PLANUNIT_ID, REF_ID, TASK_START, TASK_END, OPTIONAL_1, SCHED_LASTMODIFIED, OBJECT_TYPE_ID, TK_MODEL_INFO_ID)
          values (1000001, '2022-03-13', 1000, 1147, -1, 1007, 11, 0, 0, 12, '2022-03-13', 6001, 1012)""",
        )
        jdbcTemplate.update(
          """Insert into TW_SCHEDULE (TW_SCHEDULE_ID, ON_DATE, LEVEL_ID, ST_STAFF_ID, LAYER, PU_PLANUNIT_ID, REF_ID, TASK_START, TASK_END, OPTIONAL_1, SCHED_LASTMODIFIED, OBJECT_TYPE_ID, TK_MODEL_INFO_ID)
          values (1000002, '2022-03-13', 1000, 1147, -1, 1007, 9, $TWO_HRS, $NINE_HRS, 0, '2022-03-13', 6003, 1012)""",
        )
      }

      // Note some tables are still populated from flyway SQL
      val responseBody = RegionContext.setRegion(1) {
        detailService.getStaffDetails(LocalDate.parse("2022-03-10"), LocalDate.parse("2022-03-20"))
      }

      assertThat(responseBody).containsExactly(
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T01:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T01:05:00"),
          activity = "present",
        ),
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T01:05:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T01:10:00"),
          activity = "break",
        ),
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T01:10:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T02:00:00"),
          activity = "present",
        ),
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-13T02:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-13T09:00:00"),
          activity = "plain activity",
        ),
      )
    }
  }

  @Nested
  inner class Notification {

    private val testRowMapper: RowMapper<Long> = RowMapper { rs: ResultSet, _: Int -> rs.getLong(1) }

    private val insert =
      "insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, ACTION_TYPE, TASK_START, TASK_END, REF_ID, OPTIONAL_1) values"
    private val insertR2 =
      "insert into R2.CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, ACTION_TYPE, TASK_START, TASK_END, REF_ID, OPTIONAL_1) values"

    @Test
    @WithMockAuthUser(username = "TEST-USER")
    fun testGetNotifications() {
      RegionContext.setRegion(1) {
        jdbcTemplate.update("insert into TK_TYPE( TK_TYPE_ID,  NAME) values (11, 'type 11')")
        jdbcTemplate.update("insert into TK_MODEL(TK_MODEL_ID, NAME, FRAME_START, FRAME_END, IS_DELETED) values (12, 'model 12', $ONE_HR, $TWO_HRS, 0)")

        jdbcTemplate.update("$insert (101, 1147, 1000, '2022-03-21', CURRENT_DATE,     null,  $NINE_HRS, $TEN_HRS, 11,null)")
        jdbcTemplate.update("$insert (102, 1148, 1000, '2022-03-22', CURRENT_DATE + 1, null,  $NINE_HRS, $TEN_HRS, null,12)")
        jdbcTemplate.update("$insert (103, 1148, 1000, '2022-03-22', CURRENT_DATE + 1, 47015, null, null, null, null)")
        jdbcTemplate.update("$insert (104, 1148, 4000, '2022-03-22', CURRENT_DATE + 1, 47012, null, null, null, null)")
        jdbcTemplate.update("$insert (105, 1148, 4000, '2022-03-22', CURRENT_DATE + 1, 47999, null, null, null, null)")
        jdbcTemplate.update("$insert (106, 1100, 4000, '2022-03-22', CURRENT_DATE - 1, null,  $NINE_HRS, $TEN_HRS, null,12)") // staff id not in tw_protocol means null timestamp

        assertThat(detailService.getModified()).containsExactlyInAnyOrder(
          CsrModifiedDetailDto(
            id = 101,
            quantumId = "TEST-USER",
            shiftModified = LocalDateTime.parse("2099-08-21T00:00:00"),
            shiftType = ShiftType.SHIFT,
            detailStart = LocalDateTime.parse("2022-03-21T09:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-21T10:00:00"),
            activity = "type 11",
            actionType = DetailModificationType.EDIT,
          ),
          CsrModifiedDetailDto(
            id = 102,
            quantumId = "a_1148",
            shiftModified = LocalDateTime.parse("2099-08-21T00:00:00"),
            shiftType = ShiftType.SHIFT,
            // not overridden by tk_model
            detailStart = LocalDateTime.parse("2022-03-22T09:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-22T10:00:00"),
            activity = "model 12",
            actionType = DetailModificationType.EDIT,
          ),
          CsrModifiedDetailDto(
            id = 103,
            quantumId = "a_1148",
            shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
            shiftType = ShiftType.SHIFT,
            detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
            activity = null,
            actionType = DetailModificationType.ADD,
          ),
          CsrModifiedDetailDto(
            id = 104,
            quantumId = "a_1148",
            shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
            shiftType = ShiftType.OVERTIME,
            detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
            activity = null,
            actionType = DetailModificationType.DELETE,
          ),
          CsrModifiedDetailDto(
            id = 105,
            quantumId = "a_1148",
            shiftModified = LocalDate.now().plusDays(1).atStartOfDay(),
            shiftType = ShiftType.OVERTIME,
            detailStart = LocalDateTime.parse("2022-03-22T00:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-22T00:00:00"),
            activity = null,
            actionType = DetailModificationType.UNCHANGED,
          ),
          CsrModifiedDetailDto(
            id = 106,
            quantumId = "a_1100",
            // NOTE: from CMD_NOTIFICATION, not TW_PROTOCOL
            shiftModified = LocalDate.now().minusDays(1).atStartOfDay(),
            shiftType = ShiftType.OVERTIME,
            detailStart = LocalDateTime.parse("2022-03-22T09:00:00"),
            detailEnd = LocalDateTime.parse("2022-03-22T10:00:00"),
            activity = "model 12",
            actionType = DetailModificationType.EDIT,
          ),
        )
      }
    }

    @Test
    @WithMockAuthUser(username = "TEST-USER")
    fun testDeleteNotifications() {
      RegionContext.setRegion(1) {
        jdbcTemplate.update("$insert (101, 1147, 1000, '2022-03-21', SYSDATE,     47001, 0,0, null,null)")
        jdbcTemplate.update("$insert (102, 1148, 4000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")
        jdbcTemplate.update("$insert (103, 1149, 1000, '2022-03-22', SYSDATE + 1, 47006, 0,0, null,null)")

        detailService.deleteProcessed(listOf(101, 103, 999))

        assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList().containsExactly(102L)
      }
    }

    @Test
    fun testDeleteOld() {
      RegionContext.setRegion(1) {
        jdbcTemplate.update("$insert (101, 1147, 1000, '2022-01-01', to_date('2022-03-01 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
        jdbcTemplate.update("$insert (102, 1147, 1000, '2022-01-01', to_date('2022-03-02 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
        jdbcTemplate.update("$insert (103, 1147, 1000, '2022-01-01', to_date('2022-03-03 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")
        jdbcTemplate.update("$insert (104, 1148, 4000, '2022-01-01', to_date('2022-03-04 08:00', 'YYYY-MM-DD HH24:MI'), 47001, 0,0, null,null)")

        assertThat(detailService.deleteOld(LocalDate.parse("2022-03-03"))).contains("Deleted 2 rows up to 2022-03-03, time taken ")

        assertThat(jdbcTemplate.query("SELECT ID FROM CMD_NOTIFICATION", testRowMapper)).asList()
          .containsExactly(103L, 104L)
      }
    }
  }
}
