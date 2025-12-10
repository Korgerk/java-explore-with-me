package ru.practicum.ewm.dto;

import lombok.*;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestStatusUpdateDto {

    private List<Long> requestIds;
    private RequestStatus status;

}