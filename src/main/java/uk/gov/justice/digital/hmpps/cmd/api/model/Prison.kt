package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Prison")
data class Prison(
        @Id
        @Column(nullable = false, name = "id")
        var id: Int,

        @Column(name = "Name")
        var name: String,

        @Column(name = "Description")
        var description: String? = null,

        @Column(name = "ApiUrl")
        var apiUrl: String,

        @Column(name = "RegionId")
        var regionId: Int? = null
)
