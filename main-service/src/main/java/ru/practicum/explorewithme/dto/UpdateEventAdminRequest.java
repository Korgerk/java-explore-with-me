package ru.practicum.explorewithme.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.model.enums.StateAction;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {
    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    String description;

    Long category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    LocationDto location;
    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;
    Boolean requestModeration;
    StateAction stateAction;
}