package ru.practicum.explorewithme.request.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    @NotNull
    private String status;
}