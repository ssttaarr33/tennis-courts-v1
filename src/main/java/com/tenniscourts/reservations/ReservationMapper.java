package com.tenniscourts.reservations;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    Reservation map(ReservationDTO source);

    ReservationDTO map(Reservation source);

    @Mapping(target = "guest.id", source = "guestId")
    @Mapping(target = "schedule.id", source = "scheduleId")
    Reservation map(CreateReservationRequestDTO source);

    List<ReservationDTO> map(List<Reservation> source);

}
