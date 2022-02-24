package com.tenniscourts.reservations;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReservationFilterDTO {

    @NotNull
    @ApiModelProperty(required = true)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

}
