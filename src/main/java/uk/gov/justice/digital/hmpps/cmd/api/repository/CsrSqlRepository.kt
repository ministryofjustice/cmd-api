package uk.gov.justice.digital.hmpps.cmd.api.repository

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.cmd.api.config.CsrConfiguration
import uk.gov.justice.digital.hmpps.cmd.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Detail
import uk.gov.justice.digital.hmpps.cmd.api.model.DetailTemplate
import java.sql.ResultSet
import java.time.LocalDate

const val DAY_MODEL = 6001
const val ACTIVITY = 6003

@Repository
class CsrSqlRepository(
  @Qualifier("regionJdbcTemplate") private val regionJdbcTemplate: NamedParameterJdbcTemplate,
  csrConfiguration: CsrConfiguration,
) {
  private val schemaCommand = if (csrConfiguration.driverClassName.contains("h2")) {
    "SET SCHEMA "
  } else {
    "ALTER SESSION SET CURRENT_SCHEMA = "
  }
  fun getDetails(from: LocalDate, to: LocalDate, quantumId: String): Collection<Detail> = regionJdbcTemplate.query(
    GET_DETAILS,
    MapSqlParameterSource()
      .addValue("from", from)
      .addValue("to", to)
      .addValue("quantumId", quantumId),
    detailsRowMapper,
  )

  fun getModified(): List<CmdNotification> = regionJdbcTemplate.query(GET_MODIFIED, modifiedRowMapper)

  fun deleteProcessed(ids: List<Long>) = regionJdbcTemplate.update("delete from CMD_NOTIFICATION where ID in (:ids)", mapOf("ids" to ids))

  fun deleteAll() = regionJdbcTemplate.update("delete from CMD_NOTIFICATION", emptyMap<String, String>())

  fun deleteOld(date: LocalDate) = regionJdbcTemplate.update("delete from CMD_NOTIFICATION where LASTMODIFIED < :date", mapOf("date" to date))

  fun getDetailTemplates(templateNames: Collection<String>): Collection<DetailTemplate> = regionJdbcTemplate.query(
    GET_DETAIL_TEMPLATES,
    MapSqlParameterSource()
      .addValue("values", templateNames),
    detailsTemplateRowMapper,
  )

  fun setSchema(schemaName: String) = regionJdbcTemplate.update("$schemaCommand $schemaName", emptyMap<String, String>())

  companion object {

    val detailsRowMapper: RowMapper<Detail> = RowMapper { resultSet: ResultSet, _: Int ->
      Detail(
        null,
        null,
        resultSet.getDate("shiftDate").toLocalDate(),
        resultSet.getInt("shiftType"),
        resultSet.getLong("startTime"),
        resultSet.getLong("endTime"),
        resultSet.getString("activity"),
        null,
        resultSet.getString("templateName"),
      )
    }

    val modifiedRowMapper: RowMapper<CmdNotification> = RowMapper { resultSet: ResultSet, _: Int ->
      CmdNotification(
        id = resultSet.getLong("id"),
        levelId = resultSet.getInt("level_id"),
        onDate = resultSet.getDate("on_date").toLocalDate(),
        quantumId = resultSet.getString("quantumId"),
        lastModified = resultSet.getTimestamp("lastmodified")?.toLocalDateTime(),
        actionType = resultSet.getInt("action_type"),
        startTimeInSeconds = resultSet.getLong("startTime"),
        endTimeInSeconds = resultSet.getLong("endTime"),
        activity = resultSet.getString("activity"),
      )
    }

    val detailsTemplateRowMapper: RowMapper<DetailTemplate> = RowMapper { resultSet: ResultSet, _: Int ->
      DetailTemplate(
        resultSet.getLong("startTime"),
        resultSet.getLong("endTime"),
        resultSet.getBoolean("isRelative"),
        resultSet.getString("activity"),
        resultSet.getString("templateName"),
      )
    }

    val GET_DETAILS = """
        SELECT DISTINCT sched.on_date as shiftDate, 
                        nvl (mi.frame_start, sched.task_start) as startTime,
                        nvl (mi.frame_end, sched.task_end) as endTime, 
                        DECODE (sched.level_id, 4000, 1, 0) as shiftType,
                        nvl (m.name, t.name) as activity,
                        m.name as templateName
        FROM tw_schedule sched
                INNER JOIN sm_user usr     ON sched.st_staff_id = usr.obj_id AND usr.obj_type = 3 AND usr.is_deleted = 0
                LEFT JOIN tk_type t        ON sched.object_type_id = $ACTIVITY AND sched.ref_id = t.tk_type_id
                LEFT JOIN tk_model_info mi ON sched.object_type_id = $DAY_MODEL AND sched.tk_model_info_id = mi.tk_model_info_id AND mi.is_deleted = 0
                LEFT JOIN tk_model m       ON sched.object_type_id = $DAY_MODEL AND sched.optional_1 = m.tk_model_id AND m.is_deleted = 0
        WHERE sched.on_date BETWEEN :from AND :to
        AND   sched.layer = -1
        AND   sched.level_id IN (1000, 4000)
        AND   LOWER(usr.name) = LOWER(:quantumId)
    """.trimIndent()
    // level_id: 1000 = schedule , 3000 = actual, 4000 = time recording, 5000 = External system

    val GET_MODIFIED = """
            SELECT 
                n.id,
                n.level_id,
                n.on_date,
                usr.name AS quantumId, 
                CASE WHEN n.action_type is null
                then coalesce(
                    ( SELECT MAX(tw_protocol.lastmodified)
                      FROM tw_protocol
                      WHERE st_staff_id = n.st_staff_id
                      AND level_id = n.level_id
                      AND layer = -1 
                      AND tw_protocol.on_date BETWEEN (SYSDATE - 1) AND (SYSDATE + 2)
                    ), n.lastmodified) -- backup if no tw_protocol row
                else n.lastmodified
                end as lastmodified,
                n.action_type,
                n.task_start as startTime, 
                n.task_end as endTime, 
                NVL (tk_model.name, tk_type.name) as activity

            FROM CMD_NOTIFICATION n
                LEFT JOIN sm_user usr ON n.ST_STAFF_ID = usr.OBJ_ID 
                    AND usr.OBJ_TYPE = 3 
                    AND usr.IS_DELETED = 0
                LEFT JOIN tk_type  ON n.ref_id     = tk_type.tk_type_id 
                LEFT JOIN tk_model ON n.optional_1 = tk_model.tk_model_id 
            WHERE processed = 0
    """.trimIndent()

    val GET_DETAIL_TEMPLATES = """
         SELECT i.TASK_START AS startTime,
                i.TASK_END AS endTime,
                i.IS_FRAME_RELATIVE AS isRelative,
                t.NAME AS activity,
                m.NAME AS templateName
         FROM tk_modelitem i
            JOIN tk_model_info mi ON i.TK_MODEL_ID = mi.TK_MODEL_INFO_ID AND mi.IS_DELETED = 0
            JOIN tk_model m ON mi.TK_MODEL_ID = m.TK_MODEL_ID AND m.IS_DELETED = 0
            JOIN tk_type t on t.TK_TYPE_ID = i.TK_TYPE_ID
            WHERE m.NAME IN (:values)
              AND i.taskstyle = 0
    """.trimIndent()
    // Yes, TK_MODEL_INFO.TK_MODEL_ID is a TK_MODEL_INFO_ID, not a TK_MODEL_ID !
  }
}
